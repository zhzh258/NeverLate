package com.snowman.neverlate.ui.addressSelection

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.snowman.neverlate.R
import com.snowman.neverlate.databinding.ActivityAddressSelectionBinding
import com.snowman.neverlate.util.getMetaData
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AddressSelectionActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityAddressSelectionBinding
    private val addressSelectionViewModel: AddressSelectionViewModel by viewModels()
    var placesClient: PlacesClient? = null
    private val TAG = "Address Selection Activity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddressSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val apiKey = getMetaData("com.google.android.geo.API_KEY", applicationContext)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }
        // Create a new Places client instance.
        placesClient = Places.createClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete) as AutocompleteSupportFragment?
        autocompleteFragment!!.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                addressSelectionViewModel.id.value = place.id
                addressSelectionViewModel.name.value = place.name
                addressSelectionViewModel.address.value = place.address
                addressSelectionViewModel.selection.value = place.latLng

                place.address?.let { address ->
                    place.latLng?.let { latLng ->
                        map.clear()
                        map.addMarker(MarkerOptions().position(latLng).title(address))
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                    }
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(applicationContext, status.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        binding.confirmButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("addressString", addressSelectionViewModel.address.value)
            resultIntent.putExtra("latitude", addressSelectionViewModel.selection.value?.latitude)
            resultIntent.putExtra("longitude", addressSelectionViewModel.selection.value?.longitude)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        addressSelectionViewModel.name.observe(this) { id: String? ->
            binding.selection.isEnabled = id != null
            binding.selection.text = id ?: "No Place Selected"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        lifecycleScope.launch {
            getUserGPS()?.let { userLatLng ->
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
                Toast.makeText(applicationContext, "locating the user...", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private suspend fun getUserGPS(): LatLng? {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) { // permission is NOT granted
            Toast.makeText(applicationContext, "please enable location permissions in device settings", Toast.LENGTH_SHORT).show()
            return null;
        } else { // permission is granted, show the location
            map.isMyLocationEnabled = true
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            val priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
            val cancellationTokenSource = CancellationTokenSource()

            val location = fusedLocationProviderClient.getCurrentLocation(priority, cancellationTokenSource.token).await()
            if(location != null){
                Log.d(TAG, "fusedLocationProviderClient successfully fetches a location ${location.toString()}")
                Log.d(TAG, "updateUserGPS is called and the location is ${location.toString()}")
                val latLng = LatLng(location.latitude, location.longitude)

                return latLng
            } else{
                Toast.makeText(applicationContext, "Location not available. Is this a new phone??? Or you reboot it just now?? Try again later.", Toast.LENGTH_LONG).show()
                Log.d(TAG, "updateUserGPS is called and fusedLocationProviderClient failed to get a location")
                Log.d(TAG, "No location available at this time.")
                return null
            }
        }
    }
}