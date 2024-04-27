package com.snowman.neverlate.model.types

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Event(
    override val active: Boolean = true,
    override val address: String = "",
    override val category: String = "",
    override val date: Timestamp = Timestamp(Date()),
    override val description: String = "",
    override val duration: Long = 0L,
    override val id: String = "",
    override val members: List<String> = emptyList<String>(),
    override val name: String = "",
    // Chicago. If you see Chicago on map it means something is wrong
    override val location: GeoPoint = GeoPoint(41.8781, 87.6298),
    override val photoURL: String = "",
) : IEvent {

}