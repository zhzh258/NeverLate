package com.snowman.neverlate.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.User

class SettingsViewModel: ViewModel() {
    private val firebaseManager = FirebaseManager.getInstance()
    private val _me = MutableLiveData<User?>()
    val me: LiveData<User?> = _me
    var isEdit: Boolean = false
    init {
        loadUserData()
    }

    private fun loadUserData() {
        firebaseManager.loadUserData { user ->
            if (user != null) {
                _me.value = user as User?
            } else {
                // Handle the exception, e.g., log an error or show a message
                // For now, just set the LiveData value to null
                _me.value = null
            }
        }
    }

}