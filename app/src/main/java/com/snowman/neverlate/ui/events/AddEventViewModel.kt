package com.snowman.neverlate.ui.events

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.types.Event
import com.snowman.neverlate.model.types.IUser
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class AddEventViewModel: ViewModel() {
    var event: Event = Event()
    val selectedDate = MutableLiveData(LocalDate.now())
    val selectedTime = MutableLiveData(LocalTime.now())
    val attendees = MutableLiveData(mutableListOf<IUser>())
    val profileUri = MutableLiveData<Uri>(null)
    init {
        event.active = true
        event.id = UUID.randomUUID().toString();
        event.name = ""
        event.description = ""
        event.category = ""
    }
}