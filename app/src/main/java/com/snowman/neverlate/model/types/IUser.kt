package com.snowman.neverlate.model.types

import com.snowman.neverlate.model.shared.UserViewModel

interface IUser {
    val userId: String // UUID
    val phoneNumber: String
    val firstName: String
    val lastName: String
    val displayName: String
    val photoURL: String // For storing profile picture, you might need a custom type or a String URL
    val email: String
    val passwordHash: String // Encrypted password
    val friends: List<String> // List of UUIDs of other users
    val totalLateTime: Long // For analysis
    val totalEarlyTime: Long // For analysis
    val status: String
    val friendRequests: List<String>
    // Assuming "About Me" and "Setting Config" are strings for simplicity; adjust as necessary
//    val aboutMe: String
    // ** val settingConfig: String
    // ** val profile: String // Other information related to personal profile page; adjust type as necessary

    fun updateUserViewModel(viewModel: UserViewModel)
}