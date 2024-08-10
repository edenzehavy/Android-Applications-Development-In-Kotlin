package com.example.trippie.Fragments

import androidx.lifecycle.*
import com.example.trippie.Model.User
import com.example.trippie.R
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Utils.Resource
import kotlinx.coroutines.launch
import java.util.Locale


/**
 * ViewModel for managing sign-in operations and current user status within the application.
 */
class SignInViewModel(private val authRep: AuthRepository) : ViewModel() {

    private val _userSignInStatus = MutableLiveData<Resource<User>>()
    val userSignInStatus: LiveData<Resource<User>> = _userSignInStatus

    private val _currentUser = MutableLiveData<Resource<String?>>()
    val currentUser: LiveData<Resource<String?>> = _currentUser

    init {
        viewModelScope.launch {
            _currentUser.postValue(Resource.Loading())
            val currentUserResult = authRep.currentUser()
            _currentUser.postValue(currentUserResult)
        }
    }

    // Attempts to sign in a user with the provided email and password.
    fun signInUser(userEmail: String, userPassword: String) {
        var toast = "Please enter Email & Password"
        val locale = Locale.getDefault()
        if(locale.language == "iw"){
            toast = "אנא מלא/י אימייל וסיסמא"
        }
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            _userSignInStatus.postValue(Resource.Error(toast))
        } else {
            _userSignInStatus.postValue(Resource.Loading())
            viewModelScope.launch {
                val loginResult = authRep.login(userEmail, userPassword)
                _userSignInStatus.postValue(loginResult)
            }
        }
    }

    /**
     * Factory class for creating instances of SignInViewModel.
     */
    class SignInViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignInViewModel(repository) as T
        }
    }
}