package com.example.trippie.Fragments

import android.util.Patterns
import androidx.lifecycle.*
import com.example.trippie.Model.User
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Utils.Resource
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel for managing user registration in the application.
 */
class SignUpViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _userRegistrationStatus = MutableLiveData<Resource<User>>()
    val userRegistrationStatus: LiveData<Resource<User>> = _userRegistrationStatus


    //Attempts to create a new user with the provided email and password.
    fun createUser(userEmail: String, userPass: String) {
        val locale = Locale.getDefault()
        val emptyStringsMessage = if (locale.language == "iw") "שדות ריקים" else "Empty Strings"
        val invalidEmailMessage = if (locale.language == "iw") "לא דוא\"ל חוקי" else "Not a valid email"

        val error = when {
            userEmail.isEmpty() || userPass.isEmpty() -> emptyStringsMessage
            !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches() -> invalidEmailMessage
            else -> null
        }

        error?.let {
            _userRegistrationStatus.postValue(Resource.Error(it))
            return
        }

        _userRegistrationStatus.value = Resource.Loading()
        viewModelScope.launch {
            val registrationResult = repository.createUser(userEmail, userPass)
            _userRegistrationStatus.postValue(registrationResult)
        }
    }


    /**
     * Factory class to instantiate SignUpViewModel with necessary dependencies.
     */
    class SignUpViewModelFactory(private val repo: AuthRepository) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignUpViewModel(repo) as T
        }
    }
}