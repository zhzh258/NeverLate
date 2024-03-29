package com.snowman.neverlate.model.types

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _userData = MutableLiveData<IUser?>()
    val userData: LiveData<IUser?> = _userData

    fun setUserData(user: IUser?) {
        _userData.value = user
    }
}