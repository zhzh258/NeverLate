package com.snowman.neverlate.ui.profile

import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.types.User
import java.util.UUID

// TODO: Make this a shared view model. By fetching auth data from firebase. "me" should always be the current user using the App.
class ProfileViewModel: ViewModel() {
    lateinit var me: User
    init {
        me = User(
            id = UUID.randomUUID().toString(),
            phoneNumber = 1234567890,
            displayName = "ZhaoZhan Huang",
            profilePicture = "https://www.womansworld.com/wp-content/uploads/2024/08/cute-cats.jpg",
            status = "This app is a pain for me"
        )
    }
}