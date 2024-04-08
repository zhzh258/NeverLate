package com.snowman.neverlate.ui.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

        mapViewModel.cameraPos.observe(viewLifecycleOwner) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(it))
        }

        mapViewModel.markers.observe(viewLifecycleOwner) {
            map.clear()
            it.forEach {
                map.addMarker(it)
            }
        }
        map.isTrafficEnabled = true

    }
}