package com.example.trippie.Repository.FirebaseImpl

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.trippie.Repository.TripsRepository
import com.example.trippie.Trips.Trip
import com.example.trippie.Utils.Resource
import com.example.trippie.Utils.safeCall
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale


/**
 * Implementation of the TripsRepository interface using Firebase Firestore.
 */
class TripsRepositoryFirebase : TripsRepository {

    private val tripRef = FirebaseFirestore.getInstance().collection("trips")
    private val locale = Locale.getDefault()

    //Adds a trip to Firestore with the provided details.
    override suspend fun addTrip(tripID: String, userID: String?, country: String, city: String, startDate: String, endDate: String, description: String, imageUrl: String?, activities: String, hotelDetails: String) = withContext(Dispatchers.IO){
        safeCall {
            val trip = Trip(tripID, userID?: "", country, city, startDate, endDate, description, imageUrl, activities, hotelDetails)
            val addition = tripRef.document(tripID).set(trip).await()
            Resource.Success(addition)
        }
    }

    // Deletes a specific trip from Firestore using its ID.
    override suspend fun deleteTrip(tripID: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result  = tripRef.document(tripID).delete().await()
            Resource.Success(result)
        }
    }

    //Retrieves a single trip from Firestore by its ID.
    override suspend fun getTrip(tripID: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = tripRef.document(tripID).get().await()
            val trip = result.toObject(Trip::class.java)
            Resource.Success(trip!!)
        }
    }

    //Retrieves all trips associated with a specific user from Firestore.
    override suspend fun getTrips(userID: String)= withContext(Dispatchers.IO) {
        safeCall {
            val result  = tripRef.whereEqualTo("userID", userID).get().await()
            val trips = result.toObjects(Trip::class.java)
            Resource.Success(trips)
        }
    }

    //Provides real-time updates of trips data for a specific user using Firestore snapshot listeners.
    override fun getTripsLiveData(data: MutableLiveData<Resource<List<Trip>>>, userID: String?) {

        data.postValue(Resource.Loading())

        tripRef.whereEqualTo("userID", userID).addSnapshotListener {snapshot, e ->
            if(e != null) {
                data.postValue(Resource.Error(e.localizedMessage))
                return@addSnapshotListener
            }
            if(snapshot != null && !snapshot.isEmpty) {
                data.postValue(Resource.Success(snapshot.toObjects(Trip::class.java)))
            }
            else {
                var err = "No Data"
                if(locale.language == "iw"){
                    err = "אין מידע"
                }
                data.postValue(Resource.Error(err))
            }
        }
    }

    //Updates an existing trip document in Firestore.
    override suspend fun updateTrip(
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
    ) = withContext(Dispatchers.IO) {
        safeCall {
            val trip = Trip(tripID, userID ?: "", country, city, startDate, endDate, description, imageUrl, activities, hotelDetails)
            val updateResult = tripRef.document(tripID).set(trip).await()
            Resource.Success(updateResult)
        }
    }
}