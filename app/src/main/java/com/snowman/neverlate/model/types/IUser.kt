package com.snowman.neverlate.model.types

import com.snowman.neverlate.model.shared.SharedUserViewModel

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
    val status: String //status is aboutMe
    val address: String
    val personalSignature: String
    val rate: Int
    val friendRequests: List<String>
    fun updateUserViewModel(viewModel: SharedUserViewModel)
}