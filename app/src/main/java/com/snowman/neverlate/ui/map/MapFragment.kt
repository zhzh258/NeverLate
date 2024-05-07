package com.snowman.neverlate.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.FragmentMapBinding
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.retrofit.DirectionsResponse
import com.snowman.neverlate.model.retrofit.GoogleMapsDirectionsService
import com.snowman.neverlate.model.shared.SharedOneEventViewModel
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.util.getMetaData
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Error

private val TAG = "Map Fragment"

class MapFragment : Fragment(), OnMapReadyCallback {
    val firebaseManager = FirebaseManager.getInstance()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var _mapViewModel: MapViewModel? = null
    private val mapViewModel get() = _mapViewModel!!
    private val sharedOneEventViewModel: SharedOneEventViewModel by activityViewModels()

    private var _bottomSheetBehavior:  BottomSheetBehavior<FrameLayout>? = null
    private val bottomSheetBehavior get() = _bottomSheetBehavior!!

    private lateinit var map: GoogleMap
    private val marker2event: HashMap<Marker, IEvent> = HashMap()
    private var polyline: Polyline? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("MY_DEBUG", "MapFragment: onViewCreated")
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]
        _bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.peekHeight = 300 // How much content to show when it's COLLAPSED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val mapFragment = childFragmentManager
            .findFragmentById(com.snowman.neverlate.R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)


        return binding.root
    }

    override fun onDestroyView() {
        Log.d("MY_DEBUG", "MapFragment: onDestroyView")
        super.onDestroyView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady() is called. Launching coroutine in lifecycleScope...")
        lifecycleScope.launch {
            map = googleMap
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            setMapConfig()
            observeCamera()
            val origin = getUserGPS() // suspend function
            origin?.let {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 12f))
            }
            if(origin != null){
                Log.d(TAG, "Moving camera to ${origin.toString()}")
                observeMarkers()
            }
            setUpBottomSheet()
        }
    }

    private fun setMapConfig() {
        map.isTrafficEnabled = false
    }

    private fun observeCamera() {
        mapViewModel.cameraPos.observe(viewLifecycleOwner) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
        }
    }

    private fun observeMarkers() {
        mapViewModel.markerData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                this.map.clear()
                this.marker2event.clear()
                val builder = LatLngBounds.Builder()

                it.forEach { markerData ->
                    val marker = this.map.addMarker(markerData.markerOptions)
                    marker?.let { this.marker2event[marker] = markerData.event }
                    builder.include(markerData.markerOptions.position)
                }

                val bounds = builder.build()
                val padding = resources.getDimensionPixelSize(R.dimen.map_padding)
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                map.animateCamera(cameraUpdate)
            }
        }
    }



    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        map.setOnMarkerClickListener { marker: Marker -> lifecycleScope.launch {
            val event = marker2event[marker] ?: throw Error("IEvent not found in the marker2event hashmap!")

            marker.showInfoWindow()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val origin: LatLng? = getUserGPS()
            // event card
            binding.eventCard.setOnClickListener {
                Toast.makeText(requireContext(), "Now navigating to event ${event.name}", Toast.LENGTH_SHORT).show()
                sharedOneEventViewModel.setSelectedEvent(event)
                findNavController().navigate(R.id.nav_eventDetails)
            }
            binding.eventNameTv.text = event.name
            Glide.with(requireActivity())
                .load(event.photoURL)
                .circleCrop()
                .error(R.mipmap.ic_launcher_round)
                .into(binding.eventImageIv)
            lifecycleScope.launch {
                var counter = event.members.size
                val usernameList = mutableListOf<String>()
                for (memberStatus in event.members) {
                    val userId = memberStatus.id
                //for (userId in event.members) {
                    firebaseManager.getUserDataForId(userId) { user ->
                        Log.d(TAG, "The user we get from $userId is${user.toString()}")

                        user?.let { usernameList.add(user.displayName) }
                        if(--counter == 0) {
                            binding.eventMembersTv.text = usernameList.joinToString(",")
                            Log.d(TAG, usernameList.toString())
                        }
                    }
                }
            }
            binding.eventPeopleNumberTv.text = event.members.size.toString()

            // Start all fetches in parallel
            val driving = async { fetchDirectionsResponse(origin, "driving", marker.position) }
            val walking = async { fetchDirectionsResponse(origin, "walking", marker.position) }
            val bicycling = async { fetchDirectionsResponse(origin, "bicycling", marker.position) }
            val transit = async { fetchDirectionsResponse(origin, "transit", marker.position) }

            // await all
            val responses = awaitAll(driving, walking, bicycling, transit)
            val (drivingResponse, walkingResponse, bicyclingResponse, transitResponse) = responses
            binding.driveButton.apply {
                this.setOnClickListener {
                    handleTransportButtonClicked("drive", marker, drivingResponse)
                }
                val duration = drivingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
                val distance = drivingResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
                this.text = "Drive: " + duration
            }
            binding.walkButton.apply {
                this.setOnClickListener {
                    handleTransportButtonClicked("walk", marker, walkingResponse)
                }
                val duration = walkingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
                val distance = walkingResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
                this.text = "Walk: " + duration
            }
            binding.bikeButton.apply {
                this.setOnClickListener {
                    handleTransportButtonClicked("bike", marker, bicyclingResponse)
                }
                val duration = bicyclingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
                val distance = bicyclingResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
                this.text = "Bike: " + duration
            }
            binding.transitButton.apply {
                this.setOnClickListener {
                    handleTransportButtonClicked("transit", marker, transitResponse)
                }
                val duration = transitResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
                val distance = transitResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
                this.text = "Transit: " + duration
            }

        }
            true
        }

        map.setOnMapClickListener {
            if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    private fun handleTransportButtonClicked(transport: String, marker: Marker, response: DirectionsResponse?) = lifecycleScope.launch {
        this@MapFragment.map.isTrafficEnabled = true
        val origin: LatLng? = getUserGPS()
        this@MapFragment.polyline?.remove()
        origin?.let { map.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 12f)) }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val polylineOptions = PolylineOptions().color(Color.BLUE)
        val points = PolyUtil.decode(response?.routes?.get(0)?.overview_polyline?.points ?: "")
        polylineOptions.addAll(points)
        this@MapFragment.polyline = map.addPolyline(polylineOptions)
        binding.navigationCard.setVisibility(View.VISIBLE)

        binding.destinationMb.text = marker2event[marker]?.address
        val distance = response?.routes?.get(0)?.legs?.get(0)?.distance?.text
        binding.transportChip.text = transport
        binding.distanceChip.text = distance
        binding.closeButton.setOnClickListener {
            binding.navigationCard.setVisibility(View.INVISIBLE)
            this@MapFragment.polyline?.remove()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            this@MapFragment.map.isTrafficEnabled = false;
        }
        binding.bikeButton.isChecked = false
        binding.walkButton.isChecked = false
        binding.driveButton.isChecked = false
        binding.transitButton.isChecked = false
        when (transport) {
            "bike" -> binding.bikeButton.isChecked = true
            "walk" -> binding.walkButton.isChecked = true
            "drive" -> binding.driveButton.isChecked = true
            "transit" -> binding.transitButton.isChecked = true
        }
    }

    /***
     * @param origin The LatLng of the origin point
     * @param mode Must be one of "drive", "walk", "bike", "transit". Or other mode supported by Google (idk).
     * @param destination The LatLng of the origin point
     * @return DirectionsResponse?
     */
    private suspend fun fetchDirectionsResponse(origin: LatLng?, mode: String, destination: LatLng): DirectionsResponse? {
        Log.d(TAG, "Now fetching data with origin = ${origin}")
        origin ?: return null

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GoogleMapsDirectionsService::class.java)

        try {
            val apiKey = getMetaData("com.google.android.geo.API_KEY", context)
            val response = service.getDirections("${origin.latitude},${origin.longitude}", "${destination.latitude},${destination.longitude}", mode, apiKey?:"" )
//            Log.d(TAG, response.body().toString())
            if (response.isSuccessful) {
                if(response.body() == null || response.body()?.routes == null || response.body()?.routes?.size == 0)
                    return null
                else
                    return response.body()
            } else {
                return null
            }
        } catch (e: Exception) {
            // Handle exceptions
            return null
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
            Toast.makeText(requireContext(), "please enable location permissions in device settings", Toast.LENGTH_SHORT).show()
            return null;
        } else { // permission is granted, show the location
            map.isMyLocationEnabled = true
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            val priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
            val cancellationTokenSource = CancellationTokenSource()

            val location = fusedLocationProviderClient.getCurrentLocation(priority, cancellationTokenSource.token).await()
            if(location != null){
                Log.d(TAG, "fusedLocationProviderClient successfully fetches a location ${location.toString()}")
                Log.d(TAG, "updateUserGPS is called and the location is ${location.toString()}")
                val latLng = LatLng(location.latitude, location.longitude)

                return latLng
            } else{
                Toast.makeText(requireContext(), "Location not available. Is this a new phone??? Or you reboot it just now?? Try again later.", Toast.LENGTH_LONG).show()
                Log.d(TAG, "updateUserGPS is called and fusedLocationProviderClient failed to get a location")
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


