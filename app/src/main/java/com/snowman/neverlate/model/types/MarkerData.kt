package com.snowman.neverlate.model.types

import com.google.android.gms.maps.model.MarkerOptions

data class MarkerData(
    val event: IEvent,
    val markerOptions: MarkerOptions,
)
