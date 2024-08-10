package com.example.trippie.Package

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.trippie.databinding.PackageItemBinding

/**
 * Adapter for displaying package items in a RecyclerView.
 */
class PackageItemAdapter(private val callBack: PackageItemListener) :
    RecyclerView.Adapter<PackageItemAdapter.PackageViewHolder>() {

    private val packageItems = ArrayList<PackageItem>()

    // Interface to handle interaction events on package items
    interface PackageItemListener {
        fun onItemClick(index: Int)
        fun onItemLongClick(index: Int)
        fun onItemSwiped(position: Int)
        fun onCheckPackageItem(packageItem: PackageItem)
    }

    /**
     * ViewHolder for package items, manages item views and handles UI interactions.
     */
    inner class PackageViewHolder(private val binding: PackageItemBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener, OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
            binding.completedButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val packageItem = packageItems[position]
                    packageItem.isChecked = if (packageItem.isChecked == 0) 1 else 0
                    callBack.onCheckPackageItem(packageItem)
                    binding.completedButton.setImageResource(packageItem.checkedImage())
                }
            }
        }

        // Binds package item data to the views in the ViewHolder
        fun bind(packageItem: PackageItem) {
            binding.packageItemDescription.text = packageItem.description
            binding.packageItemCount.text = packageItem.count.toString()
            binding.completedButton.setImageResource(packageItem.checkedImage())
        }

        // Handles item click events
        override fun onClick(p0: View?) {
            callBack.onItemClick(adapterPosition)
        }

        // Handles item long click events
        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClick(adapterPosition)
            return true
        }
    }

    // Creates ViewHolder instances for package items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val binding = PackageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackageViewHolder(binding)
    }

    // Binds data to an existing ViewHolder at the specified position
    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.bind(packageItems[position])
    }

    // Returns the total number of items in the list
    override fun getItemCount() = packageItems.size

    // Returns the package item at a specific position
    fun itemAt(index: Int) = packageItems[index]

    // Updates the data set of the adapter and refreshes the view
    fun setPackageItems(newPackageItems: Collection<PackageItem>) {
        packageItems.clear()
        packageItems.addAll(newPackageItems)
        notifyDataSetChanged()
    }

}
