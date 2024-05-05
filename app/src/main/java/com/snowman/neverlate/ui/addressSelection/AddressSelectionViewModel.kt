package com.snowman.neverlate.ui.addressSelection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IEvent

class AddressSelectionViewModel : ViewModel() {
    private val TAG = "AddressSelectionViewModel"
//    val cameraPos =  MutableLiveData<LatLng?>(null)
//    val userLocation = MutableLiveData<LatLng?>(null)
    val selection = MutableLiveData<LatLng?>(null)
    val address = MutableLiveData<String?>(null)
    val id = MutableLiveData<String?>(null)
    val name = MutableLiveData<String?>(null)

}