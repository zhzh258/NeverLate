package com.snowman.neverlate.ui.events

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.snowman.neverlate.R
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.databinding.FragmentOneEventBinding
import com.snowman.neverlate.model.retrofit.DirectionsResponse
import com.snowman.neverlate.model.retrofit.GoogleMapsDirectionsService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import java.util.Locale
import java.util.TimeZone
import java.time.ZoneId;
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import com.snowman.neverlate.model.*
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.shared.SharedOneEventViewModel
import com.snowman.neverlate.model.types.MemberStatus
import com.snowman.neverlate.util.TimeUtil
import com.snowman.neverlate.util.getMetaData
import java.text.SimpleDateFormat
import java.util.Date


class OneEventFragment : Fragment() {
    private val TAG = "eventdetailsfragment"

    private val firebaseManager = FirebaseManager.getInstance()
    private lateinit var addEventsAdapter: AddEventsAdapter
    private lateinit var friendsRV: RecyclerView
    private lateinit var friendsAdapter: EventFriendsAdapter
    private lateinit var theEvent: IEvent
    val auth = com.google.firebase.ktx.Firebase.auth
    private val currentUser = auth.currentUser


    private var _binding: FragmentOneEventBinding? = null
    private val binding get() = _binding!!

    private val sharedOneEventViewModel: SharedOneEventViewModel by activityViewModels()

    private val mockDataEventTime = "2024-04-22 23:30:00" // Replace it with specific event time
    private lateinit var DataEventTime: String
    private val handler = Handler(Looper.getMainLooper())
    // ------ Get Current Time Every 1000 ms ------ //
    private val updateRunnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedDateTime = currentDateTime.format(formatter)
            calculateRemainingTime(DataEventTime)
            binding.currentTimeTV.text = formattedDateTime
            handler.postDelayed(this, 1000) // schedule the next run
        }
    }

    // ------ Fetch ETA Every 10000 ms ------ //
    private val updateRunnableETA = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            setUpETA()
            calculatePunctualStatus(DataEventTime)
            handler.postDelayed(this, 1000000) // schedule the next run
        }
    }
    // ------ Dummy origin laglng data and destination laglng data for testing ------ //
    val originPos = LatLng(42.350050, -71.103846)
    val destinationPos = LatLng(42.365824, -71.062756)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("MY_DEBUG", "OneEventFragment: onCreateView")
        _binding = FragmentOneEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("MY_DEBUG", "OneEventFragment: onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        sharedOneEventViewModel.selectedEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                theEvent = event
                view.findViewById<TextView>(R.id.text_title).text = theEvent.name
                view.findViewById<TextView>(R.id.textview_description).text = theEvent.description
                view.findViewById<TextView>(R.id.text_event_time).text = TimeUtil.dateFormat.format(theEvent.date.toDate())
                view.findViewById<TextView>(R.id.text_event_location).text = event.location.toString()
                view.findViewById<TextView>(R.id.text_people_count).text = event.members.size.toString() + " people"
                setUpFriends(view)
                setUpMapNavigation(view)
                DataEventTime = convertEventTimeToStandardFormat(theEvent.date)
                Log.d("DataEventTime", DataEventTime)
                updateRunnableETA.run()
                Glide.with(this)
                    .load(theEvent.photoURL)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_util_clear_24)
                    .into(binding.imageBackground)
            }
        }
        handler.post(updateRunnable)
        handler.postDelayed({
            updateRunnableETA.run()
            handler.postDelayed(updateRunnableETA, 1000000) // Schedule subsequent updates after initial run
        }, 10000) // Delay the first execution by 10 seconds

        //handler.post(updateRunnableETA)
        val btnArrived = view.findViewById<Button>(R.id.btn_arrived)
        btnArrived.setOnClickListener {
            markAsArrived()
        }

