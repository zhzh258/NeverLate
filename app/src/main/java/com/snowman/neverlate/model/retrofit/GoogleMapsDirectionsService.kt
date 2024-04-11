package com.snowman.neverlate.model.retrofit

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapsDirectionsService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String,
        @Query("key") apiKey: String,
    ): Response<DirectionsResponse>
}

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val legs: List<Leg>,
    val overview_polyline: Polyline
)

data class Leg(
    val distance: Distance,
    val duration: Duration
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
)

data class Polyline(
    val points: String
)