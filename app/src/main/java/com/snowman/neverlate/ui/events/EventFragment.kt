package com.snowman.neverlate.ui.events

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import com.snowman.neverlate.databinding.FragmentMapBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.snowman.neverlate.model.*



class EventFragment : Fragment() {
    private val TAG = "eventdetailsfragment"

    private val firebaseManager = FirebaseManager.getInstance()
    private lateinit var addEventsAdapter: AddEventsAdapter
    private lateinit var friendsRV: RecyclerView
    private lateinit var friendsAdapter: EventFriendsAdapter

    private var _binding: FragmentOneEventBinding? = null
    private val binding get() = _binding!!

    private val mockDataEventTime = "2024-04-22 23:30:00" // Replace it with specific event time

    private val handler = Handler(Looper.getMainLooper())
    // ------ Get Current Time Every 1000 ms ------ //
    private val updateRunnable = object : Runnable {
        override fun run() {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val formattedDateTime = currentDateTime.format(formatter)
            calculateRemainingTime(mockDataEventTime)
            binding.currentTimeTV.text = formattedDateTime
            handler.postDelayed(this, 1000) // schedule the next run
        }
    }

    // ------ Fetch ETA Every 10000 ms ------ //
    private val updateRunnableETA = object : Runnable {
        override fun run() {
            setUpETA()
            calculatePunctualStatus(mockDataEventTime)
            handler.postDelayed(this, 10000) // schedule the next run
        }
    }
    // ------ Dummy origin laglng data and destination laglng data for testing ------ //
    val originPos = LatLng(42.350050, -71.103846)
    val destinationPos = LatLng(42.365824, -71.062756)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOneEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpETA()
        handler.post(updateRunnable)
        handler.post(updateRunnableETA)
//        val args: EventDetailsFragmentArgs by navArgs()
//        val eventId = args.eventId
        setUpFriends(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateRunnable)
        handler.removeCallbacks(updateRunnableETA)
        _binding = null
    }

    // TODO: REPLACE THIS WITH EVENTS MEMBERS LIST
    private val MOCK_DATA_REMOVE_LATER = listOf("7G8aYM2nCQYee2Ty2gOF6WfJfZi2", "kqrDkWba3dVMLEUnGFjX4gjqmmF3",
    "rPzbvBIau8OLR9yenHZDyhVNcVX2", "uoFb8MuJOAeAaL2wZqE8PTmrS8M2")

    private fun setUpFriends(view: View) {
        friendsRV = view.findViewById(R.id.friendsRV)
        friendsRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        friendsAdapter = EventFriendsAdapter(mutableListOf(), requireContext())
        firebaseManager.getUsersDataForIds(MOCK_DATA_REMOVE_LATER) { users ->
            friendsAdapter.setData(users)
            friendsAdapter.notifyDataSetChanged()
        }
        friendsRV.adapter = friendsAdapter
        PagerSnapHelper().attachToRecyclerView(friendsRV)
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
            val apiKey = getMetaData("com.google.android.geo.API_KEY")
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

    private fun getMetaData(name: String): String? {
        val context = context ?: return null
        return try {
            val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            applicationInfo.metaData.getString(name)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    // ------ Remaining Time = Event Time - Current Time ------ //
    private fun calculateRemainingTime(eventTime: String) {
        val givenTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val givenTime = LocalDateTime.parse(eventTime, givenTimeFormatter)
        val currentTime = LocalDateTime.now()
        val duration = Duration.between(currentTime, givenTime)
        val formattedDuration = formatDuration(duration)
        binding.remainingTimeTV.text = formattedDuration
    }

    // ------ Punctual Status based on => Event Time - Expected Time of Arrival ------ //
    private fun calculatePunctualStatus(eventTime: String) {
        val givenTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val givenTime = LocalDateTime.parse(eventTime, givenTimeFormatter)
        val etaTime = LocalDateTime.parse(binding.etaTV.text, givenTimeFormatter)
        val duration = Duration.between(etaTime, givenTime)

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

}