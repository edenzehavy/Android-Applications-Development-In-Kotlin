package com.example.trippie.Repository

import androidx.lifecycle.MutableLiveData
import com.example.trippie.Expenses.Expense
import com.example.trippie.Utils.Resource

/**
 * Interface defining the necessary operations for managing expense data.
 */
interface ExpensesRepository {

    //Adds an expense to the data source.
    suspend fun addExpense(
        expenseID: String,
        userID: String?,
        country: String,
        city: String,
        date: String,
        description: String,
        price: Double,
        currency: String
    ): Resource<Void>

    //Deletes an existing expense based on its unique identifier.
    suspend fun deleteExpense(expenseID: String): Resource<Void>

    //Retrieves a single expense by its unique id.
    suspend fun getExpense(expenseID: String): Resource<Expense>

    // Retrieves all expenses associated with a specific user.
    suspend fun getExpenses(userID: String): Resource<List<Expense>>

    //Updates an existing expense with new details.
    suspend fun updateExpense(
        expenseID: String,
        userID: String?,
        country: String,
        city: String,
        date: String,
        description: String,
        price: Double,
        currency: String
    ): Resource<Void>

    //Provides real-time updates for all expenses related to a specific user.
    fun getExpensesLiveData(data: MutableLiveData<Resource<List<Expense>>>, userID: String?)
}
