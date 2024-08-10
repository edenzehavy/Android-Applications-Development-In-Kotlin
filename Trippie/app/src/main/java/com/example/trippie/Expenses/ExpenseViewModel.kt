package com.example.trippie.Expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trippie.R
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Repository.ExpensesRepository
import com.example.trippie.Utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel to manage expenses related operations such as fetching, adding, updating, and deleting expenses.
 */
class ExpenseViewModel(private val authRep: AuthRepository, private val expensesRep: ExpensesRepository) : ViewModel() {

    // With LiveData
    private val _expensesStatus: MutableLiveData<Resource<List<Expense>>> = MutableLiveData()
    val expenseStatus: LiveData<Resource<List<Expense>>> = _expensesStatus

    private val _addExpenseStatus = MutableLiveData<Resource<Void>>()
    val addExpenseStatus: LiveData<Resource<Void>> = _addExpenseStatus

    private val _deleteExpenseStatus = MutableLiveData<Resource<Void>>()
    val deleteExpenseStatus: LiveData<Resource<Void>> = _deleteExpenseStatus

    private val _updateExpenseStatus = MutableLiveData<Resource<Void>>()
    val updateExpenseStatus: LiveData<Resource<Void>> = _updateExpenseStatus

    // Fetches expenses for the currently authenticated user.
    fun fetchExpenses() {
        viewModelScope.launch {
            val userResult = authRep.currentUser()
            if (userResult is Resource.Success) {
                val userID = userResult.data
                expensesRep.getExpensesLiveData(_expensesStatus, userID)
            } else if (userResult is Resource.Error) {
                _expensesStatus.postValue(Resource.Error(userResult.message!!, null))
            }
        }
    }

    // Initializes the ViewModel by fetching expenses.
    init {
        fetchExpenses()
    }

    // Logs out the current user.
    fun signOut() {
        authRep.logout()
    }

    // Adds a new expense with provided details.
    fun addExpense(
        country: String,
        city: String,
        date: String,
        description: String,
        price: Double,
        currency: String
    ) : Job {
            return viewModelScope.launch {
                _addExpenseStatus.postValue(Resource.Loading())
                _addExpenseStatus.postValue(
                    expensesRep.addExpense(
                        UUID.randomUUID().toString(),
                        authRep.currentUser().data,
                        country,
                        city,
                        date,
                        description,
                        price,
                        currency
                    )
                )
        }
    }

    // Deletes an expense by its ID.
    fun deleteExpense(expenseID: String) {
        viewModelScope.launch {
            _deleteExpenseStatus.postValue(Resource.Loading())
            _deleteExpenseStatus.postValue(expensesRep.deleteExpense(expenseID))
        }
    }

    // Updates an existing expense with new details.
    fun updateExpense(
        expenseID: String,
        country: String,
        city: String,
        date: String,
        description: String,
        price: Double,
        currency: String
    ) : Job {
        return viewModelScope.launch {
            _updateExpenseStatus.postValue(Resource.Loading())
            val result = expensesRep.updateExpense(
                expenseID,
                authRep.currentUser().data,
                country,
                city,
                date,
                description,
                price,
                currency
            )
            _updateExpenseStatus.postValue(result)
        }
    }
    // Factory class to create instances of ExpenseViewModel.
    class ExpensesViewModelFactory(
        private val authRepo: AuthRepository,
        private val expenseRep: ExpensesRepository
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExpenseViewModel(authRepo, expenseRep) as T
        }
    }
}
