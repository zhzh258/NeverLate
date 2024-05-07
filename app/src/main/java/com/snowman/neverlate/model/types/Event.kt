package com.snowman.neverlate.model.types

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Event(
    override var active: Boolean = true,
    override var address: String = "",
    override var category: String = "",
    override var date: Timestamp = Timestamp(Date()),
    override var description: String = "",
    override var duration: Long = 0L,
    override var id: String = "",
    override var members: List<MemberStatus> = emptyList(),
    override var name: String = "",
    // Chicago. If you see Chicago on map it means something is wrong
    override var location: GeoPoint = GeoPoint(41.8781, 87.6298),
    override var photoURL: String = "",
) : IEvent