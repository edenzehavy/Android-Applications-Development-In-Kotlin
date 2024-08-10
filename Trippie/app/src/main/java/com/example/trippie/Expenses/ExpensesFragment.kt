package com.example.trippie.Expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Repository.FirebaseImpl.ExpensesRepositoryFirebase
import com.example.trippie.Utils.Resource
import com.example.trippie.databinding.FragmentExpensesBinding
import java.util.Locale

/**
 * Fragment for managing the display and interaction of expenses within the application.
 */
class ExpensesFragment : Fragment(), ExpenseAdapter.ExpenseListener {

    private val euroToUsdRate = 1.08
    private val usdToNisRate = 3.69
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by activityViewModels {
        ExpenseViewModel.ExpensesViewModelFactory(AuthRepositoryFirebase(), ExpensesRepositoryFirebase())
    }
    private lateinit var expenseAdapter: ExpenseAdapter
    private var isUserInitiatedDeletion = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        binding.addExpenseButton.setOnClickListener {
            findNavController().navigate(R.id.action_expensesFragment_to_addExpenseFragment)
        }

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDisconnectConfirmationDialog()
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView with StaggeredGridLayoutManager
        expenseAdapter = ExpenseAdapter(this)
        binding.expensesRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) // Staggered grid with 2 columns
            adapter = expenseAdapter
        }

        updateTotalExpensePrice()

        viewModel.expenseStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.addExpenseButton.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.addExpenseButton.isEnabled = true
                    expenseAdapter.updateExpenses(it.data!!)
                    updateTotalExpensePrice() // Recalculate total price when expenses change
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.addExpenseButton.isEnabled = true
                }
            }
        }

        viewModel.deleteExpenseStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    updateTotalExpensePrice()
                    if(isUserInitiatedDeletion){
                        val toast = getString(R.string.expense_deleted)
                        Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
                        isUserInitiatedDeletion = false
                    }

                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                }
            }
        }

        viewModel.updateExpenseStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                }
            }
        }

        // Set up ItemTouchHelper for swipe to delete
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // We don't want support for drag & drop
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                onItemSwiped(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.expensesRecyclerView)

    }

    override fun onItemClick(index: Int) {
        val selectedExpense = expenseAdapter.itemAt(index)
        val bundle = bundleOf("expense" to selectedExpense)
        findNavController().navigate(R.id.action_expensesFragment_to_detailedExpenseFragment, bundle)
    }

    override fun onItemLongClick(index: Int) {
        val selectedExpense = expenseAdapter.itemAt(index)
        val bundle = bundleOf("expense" to selectedExpense)
        findNavController().navigate(R.id.action_expensesFragment_to_editExpenseFragment, bundle)
    }

    override fun onItemSwiped(position: Int) {
        val expense = expenseAdapter.itemAt(position)
        val title = getString(R.string.delete_expense_title)
        val msg = getString(R.string.delete_expense_are_you_sure)
        val pos = getString(R.string.yes)
        val neg = getString(R.string.no)

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(pos) { dialog, which ->
                isUserInitiatedDeletion = true
                expenseAdapter.removeItem(position)
                viewModel.deleteExpense(expense.expenseID)
            }
            .setNegativeButton(neg) { dialog, which ->
                expenseAdapter.notifyItemChanged(position)  // Restore the item if the user cancels
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchExpenses()
    }


    // Show a dialog to confirm disconnect action on back button press.
    private fun showDisconnectConfirmationDialog() {
        val title = getString(R.string.disconnect_title)
        val msg = getString(R.string.disconnect_are_you_sure)
        val pos = getString(R.string.yes)
        val neg = getString(R.string.no)
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(pos) { dialog, which ->
                viewModel.signOut()
                findNavController().navigate(R.id.action_expensesFragment_to_signInFragment)
            }
            .setNegativeButton(neg) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    // Calculate and update the total expenses in different currencies.
    private fun updateTotalExpensePrice() {
        val totalDollars = calculateTotalAllCurrenciesInUSD()
        val totalEuros = calculateTotalAllCurrenciesInEUR()
        val totalNis = calculateTotalAllCurrenciesInNIS()

        val formattedTotalDollars = if (totalDollars > 0) String.format("%.2f", totalDollars) else "0"
        val formattedTotalEuros = if (totalEuros > 0) String.format("%.2f", totalEuros) else "0"
        val formattedTotalNis = if (totalNis > 0) String.format("%.2f", totalNis) else "0"
        val locale = Locale.getDefault()
        if(locale.language == "iw"){
            binding.totalPriceTextView.text = "סך הכל: $formattedTotalEuros€ | $formattedTotalDollars$ | $formattedTotalNis₪"
        }
        else {
            binding.totalPriceTextView.text =
                "Total: $formattedTotalEuros€ | $formattedTotalDollars$ | $formattedTotalNis₪"
        }
    }

    // Calculates total expenses in Euros from USD, Euros, and NIS using exchange rates.
    fun calculateTotalAllCurrenciesInEUR(): Double {
        val totalDollars = expenseAdapter.calculateTotalExpenseOfDollars()
        val totalEuros = expenseAdapter.calculateTotalExpenseOfEuros()
        val totalNis = expenseAdapter.calculateTotalExpenseOfNis()

        val totalInEURFromNis = totalNis * (1 / usdToNisRate) * (1 / euroToUsdRate)

        // Sum all totals
        return (totalDollars / euroToUsdRate) + totalEuros + totalInEURFromNis
    }

    // Converts total expenses in Euros to USD using the euroToUsdRate exchange rate.
    fun calculateTotalAllCurrenciesInUSD(): Double {
        return calculateTotalAllCurrenciesInEUR() * euroToUsdRate
    }

    // Converts total expenses in USD to NIS using the usdToNisRate exchange rate.
    fun calculateTotalAllCurrenciesInNIS(): Double {
        return calculateTotalAllCurrenciesInUSD() * usdToNisRate
    }
}
