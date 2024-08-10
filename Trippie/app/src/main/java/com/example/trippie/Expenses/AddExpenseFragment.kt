package com.example.trippie.Expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Repository.FirebaseImpl.ExpensesRepositoryFirebase
import com.example.trippie.Utils.Resource
import com.example.trippie.databinding.FragmentAddExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for adding a new expense entry.
 */
class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var date: Calendar

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModel.ExpensesViewModelFactory(AuthRepositoryFirebase(), ExpensesRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        date = Calendar.getInstance()

        setupSpinner()

        binding.dateButton.setOnClickListener { showDatePicker(date) }

        binding.finishBtn.setOnClickListener {
            val selectedCurrency = binding.currencySpinner.selectedItem.toString()
            viewModel.addExpense(
                country = binding.countryEt.text.toString(),
                city = binding.cityEt.text.toString(),
                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date.time),
                description = binding.descriptionEt.text.toString(),
                price = binding.priceEt.text.toString().toDouble(),
                currency = selectedCurrency
            ).invokeOnCompletion {
                findNavController().navigateUp()
            }
        }

        addTextWatchers()
        addSpinnerListener()
        validateForm()

        observeAddExpenseStatus()
    }

    // Initialize the currency spinner with currency options.
    private fun setupSpinner() {
        val currencies = arrayOf("₪", "€", "$")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencySpinner.adapter = adapter
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

    // Adds text change listeners to form fields to enable or disable the finish button based on input validity.
    private fun addTextWatchers() {
        val textWatcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
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


    // Adds a listener to the currency spinner to validate the form whenever a selection is made.
    private fun addSpinnerListener() {
        binding.currencySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                validateForm()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        }
    }

    // Validates the form fields and enables the finish button if all fields are properly filled.
    private fun validateForm() {
        val isCountryFilled = binding.countryEt.text.toString().isNotEmpty()
        val isCityFilled = binding.cityEt.text.toString().isNotEmpty()
        val isDescriptionFilled = binding.descriptionEt.text.toString().isNotEmpty()
        var isPriceFilled = binding.priceEt.text.toString().isNotEmpty()
        isPriceFilled = isPriceFilled && (binding.priceEt.text.toString() != ".")

        binding.finishBtn.isEnabled = isCountryFilled && isCityFilled && isDescriptionFilled && isPriceFilled
    }

    // Processes the expense submission by capturing form data and invoking the ViewModel.
    private fun observeAddExpenseStatus() {
        viewModel.addExpenseStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.finishBtn.isEnabled = false
                }
                is Resource.Success -> {
                    val toast = getString(R.string.expense_added_toast)
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