//        val args: EventDetailsFragmentArgs by navArgs()
//        val eventId = args.eventId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("MY_DEBUG", "OneEventFragment: onDestroyView")
        handler.removeCallbacks(updateRunnable)
        handler.removeCallbacks(updateRunnableETA)
        _binding = null
    }


    private fun setUpFriends(view: View) {
        var friendsNames = ""
        var memberStatusList1 = theEvent.members.toMutableList()
        for(member in memberStatusList1) {
            if(currentUser != null) {
                if (member.id.equals(currentUser.uid)) {
                    memberStatusList1.remove(member)
                    break
                }
            }
        }
        friendsRV = view.findViewById(R.id.friendsRV)
        friendsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        friendsAdapter = EventFriendsAdapter(mutableListOf(), requireContext())
        val memberStatusList = memberStatusList1.toList()
        val userIdList = memberStatusList.map { memberStatus ->
            memberStatus.id
        }
        firebaseManager.getUsersDataForIds(userIdList) { users ->
            friendsAdapter.setData(users)
            friendsAdapter.notifyDataSetChanged()
        }
        friendsRV.adapter = friendsAdapter
        PagerSnapHelper().attachToRecyclerView(friendsRV)
    }

    private fun setUpMapNavigation(view: View) {
        val toMapButton: Button = view.findViewById(R.id.to_map_button)
        toMapButton.setOnClickListener {
            findNavController().navigate(R.id.action_oneEventFragment_to_mapFragment)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpETA() {
        lifecycleScope.launch {
            val driving = async { fetchDirectionsResponse(originPos, "driving", destinationPos) }
            val walking = async { fetchDirectionsResponse(originPos, "walking", destinationPos) }
            val bicycling = async { fetchDirectionsResponse(originPos, "bicycling", destinationPos) }
            val transit = async { fetchDirectionsResponse(originPos, "transit", destinationPos) }
            // await all
            val responses = awaitAll(driving, walking, bicycling, transit)
            val (drivingResponse, walkingResponse, bicyclingResponse, transitResponse) = responses
            // ------ Setting Expected Travel Time ------ //
            val duration = drivingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
            val distance = drivingResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
            binding.ettTV.text = "Drive: " + duration + " ( " + distance + " )"
            // ------ Setting Expected Time of Arrival ------ //
            val durationValue = drivingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.value?: 0
            val currentDateTime = LocalDateTime.now()
            val eta = currentDateTime.plusSeconds(durationValue.toLong())
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedETA = eta.format(formatter)
            binding.etaTV.text = "$formattedETA"
        }
    }

    private suspend fun fetchDirectionsResponse(origin: LatLng?, mode: String, destination: LatLng?): DirectionsResponse? { // show a line from current location to
        origin ?: return null
        destination ?: return null
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(GoogleMapsDirectionsService::class.java)
        try {
            val apiKey = getMetaData("com.google.android.geo.API_KEY", context)
            val response = service.getDirections("${origin.latitude},${origin.longitude}", "${destination.latitude},${destination.longitude}", mode, apiKey?:"" )
            if (response.isSuccessful) {
                if(response.body() == null || response.body()?.routes == null || response.body()?.routes?.size == 0){
                    Toast.makeText(requireContext(), "Is Successful but Null", Toast.LENGTH_SHORT).show()
                    return null
                }
                else
                    return response.body()
            } else {
                Toast.makeText(requireContext(), "Not Successful", Toast.LENGTH_SHORT).show()
                return null
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Other Exceptions", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun markAsArrived() {

        val currentUserID = auth.currentUser?.uid
        val remainingTimeText = binding.remainingTimeTV.text.toString()
        val minutes = extractMinutes(remainingTimeText)
        val status  = when {
            minutes.toLong() > 20 -> getString(R.string.preStatus_1)
            minutes.toLong() > 5 -> getString(R.string.preStatus_2)
            minutes.toLong() > -5 -> getString(R.string.preStatus_3)
            minutes.toLong() > -15 -> getString(R.string.preStatus_4)
            minutes.toLong() > -25 -> getString(R.string.preStatus_5)
            else -> getString(R.string.preStatus_6)
        }

        theEvent.members.find { it.id == currentUserID }?.let { memberStatus ->
            val updatedMember = memberStatus.copy(isArrived = true, arriveTime = minutes.toLong(), status = status)
            updateMemberStatusInDatabase(updatedMember, {
                // Success callback
                /////checkEventStatusAndDeactivateIfRequired()
                //---markEventAsInactive()
                Toast.makeText(context, "Arrival time updated successfully.", Toast.LENGTH_SHORT).show()
            }, {
                // Failure callback
                    e -> Toast.makeText(context, "Failed to update arrival time: ${e.message}", Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun updateMemberStatusInDatabase(memberStatus: MemberStatus, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firebaseManager.updateMemberStatus(theEvent.id, memberStatus, onSuccess, onFailure)
    }

    private fun extractMinutes(remainingTimeText: String): Int {
        val parts = remainingTimeText.split(":")
        if (parts.size == 3) {
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            val isNegative = hours < 0
            val totalMinutes = if (isNegative) hours * 60 - minutes else hours * 60 + minutes
            return totalMinutes
        }
        return 0
    }

    private fun checkEventStatusAndDeactivateIfRequired() {
        firebaseManager.checkAndDeactivateEventIfAllArrived(theEvent.id) { allArrived ->
            if (allArrived) {
                Toast.makeText(context, "All members have arrived, event marked as inactive.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Not all members have arrived yet.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun markEventAsInactive() {
        val eventId = theEvent.id // Assuming theEvent is an instance of IEvent and has an id field

        firebaseManager.updateEventActiveStatus(eventId, false,
            onSuccess = {
                Toast.makeText(context, "Event marked as inactive successfully.", Toast.LENGTH_SHORT).show()
            },
            onFailure = { e ->
                Toast.makeText(context, "Failed to mark event as inactive: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }


    // ------ Remaining Time = Event Time - Current Time ------ //
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateRemainingTime(eventTime: String) {
        val givenTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val givenTime = LocalDateTime.parse(eventTime, givenTimeFormatter)
        val currentTime = LocalDateTime.now()
        val duration = Duration.between(currentTime, givenTime)
        val formattedDuration = formatDuration(duration)
        binding.remainingTimeTV.text = formattedDuration
    }

    // ------ Punctual Status based on => Event Time - Expected Time of Arrival ------ //
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculatePunctualStatus(eventTime: String) {
        val givenTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val givenTime = LocalDateTime.parse(eventTime, givenTimeFormatter)
        val etaTime = LocalDateTime.parse(binding.etaTV.text, givenTimeFormatter)
        val duration = Duration.between(etaTime, givenTime)
        Log.d("givenTime", givenTime.toString())
        Log.d("etaTime", etaTime.toString())
        Log.d("duration", duration.toString())
        val durationMinutes = duration.toMinutes()
        binding.punctualityTV.text = when {
            durationMinutes > 30 -> getString(R.string.status_1)
            durationMinutes > 10 -> getString(R.string.status_2)
            durationMinutes > 5 -> getString(R.string.status_3)
            durationMinutes > 0 -> getString(R.string.status_4)
            durationMinutes > -10 -> getString(R.string.status_5)
            durationMinutes > -30 -> getString(R.string.status_6)
            else -> getString(R.string.status_7)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDuration(duration: Duration): String {
        val seconds = duration.seconds
        val absSeconds = Math.abs(seconds)
        val positive = String.format(
            "%d:%02d:%02d",
            absSeconds / 3600,
            (absSeconds % 3600) / 60,
            absSeconds % 60
        )
        return if (seconds < 0) "-$positive" else positive
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertEventTimeToStandardFormat(timestamp: com.google.firebase.Timestamp): String {
        val instant = java.time.Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong())
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        var localDateTime = zonedDateTime.toLocalDateTime()
        localDateTime = localDateTime.minusYears(1900)
        localDateTime = localDateTime.minusHours(1)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return formatter.format(localDateTime)
    }

}