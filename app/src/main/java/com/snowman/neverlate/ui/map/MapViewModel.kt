package com.snowman.neverlate.ui.map

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// Consider changing this viewModel into a shared one.(Or create another that is shared)
// So that whenever the user click on an event or ..., the map's state is changed accordingly.
class MapViewModel: ViewModel() {
    private val dummyData = listOf<MarkerOptions>(
        MarkerOptions()
            .position(LatLng(42.350498, -71.105400))
            .title("Boston University"),
        MarkerOptions()
            .position(LatLng(42.354044,-71.069957))
            .title("Boston Public Garden")
    )

    val cameraPos = MutableLiveData<CameraPosition>().apply {
        value = CameraPosition.fromLatLngZoom(LatLng(42.361145, -71.057083), 14F)
    }

    val markers = MutableLiveData<List<MarkerOptions>>().apply {
        value = dummyData
    }

    val userLocation = MutableLiveData<LatLng?>().apply {
        value = null
    }
}