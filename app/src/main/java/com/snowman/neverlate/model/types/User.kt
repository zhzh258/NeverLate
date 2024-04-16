package com.snowman.neverlate.model.types

data class User(
    override val userId: String = "",
    //override val phoneNumber: Long = 0L,
    override val phoneNumber: String = "",
    override val firstName: String = "",
    override val lastName: String = "",
    override val displayName: String = "",
    override val photoURL: String = "",
    override val email: String = "",
    override val passwordHash: String = "",
    override val friends: List<String> = emptyList(),
    override val totalLateTime: Long = 0L,
    override val totalEarlyTime: Long = 0L,
    override val status: String = "I'm sleepy",
    override val address: String = "in the middle of Charles River",
    override val personalSignature: String = "It's all about the Mindset",
    override val friendRequests: List<String> = emptyList()
) : IUser {

    override fun updateUserViewModel(viewModel: UserViewModel) {
        viewModel.setUserData(this)
    }
}
