package com.example.trippie.Expenses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.trippie.R
import com.example.trippie.databinding.FragmentDetailedExpenseBinding

/**
 * Fragment for displaying detailed information about an expense.
 */
class DetailedExpenseFragment : Fragment() {

    private var _binding: FragmentDetailedExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var expense: Expense

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailedExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the expense object passed from the arguments
        expense = requireArguments().getParcelable("expense")!!

        // Bind the expense data to the UI elements
        bindExpenseData(expense)

        // Set click listener for the "Back" button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Binds the data of the expense object to various TextViews in the fragment.

    private fun bindExpenseData(expense: Expense) {
        binding.apply {
            // Set expense data to the views
            detailedExpenseDestination.text = expense.country + ", " + expense.city
            detailedExpenseDate.text = expense.date
            detailedExpenseDesc.text = expense.description
            detailedExpensePrice.text = expense.price.toString() + expense.currency
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
