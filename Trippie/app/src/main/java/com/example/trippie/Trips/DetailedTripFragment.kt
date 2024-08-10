package com.example.trippie.Trips

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.trippie.R
import com.example.trippie.databinding.FragmentDetailedTripBinding
import java.util.Locale

/**
 * A Fragment subclass that displays detailed information about a specific trip.
 */
class DetailedTripFragment : Fragment() {

    private var _binding: FragmentDetailedTripBinding? = null
    private val binding get() = _binding!!
    private lateinit var trip: Trip

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailedTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the trip object passed from the arguments
        trip = requireArguments().getParcelable("trip")!!

        // Bind the trip data to the UI elements
        bindTripData(trip)

        // Set click listener for the "Back" button
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    //Binds the trip data to the UI components of the fragment.
    private fun bindTripData(trip: Trip) {
        val locale = Locale.getDefault()
        var date = trip.startDate + " - " + trip.endDate
        if(locale.language == "iw"){
            date = trip.endDate + " - " + trip.startDate
        }
        binding.apply {
            // Set trip data to the views
            detailedTripDestination.text = trip.country + ", "+ trip.city
            detailedDate.text = date
            itemDesc.text = trip.description
            activitiesDetailed.text = trip.activities
            hotelsDetailed.text = trip.hotelDetails

            // Bind image using Glide
            Glide.with(requireContext())
                .load(trip.imageUrl)
                .placeholder(R.drawable.add_image) // Placeholder image while loading
                .error(R.drawable.add_image) // Error image if loading fails
                .into(tripImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}