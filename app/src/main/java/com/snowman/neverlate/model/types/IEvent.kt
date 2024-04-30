package com.snowman.neverlate.model.types

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.snowman.neverlate.model.types.Event

data class MemberStatus(
    val id: String = "",
    val isArrived: Boolean = false,
    val status: String = "",
    val arriveTime: Long = 0L
)

interface IEvent {
    val active: Boolean // History event or ongoing event?
    val address: String // Think about the format/how to check if it is accurate
    val category: String // Category that is one of Dining, Study, Meeting. Useful for filtering
    val date: Timestamp // '3/26/2023'; consider using a more suitable date type
    val description: String
    val duration: Long // Duration in milliseconds
    val id: String // UUID
    val members: List<MemberStatus> // List of members userid
    val name: String
    val location: GeoPoint
    val photoURL: String // For storing profile picture, you might need a custom type or a String URL
}