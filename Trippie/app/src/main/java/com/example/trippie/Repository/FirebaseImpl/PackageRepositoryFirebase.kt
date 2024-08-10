package com.example.trippie.Repository.FirebaseImpl

import androidx.lifecycle.MutableLiveData
import com.example.trippie.Package.PackageItem
import com.example.trippie.Repository.PackageRepository
import com.example.trippie.Utils.Resource
import com.example.trippie.Utils.safeCall
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Implementation of the PackageRepository interface using Firebase Firestore.
 */
class PackageRepositoryFirebase : PackageRepository {

    private val packageRef = FirebaseFirestore.getInstance().collection("packageItems")

    //Adds a package item to Firestore.
    override suspend fun addPackageItem(
        packageItemID: String,
        userID: String?,
        description: String,
        count: Int,
        isChecked: Int
    ) = withContext(Dispatchers.IO) {
        safeCall {
            val packageItem = PackageItem(packageItemID, userID ?: "", description, count, isChecked)
            val addition = packageRef.document(packageItemID).set(packageItem).await()
            Resource.Success(addition)
        }
    }

    //Deletes a specific package item from Firestore using its ID.
    override suspend fun deletePackageItem(packageItemID: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = packageRef.document(packageItemID).delete().await()
            Resource.Success(result)
        }
    }

    //Retrieves a single package item from Firestore by its ID.
    override suspend fun getPackageItem(packageItemID: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = packageRef.document(packageItemID).get().await()
            val packageItem = result.toObject(PackageItem::class.java)
            Resource.Success(packageItem!!)
        }
    }

    //Retrieves all package items associated with a specific user from Firestore.
    override suspend fun getPackageItems(userID: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = packageRef.whereEqualTo("userID", userID).get().await()
            val packageItems = result.toObjects(PackageItem::class.java)
            Resource.Success(packageItems)
        }
    }

    // Provides real-time updates of package items for a specific user using Firestore snapshot listeners.
    override fun getPackageItemsLiveData(data: MutableLiveData<Resource<List<PackageItem>>>, userID: String?) {
        data.postValue(Resource.Loading())

        packageRef.whereEqualTo("userID", userID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                data.postValue(Resource.Error(e.localizedMessage))
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                data.postValue(Resource.Success(snapshot.toObjects(PackageItem::class.java)))
            } else {
                data.postValue(Resource.Error("No Data"))
            }
        }
    }
}
