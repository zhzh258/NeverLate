package com.snowman.neverlate.model.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IEvent

// This is the shared view model storing the events of the current user
class EventViewModel: ViewModel() {
    private val _events = MutableLiveData<MutableList<IEvent>>()
    val events: LiveData<MutableList<IEvent>>
        get() = _events

    private val firebaseManager = FirebaseManager.getInstance()

    init {
        _events.value = mutableListOf() // TODO: Replace this with database fetchData()
    }
}