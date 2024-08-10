package com.example.trippie.Repository.FirebaseImpl

import com.example.trippie.Model.User
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Utils.Resource
import com.example.trippie.Utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Implementation of the AuthRepository interface using Firebase Authentication and Firestore.
 */
class AuthRepositoryFirebase : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userRef = FirebaseFirestore.getInstance().collection("users")
    private val locale = Locale.getDefault()

    //Retrieves the currently authenticated user's UID if they are logged in.
    override suspend fun currentUser(): Resource<String?> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    Resource.Success(firebaseUser.uid)
                } else {
                    var err = "No authenticated user found"
                    if(locale.language == "iw"){
                        err = "משתמש לא נמצא"
                    }
                    Resource.Error(err)
                }
            }
        }
    }

    //Attempts to log in a user with the provided email and password.
    override suspend fun login(userEmail: String, userPassword: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val result = firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val user = userRef.document(firebaseUser.uid).get().await().toObject(User::class.java)
                    if (user != null) {
                        Resource.Success(user)
                    } else {
                        var err = "No authenticated user found in firestore"
                        if(locale.language == "iw"){
                            err = "משתמש לא נמצא במסד הנתונים"
                        }
                        Resource.Error(err)
                    }
                } else {
                    var err = "Authentication error"
                    if(locale.language == "iw"){
                        err = "שגיאת אימות"
                    }
                    Resource.Error(err)
                }
            }
        }
    }

    //Creates a new user account with the provided email and password.
    override suspend fun createUser(userEmail: String, userPassword: String): Resource<User> {
        return withContext(Dispatchers.IO) {
            safeCall {
                val registrationResult = firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).await()
                val userId = registrationResult.user?.uid!!
                val newUser = User(userEmail, userPassword)
                userRef.document(userId).set(newUser).await()
                Resource.Success(newUser)
            }
        }
    }

    //Logs out the currently authenticated user by calling Firebase Auth's signOut method.
    override fun logout() {
        firebaseAuth.signOut()
    }
}
