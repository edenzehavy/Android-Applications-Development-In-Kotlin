package com.example.trippie.Expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trippie.databinding.ExpenseItemBinding

/**
 * Adapter for managing a list of expenses in a RecyclerView.
 */
class ExpenseAdapter(private val callBack: ExpenseListener) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val expenses = ArrayList<Expense>()

    // Interface to define the callbacks for click and swipe actions on expense items.
    interface ExpenseListener {
        fun onItemClick(index: Int)
        fun onItemLongClick(index: Int)
        fun onItemSwiped(position: Int)
    }

    // ViewHolder class that binds expense data to the view and handles click and long press events.
    inner class ExpenseViewHolder(private val binding: ExpenseItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        // Binds the expense data to the view elements.
        fun bind(expense: Expense) {
            binding.expenseDestination.text = expense.country + ", " + expense.city
            binding.expenseDescription.text = expense.description
            binding.expensePrice.text = "${expense.price} ${expense.currency}"
        }

        // Handles the click event, triggers the onItemClick callback.
        override fun onClick(p0: View?) {
            callBack.onItemClick(adapterPosition)
        }

        // Handles the long press event, triggers the onItemLongClick callback.
        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClick(adapterPosition)
            return true
        }

        fun onItemSwiped(position: Int) {
            callBack.onItemSwiped(position)
        }
    }

    // Creates a new ViewHolder when RecyclerView needs it.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ExpenseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    // Binds an expense item to a ViewHolder at a specific position.
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) = holder.bind(expenses[position])

    // Returns the total number of items in the expenses list.
    override fun getItemCount() = expenses.size

    // Provides access to an expense item at a specific index.
    fun itemAt(index: Int) = expenses[index]

    // Updates the expenses list with new data and notifies the RecyclerView to refresh.
    fun updateExpenses(newExpenses: List<Expense>) {
        expenses.clear()
        expenses.addAll(newExpenses)
        notifyDataSetChanged()
    }

    // Removes an item from the adapter at the specified position and notifies changes.
    fun removeItem(position: Int) {
        expenses.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, expenses.size)
    }

    // Calculates the total expense amount in dollars.
    fun calculateTotalExpenseOfDollars(): Double {
        return if (expenses.isNotEmpty()) expenses.filter { it.currency == "$" }.sumOf { it.price } else 0.0
    }

    // Calculates the total expense amount in euros.
    fun calculateTotalExpenseOfEuros(): Double {
        return if (expenses.isNotEmpty()) expenses.filter { it.currency == "€" }.sumOf { it.price } else 0.0
    }

    // Calculates the total expense amount in NIS.
    fun calculateTotalExpenseOfNis(): Double {
        return if (expenses.isNotEmpty()) expenses.filter { it.currency == "₪" }.sumOf { it.price } else 0.0
    }

    // Checks if the adapter's expense list is empty.
    fun isAdapterEmpty(): Boolean {
        return expenses.isEmpty()
    }
}
