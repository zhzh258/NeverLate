package com.snowman.neverlate.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.PolyUtil
import com.snowman.neverlate.databinding.FragmentMapBinding
import com.snowman.neverlate.model.retrofit.DirectionsResponse
import com.snowman.neverlate.model.retrofit.GoogleMapsDirectionsService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val TAG = "MAP_DEBUG"

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var _mapViewModel: MapViewModel? = null
    private val mapViewModel get() = _mapViewModel!!
    private var _bottomSheetBehavior:  BottomSheetBehavior<FrameLayout>? = null
    private val bottomSheetBehavior get() = _bottomSheetBehavior!!

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        _bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.peekHeight = 300 // How much content to show when it's COLLAPSED

        val mapFragment = childFragmentManager
            .findFragmentById(com.snowman.neverlate.R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        Log.d("MY_DEBUG", "MapFragment is destroyed!")
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady() is called. Launching coroutine in lifecycleScope...")
        lifecycleScope.launch {
            map = googleMap
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            setMapConfig()
            bindLiveData()
            updateUserGPS() // suspend function
            if(mapViewModel.userLocation.value != null)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(mapViewModel.userLocation.value!!, 12f))
            setUpBottomSheet()
        }
    }

    private fun setMapConfig() {
        map.isTrafficEnabled = true
    }

    private fun bindLiveData() {
        mapViewModel.cameraPos.observe(viewLifecycleOwner) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
        }

        mapViewModel.markers.observe(viewLifecycleOwner) {
            map.clear()
            it.forEach {
                map.addMarker(it)
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
                    updateUserGPS()
                }
            } else {
                // Permission was denied. Handle the situation by showing a message to the user or taking appropriate action.
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setUpBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        map.setOnMarkerClickListener {marker: Marker -> lifecycleScope.launch {
//            updateUserGPS() // suspend function
            marker.showInfoWindow()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            Log.d(TAG, mapViewModel.userLocation.value.toString())
            // Start all fetches in parallel
            val driving = async { fetchDirectionsResponse("driving", marker) }
            val walking = async { fetchDirectionsResponse("walking", marker) }
            val bicycling = async { fetchDirectionsResponse("bicycling", marker) }
            val transit = async { fetchDirectionsResponse("transit", marker) }
            // await all
            val responses = awaitAll(driving, walking, bicycling, transit)
            val (drivingResponse, walkingResponse, bicyclingResponse, transitResponse) = responses
            binding.driveButton.apply {
                this.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    val polylineOptions = PolylineOptions()
                    val points = PolyUtil.decode(drivingResponse?.routes?.get(0)?.overview_polyline?.points ?: "")
                    polylineOptions.addAll(points)
                    map.addPolyline(polylineOptions)
                }
                val duration = drivingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
                val distance = drivingResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
                this.text = "Drive: " + duration + " " + distance
            }
            binding.walkButton.apply {
                this.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    val polylineOptions = PolylineOptions()
                    val points = PolyUtil.decode(walkingResponse?.routes?.get(0)?.overview_polyline?.points ?: "")
                    polylineOptions.addAll(points)
                    map.addPolyline(polylineOptions)
                }
                val duration = walkingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
                val distance = walkingResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
                this.text = "Walk: " + duration + " " + distance
            }
            binding.bikeButton.apply {
                this.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    val polylineOptions = PolylineOptions()
                    val points = PolyUtil.decode(bicyclingResponse?.routes?.get(0)?.overview_polyline?.points ?: "")
                    polylineOptions.addAll(points)
                    map.addPolyline(polylineOptions)
                }
                val duration = bicyclingResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
                val distance = bicyclingResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
                this.text = "Bike: " + duration + " " + distance
            }
            binding.transitButton.apply {
                this.setOnClickListener {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    val polylineOptions = PolylineOptions()
                    val points = PolyUtil.decode(transitResponse?.routes?.get(0)?.overview_polyline?.points ?: "")
                    polylineOptions.addAll(points)
                    map.addPolyline(polylineOptions)
                }
                val duration = transitResponse?.routes?.get(0)?.legs?.get(0)?.duration?.text
                val distance = transitResponse?.routes?.get(0)?.legs?.get(0)?.distance?.text
                this.text = "Transit: " + duration + " " + distance
            }
        }
            true
        }

        map.setOnMapClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
    private suspend fun fetchDirectionsResponse(mode: String, marker: Marker): DirectionsResponse? { // show a line from current location to

        Log.d(TAG, "Now fetching data with origin = ${mapViewModel.userLocation.value.toString()}")
        val origin = mapViewModel.userLocation.value ?: return null
        val destination = marker.position

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GoogleMapsDirectionsService::class.java)

        try {
            val apiKey = getMetaData("com.google.android.geo.API_KEY")
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

    private suspend fun updateUserGPS() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { // permission is NOT granted
            Toast.makeText(requireContext(), "please enable location permissions in device settings", Toast.LENGTH_SHORT).show()
        } else { // permission is granted, show the location
            map.isMyLocationEnabled = true
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            val location = fusedLocationProviderClient.lastLocation.await()
            Log.d(TAG, "updateUserGPS is called and the location is ${location.toString()}")
            if (location != null) {
                Log.d(TAG, "fusedLocationProviderClient successfully fetches a location ${location.toString()}")
                mapViewModel.userLocation.value = LatLng(location.latitude, location.longitude)
            } else { // idk why but the location can be null sometimes
                Toast.makeText(requireContext(), "Location not available. Is this a new phone??? Or you reboot it just now?? Try again later.", Toast.LENGTH_LONG).show()
                Log.d(TAG, "No location available at this time.")
            }
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
}


