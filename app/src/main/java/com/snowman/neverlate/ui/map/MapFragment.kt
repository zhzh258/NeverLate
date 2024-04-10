package com.snowman.neverlate.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.snowman.neverlate.databinding.FragmentMapBinding
import com.snowman.neverlate.ui.map.MapViewModel

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var _mapViewModel: MapViewModel? = null
    private val mapViewModel get() = _mapViewModel!!

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)

        val mapFragment = childFragmentManager
            .findFragmentById(com.snowman.neverlate.R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("MY_DEBUG", "MapFragment is destroyed!")
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Toast.makeText(requireContext(), "onMapReady() is called", Toast.LENGTH_SHORT).show()
        map = googleMap

        setMapConfig()
        bindLiveData()
        setUpUserLocation()
        setUpBottomCard()
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
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                setUpUserLocation()
            } else {
                // Permission was denied. Handle the situation by showing a message to the user or taking appropriate action.
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setUpUserLocation() {
        // Check if permission is granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { // permission is NOT granted
            // Request the permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        } else { // permission is granted, show the location
            map.isMyLocationEnabled = true

            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                // Move the camera to the user's current location once it's obtained
                location?.let {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 12f))
                }
                Toast.makeText(requireContext(), location?.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpBottomCard() {
        map.setOnMarkerClickListener {marker: Marker ->
            marker.showInfoWindow()
            showButtonCard(marker);
            true
        }
    }

    private fun showButtonCard(marker: Marker){
        binding.bottomCardContainer.visibility = View.VISIBLE
        binding.debugLatitude.text = marker.position.latitude.toString()
        binding.debugLongitude.text = marker.position.longitude.toString()
        binding.debugTitle.text = marker.title.toString()
    }
}