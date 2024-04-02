package com.snowman.neverlate.ui.friends

import androidx.lifecycle.ViewModel
import com.snowman.neverlate.model.types.IUser
import com.snowman.neverlate.model.types.User
import java.util.UUID

class FriendsListViewModel : ViewModel() {
    val friends = mutableListOf<IUser>()

    init {
        for (i in 0 until 5) {
            val friend1 = User(
                id = UUID.randomUUID().toString(),
                phoneNumber = 1234567890,
                displayName = "Mary Choe",
                profilePicture = "https://i.pinimg.com/originals/79/17/27/791727d03448c50f30f17e6da51cce65.png",
                status = "I love cats"
            )

            val friend2 = User(
                id = UUID.randomUUID().toString(),
                phoneNumber = 1234567890,
                displayName = "ChinKuan Lin",
                profilePicture = "https://www.rover.com/blog/wp-content/uploads/2019/04/cute-big-eyes-1024x682.jpg",
                status = "Sorry I was late last time"
            )

            val friend3 = User(
                id = UUID.randomUUID().toString(),
                phoneNumber = 1234567890,
                displayName = "ZhaoZhan Huang",
                profilePicture = "https://www.womansworld.com/wp-content/uploads/2024/08/cute-cats.jpg",
                status = "This app is a pain for me"
            )

            friends.addAll(listOf(friend1, friend2, friend3))
        }
    }
}