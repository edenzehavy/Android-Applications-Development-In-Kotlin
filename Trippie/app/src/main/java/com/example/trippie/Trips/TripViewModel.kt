package com.example.trippie.Trips
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Repository.TripsRepository
import com.example.trippie.Utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for managing trip data within the application.
 */
class TripViewModel (private val authRep: AuthRepository, private val tripsRep: TripsRepository) : ViewModel() {

    //With LiveData
    private val _tripsStatus: MutableLiveData<Resource<List<Trip>>> = MutableLiveData()
    val tripStatus: LiveData<Resource<List<Trip>>> = _tripsStatus

    private val _addTripStatus = MutableLiveData<Resource<Void>>()
    val addTripStatus: LiveData<Resource<Void>> = _addTripStatus

    private val _deleteTripStatus = MutableLiveData<Resource<Void>>()
    val deleteTripStatus: LiveData<Resource<Void>> = _deleteTripStatus

    private val _updateTripStatus = MutableLiveData<Resource<Void>>()
    val updateTripStatus: LiveData<Resource<Void>> = _updateTripStatus

    //Fetches trips from the repository and posts their status to the LiveData.
    fun fetchTrips() {
        viewModelScope.launch {
            val userResult = authRep.currentUser()
            if (userResult is Resource.Success) {
                val userID = userResult.data
                tripsRep.getTripsLiveData(_tripsStatus, userID)
            } else if (userResult is Resource.Error) {
                _tripsStatus.postValue(Resource.Error(userResult.message!!, null))
            }
        }
    }

    init {
        fetchTrips()
    }

    //Signs out the current user by calling the logout method on the auth repository.
    fun signOut() {
        authRep.logout()
    }

    //Adds a trip to the repository and updates the LiveData based on the result.
    fun addTrip(
        country: String,
        city: String,
        startDate: String,
        endDate: String,
        description: String,
        imageUrl: String?,
        activities: String,
        hotelDetails: String
    ): Job {
        return viewModelScope.launch {
            _addTripStatus.postValue(Resource.Loading())
            _addTripStatus.postValue(
                tripsRep.addTrip(
                    UUID.randomUUID().toString(),
                    authRep.currentUser().data,
                    country,
                    city,
                    startDate,
                    endDate,
                    description,
                    imageUrl,
                    activities,
                    hotelDetails
                )
            )
        }
    }

    //Deletes a trip by its ID. Ensures the trip ID is not empty before attempting to delete.
    fun deleteTrip(tripID: String) {
        viewModelScope.launch {
            if (tripID.isEmpty())
                _deleteTripStatus.postValue(Resource.Error("Empty trip id"))
            else {
                _deleteTripStatus.postValue(Resource.Loading())
                _deleteTripStatus.postValue(tripsRep.deleteTrip(tripID))
            }
        }
    }



    //Updates an existing trip with new details and updates the LiveData based on the result.
    fun updateTrip(
        tripID: String,
        country: String,
        city: String,
        startDate: String,
        endDate: String,
        description: String,
        imageUrl: String?,
        activities: String,
        hotelDetails: String
    ): Job {
        return viewModelScope.launch {
            _updateTripStatus.postValue(Resource.Loading())
            _updateTripStatus.postValue(tripsRep.updateTrip(
                tripID,
                authRep.currentUser().data,
                country,
                city,
                startDate,
                endDate,
                description,
                imageUrl,
                activities,
                hotelDetails
            ))
        }
    }

    /**
     * Provides a factory to create instances of TripViewModel with specific dependencies.
     */
    class TripsViewModelFactory(
        private val authRepo: AuthRepository,
        private val taskRep: TripsRepository
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripViewModel(authRepo, taskRep) as T
        }
    }
}