package com.snowman.neverlate.ui.events

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IEvent
import com.snowman.neverlate.model.types.Event

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

//    fun fetchEventsData() {
//        val event1: IEvent = Event("event_id1", "Group Project Meeting", "04/16/24", "9:30 PM", "George Sherman Union",
//            "", true, "775 Commonwealth Ave, Boston, MA 02215", listOf("7G8aYM2nCQYee2Ty2gOF6WfJfZi2", "kqrDkWba3dVMLEUnGFjX4gjqmmF3",
//                "rPzbvBIau8OLR9yenHZDyhVNcVX2", "uoFb8MuJOAeAaL2wZqE8PTmrS8M2"), "CS 501 group project meeting to plan sprint", 60, "Study")
//        val event2: IEvent = Event("event_id2", "CS 654 Group Project Meeting", "04/17/24", "9:00 PM", "George Sherman Union",
//            "", true, "775 Commonwealth Ave, Boston, MA 02215", listOf("7G8aYM2nCQYee2Ty2gOF6WfJfZi2", "kqrDkWba3dVMLEUnGFjX4gjqmmF3",
//                "rPzbvBIau8OLR9yenHZDyhVNcVX2", "uoFb8MuJOAeAaL2wZqE8PTmrS8M2"), "CS 654 group project meeting to share progress", 60, "Study")
//        _events.value = listOf(event1, event2)
//        events = _events
//    }


    fun fetchEventsData() {
        firebaseManager.fetchEventsDataForCurrentUser { eventsList, exception ->
            if (exception != null) {
                Log.i(TAG, "unable to fetch events $exception")
            } else {
                Log.d(TAG, "successfully fetched events! length: ${eventsList?.size}")
                _events.value = eventsList.orEmpty()
            }
        }
    }
}