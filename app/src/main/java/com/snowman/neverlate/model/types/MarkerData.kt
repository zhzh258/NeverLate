package com.snowman.neverlate.model.types

import com.google.android.gms.maps.model.MarkerOptions

// This is a marker on the map
/**
 * @param event: The IEvent data
 * @param markerOptions: Config of googlg map marker
 */
data class MarkerData(
    val event: IEvent,
    val markerOptions: MarkerOptions,
)
