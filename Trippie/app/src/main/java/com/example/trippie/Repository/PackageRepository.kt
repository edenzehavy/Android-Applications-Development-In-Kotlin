package com.example.trippie.Repository

import androidx.lifecycle.MutableLiveData
import com.example.trippie.Package.PackageItem
import com.example.trippie.Utils.Resource

/**
 * Interface defining the necessary operations for a repository managing package items.
 */
interface PackageRepository {

    //Adds a package item to a persistent store.
    suspend fun addPackageItem(
        packageItemID: String,
        userID: String?,
        description: String,
        count: Int,
        isChecked: Int
    ): Resource<Void>

    //Deletes a package item from a persistent store using its unique identifier.
    suspend fun deletePackageItem(packageItemID: String): Resource<Void>

    //Retrieves a single package item by its identifier.
    suspend fun getPackageItem(packageItemID: String): Resource<PackageItem>

    //Retrieves all package items associated with a specific user.
    suspend fun getPackageItems(userID: String): Resource<List<PackageItem>>

    //Provides a live data stream of package items related to a specific user.
    fun getPackageItemsLiveData(data: MutableLiveData<Resource<List<PackageItem>>>, userID: String?)
}
