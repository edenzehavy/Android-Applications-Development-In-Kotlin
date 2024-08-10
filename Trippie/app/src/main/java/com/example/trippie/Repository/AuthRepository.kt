package com.example.trippie.Repository
import com.example.trippie.Model.User
import com.example.trippie.Utils.Resource

/**
 * Interface defining the necessary authentication operations for an authentication service.
 */
interface AuthRepository {

    //Retrieves the current authenticated user's unique identifier.
    suspend fun currentUser() : Resource<String?>

    //Attempts to log in a user using their email and password.
    suspend fun login(userEmail:String, userPassword:String) : Resource<User>

    //Creates a new user account with the provided email and password.
    suspend fun createUser(userEmail:String,
                           userPassword:String) : Resource<User>
    //Logs out the current authenticated user.
    fun logout()
}