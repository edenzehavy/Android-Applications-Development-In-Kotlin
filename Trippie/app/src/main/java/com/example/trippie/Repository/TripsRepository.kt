package com.example.trippie.Repository

import androidx.lifecycle.MutableLiveData
import com.example.trippie.Trips.Trip
import com.example.trippie.Utils.Resource

/**
 * Interface defining the necessary operations for managing trip data within the application.
 */
interface TripsRepository {

    //Adds a new trip to the repository.
    suspend fun addTrip(
        tripID: String,
        userID: String?,
        country: String,
        city: String,
        startDate: String,
        endDate: String,
        description: String,
        imageUrl: String?,
        activities: String,
        hotelDetails: String
    ): Resource<Void>

    //Deletes a trip from the repository using its id.
    suspend fun deleteTrip(tripID: String): Resource<Void>

    //Retrieves a single trip by its id.
    suspend fun getTrip(tripID: String): Resource<Trip>

    //Retrieves all trips associated with a specified user.
    suspend fun getTrips(userID: String): Resource<List<Trip>>

    //Updates an existing trip with new details.
    suspend fun updateTrip(
        tripID: String,
        userID: String?,
        country: String,
        city: String,
        startDate: String,
        endDate: String,
        description: String,
        imageUrl: String?,
        activities: String,
        hotelDetails: String
    ): Resource<Void>

    //Provides a live data stream of trips related to a specific user.
    fun getTripsLiveData(data: MutableLiveData<Resource<List<Trip>>>, userID: String?)
}