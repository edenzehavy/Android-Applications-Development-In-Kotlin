package com.example.trippie.Trips

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trippie.databinding.TripItemBinding
import java.util.Locale

/**
 * Adapter for a RecyclerView that displays a list of trips.
 */
class TripAdapter(private val callBack: TripListener) :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    private val trips = ArrayList<Trip>()


    /**
     * Interface for handling item interactions in the RecyclerView.
     */
    interface TripListener {
        fun onItemClick(index: Int)
        fun onItemLongClick(index: Int)
        fun onItemSwiped(position: Int)
    }

    /**
     * ViewHolder class for trip items. Handles the binding of trip data to the layout.
     */
    inner class TripViewHolder(private val binding: TripItemBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener, OnLongClickListener {

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnLongClickListener(this)
        }

        //Binds trip data to the ViewHolder's layout.
        fun bind(trip: Trip) {
            val locale = Locale.getDefault()
            var date = trip.startDate + " - " + trip.endDate
            if(locale.language == "iw"){
                date = trip.endDate + " - " + trip.startDate
            }
            binding.tripDestination.text = trip.country + ", " + trip.city
            binding.tripDates.text = date
            Glide.with(binding.root).load(trip.imageUrl).circleCrop().into(binding.tripImage)
        }

        override fun onClick(p0: View?) {
            callBack.onItemClick(adapterPosition)
        }

        override fun onLongClick(p0: View?): Boolean {
            callBack.onItemLongClick(adapterPosition)
            return true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = TripItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    //Binds the trip at the specified position to the ViewHolder.
    override fun onBindViewHolder(holder: TripViewHolder, position: Int) = holder.bind(trips[position])

    //Returns the total number of items in the data set held by the adapter.
    override fun getItemCount() = trips.size

    //Returns the trip at the specified index.
    fun itemAt(index: Int) = trips[index]

    //Updates the list of trips in the adapter.
    fun setTrips(trips: Collection<Trip>) {
        this.trips.clear()
        this.trips.addAll(trips)
        notifyDataSetChanged()
    }
}
