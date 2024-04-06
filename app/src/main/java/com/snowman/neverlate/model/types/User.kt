package com.snowman.neverlate.model.types

data class User(
    override val userId: String = "",
    override val phoneNumber: Long = 0L,
    override val firstName: String = "",
    override val lastName: String = "",
    override val displayName: String = "",
    override val photoURL: String = "",
    override val email: String = "",
    override val passwordHash: String = "",
    override val friends: List<String> = emptyList(),
    override val totalLateTime: Long = 0L,
    override val totalEarlyTime: Long = 0L,
    override val status: String = ""
) : IUser {

    override fun updateUserViewModel(viewModel: UserViewModel) {
        viewModel.setUserData(this)
    }
}
