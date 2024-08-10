package com.example.trippie.Diaries
import com.example.trippie.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.trippie.databinding.DiaryItemBinding


/**
 * Adapter for the RecyclerView that displays diary entries.
 */
class DiaryAdapter() : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    // ViewHolder class that holds the binding for each diary item.
    class DiaryViewHolder(val binding: DiaryItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Callback for calculating the difference between two items in the list.
    private val differCallback = object : DiffUtil.ItemCallback<Diary>() {
        // Checks if two diary items are the same by comparing their IDs and content.
        override fun areItemsTheSame(oldItem: Diary, newItem: Diary): Boolean {
            return oldItem.diaryID == newItem.diaryID &&
                    oldItem.content == newItem.content &&
                    oldItem.title == newItem.title
        }

        // Checks if the content of two diary items is the same.
        override fun areContentsTheSame(oldItem: Diary, newItem: Diary): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    // Creates a new ViewHolder for a diary item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = DiaryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiaryViewHolder(binding)
    }


    // Binds data to an existing ViewHolder, setting the title and content of the diary entry and handling item clicks.
    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val currentDiary = differ.currentList[position]
        holder.binding.diaryTitle.text = currentDiary.title
        holder.binding.diaryContent.text = currentDiary.content

        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("diary", currentDiary)
            }
            it.findNavController().navigate(R.id.action_diaryListFragment_to_editDiaryFragment, bundle)
        }
    }

    // Returns the total number of items in the list.
    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}
