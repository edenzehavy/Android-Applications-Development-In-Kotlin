package com.example.trippie.Expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.trippie.Repository.FirebaseImpl.ExpensesRepositoryFirebase
import com.example.trippie.databinding.FragmentEditExpenseBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.text.Editable
import android.text.TextWatcher
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase

/**
 * Fragment for editing an existing expense entry.
 */
class EditExpenseFragment : Fragment() {

    private var _binding: FragmentEditExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var date: Calendar
    private lateinit var expense: Expense
    private val expenseViewModel: ExpenseViewModel by activityViewModels {
        ExpenseViewModel.ExpensesViewModelFactory(AuthRepositoryFirebase(), ExpensesRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditExpenseBinding.inflate(inflater, container, false)
        date = Calendar.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the currency spinner
        val currencies = arrayOf("₪", "€", "$")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter

        binding.dateButton.setOnClickListener { showDatePicker(date) }

        // Retrieve the expense object passed from the arguments
        expense = requireArguments().getParcelable("expense")!!

        // Bind the expense data to the UI elements
        bindExpenseData(expense)

        // Set click listener for the "Save" button
        binding.saveBtn.setOnClickListener {
            // Retrieve the updated expense information from UI elements
            val updatedCountry = binding.countryEt.text.toString()
            val updatedCity = binding.cityEt.text.toString()
            val updatedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date.time)
            val updatedDescription = binding.descriptionEt.text.toString()
            val updatedPrice = binding.priceEt.text.toString().toDoubleOrNull() ?: 0.0 // Ensure it is a double
            val updatedCurrency = binding.currencySpinner.selectedItem.toString()

            // Call the view model method to update the expense
            expenseViewModel.updateExpense(
                expenseID = expense.expenseID,
                country = updatedCountry,
                city = updatedCity,
                date = updatedDate,
                description = updatedDescription,
                price = updatedPrice,
                currency = updatedCurrency
            ).invokeOnCompletion {
                findNavController().navigateUp()
            }
        }
        addTextWatchers()
        addSpinnerListener()
        validateForm()
    }

    // Binds the data of the expense object to various input fields in the form.
    private fun bindExpenseData(expense: Expense) {
        var position = 0
        when (expense.currency) {
            "₪" -> position = 0
            "€" -> position = 1
            "$" -> position = 2
        }
        binding.apply {
            // Set expense data to the views
            dateButton.text = expense.date
            countryEt.setText(expense.country)
            cityEt.setText(expense.city)
            descriptionEt.setText(expense.description)
            currencySpinner.setSelection(position)
            priceEt.setText(expense.price.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Displays a DatePicker dialog to select the date for the expense.
    private fun showDatePicker(calendar: Calendar) {
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            updateDateButton()
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            datePickerListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    // Updates the date button text to show the selected date.
    private fun updateDateButton() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.dateButton.text = dateFormat.format(date.time)
    }

    // Adds text change listeners to validate the form as the user types.
    private fun addTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.countryEt.addTextChangedListener(textWatcher)
        binding.cityEt.addTextChangedListener(textWatcher)
        binding.descriptionEt.addTextChangedListener(textWatcher)
        binding.priceEt.addTextChangedListener(textWatcher)
    }

    // Adds a listener to the currency spinner to re-validate the form on selection changes.
    private fun addSpinnerListener() {
        binding.currencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                validateForm()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Validates the form fields and enables the save button if all conditions are met.
    private fun validateForm() {
        val isCountryFilled = binding.countryEt.text.toString().isNotEmpty()
        val isCityFilled = binding.cityEt.text.toString().isNotEmpty()
        val isDescriptionFilled = binding.descriptionEt.text.toString().isNotEmpty()
        var isPriceFilled = binding.priceEt.text.toString().isNotEmpty()
        isPriceFilled = isPriceFilled && (binding.priceEt.text.toString() != ".")

        binding.saveBtn.isEnabled = isCountryFilled && isCityFilled && isDescriptionFilled && isPriceFilled
    }
}
