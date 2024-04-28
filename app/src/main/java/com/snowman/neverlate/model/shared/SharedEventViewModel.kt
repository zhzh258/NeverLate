package com.snowman.neverlate.model.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.types.Event

class SharedEventViewModel : ViewModel() {
    private val _selectedEvent = MutableLiveData<Event?>()
    val selectedEvent: LiveData<Event?> get() = _selectedEvent

    fun setSelectedEvent(event: Any?) {
        _selectedEvent.value = event as Event?
    }
}