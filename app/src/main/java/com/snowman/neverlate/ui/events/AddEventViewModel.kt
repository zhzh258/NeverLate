package com.snowman.neverlate.ui.events

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.snowman.neverlate.model.types.Event
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.MemberStatus
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.UUID

class AddEventViewModel: ViewModel() {
    var event: Event = Event()
    val selectedDate = MutableLiveData<LocalDate>(LocalDate.now())
    val selectedTime = MutableLiveData<LocalTime>(LocalTime.now())
    val attendees = MutableLiveData<MutableList<IUser>>(mutableListOf<IUser>())
    val profileUri = MutableLiveData<Uri>(null)
    init {
        event.active = true
        event.id = UUID.randomUUID().toString();
        event.name = ""
        event.description = ""
        event.category = ""
    }
}