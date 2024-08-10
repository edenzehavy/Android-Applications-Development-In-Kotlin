package com.example.trippie.Repository.FirebaseImpl

import androidx.lifecycle.MutableLiveData
import com.example.trippie.Expenses.Expense
import com.example.trippie.Repository.ExpensesRepository
import com.example.trippie.Utils.Resource
import com.example.trippie.Utils.safeCall
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale


/**
 * Implementation of the ExpensesRepository interface using Firebase Firestore.
 */
class ExpensesRepositoryFirebase : ExpensesRepository {

    private val expenseRef = FirebaseFirestore.getInstance().collection("expenses")
    private var locale = Locale.getDefault()

    //Adds an expense document to Firestore.
    override suspend fun addExpense(
        expenseID: String,
        userID: String?,
        country: String,
        city: String,
        date: String,
        description: String,
        price: Double,
        currency: String
    ) = withContext(Dispatchers.IO) {
        safeCall {
            val expense = Expense(expenseID, userID, country, city, date, description, price, currency)
            val addition = expenseRef.document(expenseID).set(expense).await()
            Resource.Success(addition)
        }
    }

    //Deletes an expense document from Firestore based on its ID.
    override suspend fun deleteExpense(expenseID: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = expenseRef.document(expenseID).delete().await()
            Resource.Success(result)
        }
    }

    //Retrieves a single expense from Firestore by its ID.
    override suspend fun getExpense(expenseID: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = expenseRef.document(expenseID).get().await()
            val expense = result.toObject(Expense::class.java)
            Resource.Success(expense!!)
        }
    }

    //Retrieves all expenses of a specific user from Firestore.
    override suspend fun getExpenses(userID: String) = withContext(Dispatchers.IO) {
        safeCall {
            val result = expenseRef.whereEqualTo("userID", userID).get().await()
            val expenses = result.toObjects(Expense::class.java)
            Resource.Success(expenses)
        }
    }

    //Provides real-time updates of expenses data for a specific user using Firestore snapshot listeners.
    override fun getExpensesLiveData(data: MutableLiveData<Resource<List<Expense>>>, userID: String?) {
        data.postValue(Resource.Loading())
        expenseRef.whereEqualTo("userID", userID).addSnapshotListener { snapshot, e ->
            if (e != null) {
                data.postValue(Resource.Error(e.localizedMessage))
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                data.postValue(Resource.Success(snapshot.toObjects(Expense::class.java)))
            } else {
                var err = "No authenticated user found"
                if(locale.language == "iw"){
                    err = "משתמש לא נמצא"
                }
                data.postValue(Resource.Error(err))
            }
        }
    }


    //Updates an existing expense document in Firestore.
    override suspend fun updateExpense(
        expenseID: String,
        userID: String?,
        country: String,
        city: String,
        date: String,
        description: String,
        price: Double,
        currency: String
    ) = withContext(Dispatchers.IO) {
        safeCall {
            val expense = Expense(expenseID, userID ?: "", country, city, date, description, price, currency)
            val updateResult = expenseRef.document(expenseID).set(expense).await()
            Resource.Success(updateResult)
        }
    }
}
