package com.snowman.neverlate.ui.events

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IEvent

class EventsViewModel : ViewModel() {
    private val TAG = "Event Fragment"
    private val firebaseManager = FirebaseManager.getInstance()

    private var _events = MutableLiveData<List<IEvent>>()
    var events: MutableLiveData<List<IEvent>> = _events
    private val _navigateToAnotherPage = MutableLiveData<Boolean>()
    val navigateToAnotherPage: LiveData<Boolean> = _navigateToAnotherPage

    fun onButtonClicked() {
        _navigateToAnotherPage.value = true
    }

    fun fetchEventsData() {
        firebaseManager.fetchEventsDataForCurrentUser(true, "All") { eventsList, exception ->
            if (exception != null) {
                Log.i(TAG, "unable to fetch events $exception")
            } else {
                Log.d(TAG, "successfully fetched events! length: ${eventsList?.size}")
                _events.value = eventsList.orEmpty()
            }
        }
    }
}