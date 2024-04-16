package com.snowman.neverlate.model.types

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EventViewModel : ViewModel() {
    private val _eventData = MutableLiveData<IEvent?>()
    val eventData: LiveData<IEvent?> = _eventData

    fun setEventData(event: IEvent?) {
        _eventData.value = event
    }
}