package com.snowman.neverlate.ui.history

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IEvent

class HistoryViewModel : ViewModel() {
    private val TAG = "History Fragment"
    private val firebaseManager = FirebaseManager.getInstance()

    private var _events = MutableLiveData<List<IEvent>>()
    var events: MutableLiveData<List<IEvent>> = _events

    fun fetchEventsData() {
        firebaseManager.fetchEventsDataForCurrentUser(false, null) { eventsList, exception ->
            if (exception != null) {
                Log.i(TAG, "unable to fetch events $exception")
            } else {
                Log.d(TAG, "successfully fetched events! length: ${eventsList?.size}")
                val eventsList1 = eventsList.orEmpty().sortedBy { event -> event.date }
                _events.value = eventsList1
            }
        }
    }
}