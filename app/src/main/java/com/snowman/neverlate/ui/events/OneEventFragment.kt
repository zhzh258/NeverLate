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
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import com.snowman.neverlate.model.*
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.shared.SharedOneEventViewModel
import com.snowman.neverlate.model.types.Event
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
    val auth = com.google.firebase.ktx.Firebase.auth
    private val currentUser = auth.currentUser


    private var _binding: FragmentOneEventBinding? = null
    private val binding get() = _binding!!

    private val sharedOneEventViewModel: SharedOneEventViewModel by activityViewModels()
    private lateinit var event: Event // The current event
    private var rt: Duration = Duration.ofSeconds(0)
    private var ett: Duration = Duration.ofSeconds(0) // estimated travel time
    private var eta: LocalDateTime = LocalDateTime.now() // estimated time (to) arrive

    private val handler = Handler(Looper.getMainLooper())
    // ------ Get Current Time Every 1000 ms ------ //
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isAdded && isVisible && binding != null) {
                val currentDateTime = LocalDateTime.now()
                binding.currentTimeTV.text = TimeUtil.localDateTime2FormattedString(currentDateTime)
                calculateRemainingTime()
                handler.postDelayed(this, 1000) // schedule the next run
            }
        }
    }

    // ------ Fetch ETA Every 10000 ms ------ //
    private val updateRunnableETA = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            if (isAdded && isVisible) {
                setUpETA()
                calculatePunctualStatus()
                handler.postDelayed(this, 500000) // schedule the next run
            }
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
        sharedOneEventViewModel.selectedEvent.observe(viewLifecycleOwner) { it ->
            if (it == null) {
                throw NullPointerException("zz - sharedOneEventViewModel.selectedEvent.value is null. Have you updated it before navigating to this fragment?")
            }

            it.let {
                Toast.makeText(requireContext(), "Hey! The event is ${it.name}", Toast.LENGTH_SHORT).show()
                event = it
                view.findViewById<TextView>(R.id.text_title).text = it.name
                view.findViewById<TextView>(R.id.textview_description).text = it.description
                view.findViewById<TextView>(R.id.text_event_time).text = TimeUtil.dateFormat.format(it.date.toDate())
                view.findViewById<TextView>(R.id.text_event_location).text = it.location.toString()
                view.findViewById<TextView>(R.id.text_people_count).text = it.members.size.toString() + " people"
                setUpFriends(view)
                setUpMapNavigation(view)
                updateRunnableETA.run()
                Glide.with(this)
                    .load(it.photoURL)
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
            Handler(Looper.getMainLooper()).postDelayed({
                checkEventStatusAndDeactivateIfRequired()
            }, 5000)
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
        var memberStatusList1 = event.members.toMutableList()
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
            findNavController().navigate(R.id.nav_map)
        }
    }
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
            val duration = drivingResponse?.routes?.get(0)?.legs?.get(0)?.duration
            val distance = drivingResponse?.routes?.get(0)?.legs?.get(0)?.distance
            ett = Duration.ofSeconds(duration?.value?.toLong() ?: 0)
            binding.ettTV.text = "Drive: " + duration?.text + " ( " + distance.toString() + " )"

            // ------ Setting Expected Time of Arrival ------ //
            val durationValue = drivingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.value?: 0
            val currentDateTime = LocalDateTime.now()
            eta = currentDateTime.plusSeconds(durationValue.toLong())
            binding.etaTV.text = TimeUtil.localDateTime2FormattedString(eta)
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
        val minutes = rt.toMinutes()
        val status  = when {
            minutes.toLong() > 20 -> getString(R.string.preStatus_1)
            minutes.toLong() > 5 -> getString(R.string.preStatus_2)
            minutes.toLong() > -5 -> getString(R.string.preStatus_3)
            minutes.toLong() > -15 -> getString(R.string.preStatus_4)
            minutes.toLong() > -25 -> getString(R.string.preStatus_5)
            else -> getString(R.string.preStatus_6)
        }

        event.members.find { it.id == currentUserID }?.let { memberStatus ->
            val updatedMember = memberStatus.copy(arrived = true, arriveTime = minutes.toLong(), status = status)
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
        firebaseManager.updateMemberStatus(event.id, memberStatus, onSuccess, onFailure)
    }


    private fun checkEventStatusAndDeactivateIfRequired() {
        firebaseManager.checkAndDeactivateEventIfAllArrived(event.id) { allArrived ->
            if (allArrived) {
                Toast.makeText(context, "All members have arrived, event marked as inactive.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Not all members have arrived yet.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ------ Remaining Time = Event Time - Current Time ------ //
    private fun calculateRemainingTime() {
        val givenTime = TimeUtil.timeStamp2LocalDateTime(event.date)
        val currentTime = LocalDateTime.now()
        rt = Duration.between(currentTime, givenTime)
        binding.remainingTimeTV.text = TimeUtil.duration2FormattedString(rt)
    }

    // ------ Punctual Status based on => Event Time - Expected Time of Arrival ------ //
    private fun calculatePunctualStatus() {
        val givenTime = TimeUtil.timeStamp2LocalDateTime(event.date)
        val etaTime = eta
        val duration = Duration.between(eta, givenTime)

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
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun formatDuration(duration: Duration): String {
//        val seconds = duration.seconds
//        val absSeconds = Math.abs(seconds)
//        val positive = String.format(
//            "%d:%02d:%02d",
//            absSeconds / 3600,
//            (absSeconds % 3600) / 60,
//            absSeconds % 60
//        )
//        return if (seconds < 0) "-$positive" else positive
//    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun convertEventTimeToStandardFormat(timestamp: com.google.firebase.Timestamp): String {
//        val instant = java.time.Instant.ofEpochSecond(timestamp.seconds, timestamp.nanoseconds.toLong())
//        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
//        var localDateTime = zonedDateTime.toLocalDateTime()
//        localDateTime = localDateTime.minusYears(1900)
//        localDateTime = localDateTime.minusHours(1)
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//        return formatter.format(localDateTime)
//    }

}