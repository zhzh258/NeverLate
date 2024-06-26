package com.snowman.neverlate.model.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.FirebaseManager
import com.snowman.neverlate.model.types.IUser

// This is the shared view model storing the friends list of the current user
class SharedFriendsViewModel : ViewModel() {
    private val TAG = "FriendViewModel"
    private val firebaseManager = FirebaseManager.getInstance()

    private val _friends = MutableLiveData<List<IUser>>()
    val friends: LiveData<List<IUser>> = _friends

    fun fetchFriendsData() {
        firebaseManager.fetchFriendsDataForCurrentUser { friendsList, exception ->
            if (exception != null || friendsList == null) {
                Log.i(TAG, "unable to fetch friends $exception")
            } else {
                val sortedList = friendsList.sortedBy { friend -> friend.displayName }
                _friends.value = sortedList
            }
        }
    }

    fun setUpFriendListener() {
        firebaseManager.friendListener {
            fetchFriendsData()
        }
    }
}