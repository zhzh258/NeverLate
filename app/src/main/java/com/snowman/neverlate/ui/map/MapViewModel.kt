package com.snowman.neverlate.ui.map

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.types.MarkerData

// Consider changing this viewModel into a shared one.(Or create another that is shared)
// So that whenever the user click on an event or ..., the map's state is changed accordingly.
class MapViewModel: ViewModel() {
    private val firebaseManager = FirebaseManager.getInstance()
    val markerData: MutableLiveData<List<MarkerData>> = MutableLiveData(listOf())
    private lateinit var marker2event: HashMap<Marker, IEvent>
    private val TAG = "Map Fragment"
//    private val dummyData = listOf<MarkerOptions>(
//        MarkerOptions()
//            .position(LatLng(42.350498, -71.105400))
//            .title("Boston University")
//
//                ,
//        MarkerOptions()
//            .position(LatLng(42.354044,-71.069957))
//            .title("Boston Public Garden")
//    )

    val cameraPos = MutableLiveData<CameraPosition>().apply {
        value = CameraPosition.fromLatLngZoom(LatLng(42.361145, -71.057083), 14F)
    }

    init {
        firebaseManager.fetchEventsDataForCurrentUser { iEventList, exception ->
            if (exception != null) {
                Log.i(TAG, "unable to fetch events $exception")
                this.markerData.value = listOf()
            } else {
                this.markerData.value = iEventList.orEmpty().map { event ->
                    val markerOption = MarkerOptions()
                        .position(LatLng(event.location.latitude, event.location.longitude))
                        .title(event.name)

                    MarkerData(event, markerOption)
                }
            }
        }
    }
}