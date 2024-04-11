package com.snowman.neverlate.ui.friends

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.User
import java.util.UUID

class FriendsListViewModel : ViewModel() {
    private val TAG = "friends list vie wmodel"
    private val firebaseManager = FirebaseManager.getInstance()

    private val _friends = MutableLiveData<List<IUser>>()
    val friends: LiveData<List<IUser>> = _friends

    fun fetchFriendsData() {
        firebaseManager.fetchFriendsDataForCurrentUser { friendsList, exception ->
            if (exception != null) {
                Log.i(TAG, "unable to fetch friends $exception")
            } else {
                _friends.value = friendsList.orEmpty()
            }
        }

    }
}