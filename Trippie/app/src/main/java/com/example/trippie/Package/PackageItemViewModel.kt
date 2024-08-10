package com.example.trippie.Package

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Repository.PackageRepository
import com.example.trippie.Utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for managing package item operations such as fetching, adding, and deleting package items.
 */
class PackageItemViewModel(private val authRep: AuthRepository, private val packageRep: PackageRepository) : ViewModel() {

    private val _packageItemsStatus: MutableLiveData<Resource<List<PackageItem>>> = MutableLiveData()
    val packageItemsStatus: LiveData<Resource<List<PackageItem>>> = _packageItemsStatus

    private val _addPackageItemStatus = MutableLiveData<Resource<Void>>()
    val addPackageItemStatus: LiveData<Resource<Void>> = _addPackageItemStatus

    private val _deletePackageItemStatus = MutableLiveData<Resource<Void>>()
    val deletePackageItemStatus: LiveData<Resource<Void>> = _deletePackageItemStatus

    init {
        fetchPackageItems()
    }

    //Fetches package items for the current user by making a call through the package repository.
    fun fetchPackageItems() {
        viewModelScope.launch {
            val userResult = authRep.currentUser()
            if (userResult is Resource.Success) {
                val userID = userResult.data
                packageRep.getPackageItemsLiveData(_packageItemsStatus, userID)
            } else if (userResult is Resource.Error) {
                _packageItemsStatus.postValue(Resource.Error(userResult.message!!, null))
            }
        }
    }

    //Adds a package item to the database via the package repository.
    fun addPackageItem(description: String, count: Int, isChecked: Int) : Job {
        return viewModelScope.launch {
            _addPackageItemStatus.postValue(Resource.Loading())
            _addPackageItemStatus.postValue(
                packageRep.addPackageItem(
                    UUID.randomUUID().toString(),
                    authRep.currentUser().data,
                    description,
                    count,
                    isChecked
                )
            )
        }
    }

    //Deletes a specific package item identified by its ID.
    fun deletePackageItem(packageItemID: String) {
        viewModelScope.launch {
            _deletePackageItemStatus.postValue(Resource.Loading())
            _deletePackageItemStatus.postValue(packageRep.deletePackageItem(packageItemID))
        }
    }

    /**
     * Factory class for creating instances of PackageItemViewModel with necessary dependencies.
     */
    class PackageItemViewModelFactory(
        private val authRepo: AuthRepository,
        private val packageRep: PackageRepository
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PackageItemViewModel(authRepo, packageRep) as T
        }
    }

    //Logs out the current user by invoking the logout method of the authentication repository.
    fun signOut() {
        authRep.logout()
    }
}
