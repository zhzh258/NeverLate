package com.snowman.neverlate.model.types

interface IEvent {
    val id: String // UUID
    val name: String
    val date: String // '3/26/2023'; consider using a more suitable date type
    val time: String // '14:00'; consider using a more suitable time type
    val active: Boolean // History event or ongoing event?
    val address: String
    val members: List<String> // List of UUIDs of users
    val description: String
    val duration: Long // Duration in milliseconds
    val category: String // Category that is one of Dining, Study, Meeting. Useful for filtering
}