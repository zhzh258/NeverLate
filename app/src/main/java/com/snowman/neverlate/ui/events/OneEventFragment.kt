package com.snowman.neverlate.ui.events

import android.Manifest
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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import kotlinx.coroutines.tasks.await
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
                //binding.currentTimeTV.text = TimeUtil.localDateTime2FormattedString(currentDateTime)
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
                Toast.makeText(requireContext(), "Hey! The event is ${it.name}", Toast.LENGTH_SHORT)
                    .show()
                event = it
                view.findViewById<TextView>(R.id.text_title).text = it.name
                view.findViewById<TextView>(R.id.textview_description).text = it.description
                view.findViewById<MaterialButton>(R.id.event_detail_button).setOnClickListener { _ ->
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(resources.getString(R.string.title))
                        .setMessage("Date: ${TimeUtil.timestamp2FormattedString(it.date)}\n" +
                                "Duration: ${it.duration} minutes\n")
                        .setPositiveButton("OK") { dialog, which ->
                            // Respond to positive button press
                        }
                        .show()
                }

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
            handler.postDelayed(
                updateRunnableETA,
                1000000
            ) // Schedule subsequent updates after initial run
        }, 10000) // Delay the first execution by 10 seconds

        //handler.post(updateRunnableETA)
        val btnArrived = view.findViewById<Button>(R.id.btn_arrived)
        btnArrived.setOnClickListener {
            markAsArrived()
            Handler(Looper.getMainLooper()).postDelayed({
                checkEventStatusAndDeactivateIfRequired()
            }, 5000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("MY_DEBUG", "OneEventFragment: onDestroyView")
        handler.removeCallbacks(updateRunnable)
        handler.removeCallbacks(updateRunnableETA)
        _binding = null
    }


    private fun setUpFriends(view: View) {
        var memberStatusList1 = event.members.toMutableList()
        for (member in memberStatusList1) {
            if (currentUser != null) {
                if (member.id.equals(currentUser.uid)) {
                    memberStatusList1.remove(member)
                    break
                }
            }
        }
        friendsRV = view.findViewById(R.id.friendsRV)
        friendsRV.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
            val originPos = getUserGPS()
            val destinationPos = LatLng(event.location.latitude, event.location.longitude)
            val driving = async { fetchDirectionsResponse(originPos, "driving", destinationPos) }
            val walking = async { fetchDirectionsResponse(originPos, "walking", destinationPos) }
            val bicycling =
                async { fetchDirectionsResponse(originPos, "bicycling", destinationPos) }
            val transit = async { fetchDirectionsResponse(originPos, "transit", destinationPos) }
            // await all
            val responses = awaitAll(driving, walking, bicycling, transit)
            val (drivingResponse, walkingResponse, bicyclingResponse, transitResponse) = responses
            // ------ Setting Expected Travel Time ------ //
            val duration = drivingResponse?.routes?.get(0)?.legs?.get(0)?.duration
            val distance = drivingResponse?.routes?.get(0)?.legs?.get(0)?.distance
            ett = Duration.ofSeconds(duration?.value?.toLong() ?: 0)
            binding.ettTV.text = "Drive: " + duration?.text // + " ( " + distance.toString() + " )"
            val minutes = -1 * ett.toMinutes().toFloat()  // Convert minutes to Float once
            binding.ETTSlider.value = when {
                minutes < -100 -> -100f  // Ensure the value is a Float
                minutes > 100 -> 100f    // Ensure the value is a Float
                else -> minutes
            }
            // ------ Setting Expected Time of Arrival ------ //
            val durationValue = drivingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.value ?: 0
            val currentDateTime = LocalDateTime.now()
            eta = currentDateTime.plusSeconds(durationValue.toLong())
        }
    }

    private suspend fun fetchDirectionsResponse(
        origin: LatLng?,
        mode: String,
        destination: LatLng?
    ): DirectionsResponse? { // show a line from current location to
        origin ?: return null
        destination ?: return null
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(GoogleMapsDirectionsService::class.java)
        try {
            val apiKey = getMetaData("com.google.android.geo.API_KEY", context)
            val response = service.getDirections(
                "${origin.latitude},${origin.longitude}",
                "${destination.latitude},${destination.longitude}",
                mode,
                apiKey ?: ""
            )
            if (response.isSuccessful) {
                if (response.body() == null || response.body()?.routes == null || response.body()?.routes?.size == 0) {
                    Toast.makeText(requireContext(), "Is Successful but Null", Toast.LENGTH_SHORT)
                        .show()
                    return null
                } else
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
        val status = when {
            minutes.toLong() > 20 -> getString(R.string.preStatus_1)
            minutes.toLong() > 5 -> getString(R.string.preStatus_2)
            minutes.toLong() > -5 -> getString(R.string.preStatus_3)
            minutes.toLong() > -15 -> getString(R.string.preStatus_4)
            minutes.toLong() > -25 -> getString(R.string.preStatus_5)
            else -> getString(R.string.preStatus_6)
        }

        event.members.find { it.id == currentUserID }?.let { memberStatus ->
            val updatedMember =
                memberStatus.copy(arrived = true, arriveTime = minutes.toLong(), status = status)
            updateMemberStatusInDatabase(updatedMember, {
                // Success callback
                /////checkEventStatusAndDeactivateIfRequired()
                //---markEventAsInactive()
                Toast.makeText(context, "Arrival time updated successfully.", Toast.LENGTH_SHORT)
                    .show()
            }, {
                // Failure callback
                    e ->
                Toast.makeText(
                    context,
                    "Failed to update arrival time: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            })
        }
    }

    private fun updateMemberStatusInDatabase(
        memberStatus: MemberStatus,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firebaseManager.updateMemberStatus(event.id, memberStatus, onSuccess, onFailure)
    }


    private fun checkEventStatusAndDeactivateIfRequired() {
        firebaseManager.checkAndDeactivateEventIfAllArrived(event.id) { allArrived ->
            if (allArrived) {
                Log.d("checkEventStatus", "All members have arrived, event marked as inactive.")
            } else {
                Log.d("checkEventStatus", "Not all members have arrived yet.")
            }
        }
    }


    // ------ Remaining Time = Event Time - Current Time ------ //
    private fun calculateRemainingTime() {
        val givenTime = TimeUtil.timeStamp2LocalDateTime(event.date)
        val currentTime = LocalDateTime.now()
        rt = Duration.between(currentTime, givenTime)
        binding.remainingTimeTV.text = TimeUtil.duration2FormattedString(rt)
        val rt_minutes = -1 * rt.toMinutes().toFloat()  // Convert minutes to Float once
        binding.RTSlider.value = when {
            rt_minutes < -100 -> -100f  // Ensure the value is a Float
            rt_minutes > 100 -> 100f    // Ensure the value is a Float
            else -> rt_minutes
        }
        val eta_minutes = -1 * (rt.toMinutes().toFloat() - ett.toMinutes().toFloat())
        binding.ETASlider.value = when {
            eta_minutes < -100 -> -100f  // Ensure the value is a Float
            eta_minutes > 100 -> 100f    // Ensure the value is a Float
            else -> eta_minutes
        }
        binding.etaTV.text = when {
            -1 * eta_minutes > 0 -> "${-1 * eta_minutes.toInt()} mins earlier"
            -1 * eta_minutes < 0 -> "${eta_minutes.toInt()} mins late"
            else -> "0 minutes late"
        }
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


    private suspend fun getUserGPS(): LatLng? {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { // permission is NOT granted
            Toast.makeText(
                requireContext(),
                "please enable location permissions in device settings",
                Toast.LENGTH_SHORT
            ).show()
            return null;
        } else { // permission is granted, show the location
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());
            val priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
            val cancellationTokenSource = CancellationTokenSource()

            val location = fusedLocationProviderClient.getCurrentLocation(
                priority,
                cancellationTokenSource.token
            ).await()
            if (location != null) {
                Log.d(
                    TAG,
                    "fusedLocationProviderClient successfully fetches a location ${location.toString()}"
                )
                Log.d(TAG, "updateUserGPS is called and the location is ${location.toString()}")
                val latLng = LatLng(location.latitude, location.longitude)

                return latLng
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location not available. Is this a new phone??? Or you reboot it just now?? Try again later.",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(
                    TAG,
                    "updateUserGPS is called and fusedLocationProviderClient failed to get a location"
                )
                Log.d(TAG, "No location available at this time.")
                return null
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult is called")
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                Log.d(TAG, "onRequestPermissionsResult - the user granted permission - go to if")
                lifecycleScope.launch {
                    getUserGPS()
                }
            } else {
                // Permission was denied. Handle the situation by showing a message to the user or taking appropriate action.
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}