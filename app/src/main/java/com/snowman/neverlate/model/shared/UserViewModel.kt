package com.snowman.neverlate.model.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IUser

// This is the shared view model storing the data (IUser) of the current user
class UserViewModel : ViewModel() {
    private val _userData = MutableLiveData<IUser?>()
    val userData: LiveData<IUser?> = _userData

    fun setUserData(user: IUser?) {
        _userData.value = user
    }
}