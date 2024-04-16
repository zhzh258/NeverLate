package com.snowman.neverlate.model.types

interface IEvent {
    val id: String // UUID
    val name: String
    val date: String // '3/26/2023'; consider using a more suitable date type
    val time: String // '12:00 PM'
    val location: String
    val photoURL: String // For storing profile picture, you might need a custom type or a String URL
    val active: Boolean // History event or ongoing event?
    val address: String
    val members: List<String> // List of members userid
    val description: String
    val duration: Long // Duration in milliseconds
    val category: String // Category that is one of Dining, Study, Meeting. Useful for filtering

    fun updateEventViewModel(viewModel: EventViewModel)
}