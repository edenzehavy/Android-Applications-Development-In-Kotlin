package com.example.trippie.Trips

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Repository.FirebaseImpl.TripsRepositoryFirebase
import com.example.trippie.databinding.FragmentEditTripBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A Fragment that provides functionality to edit an existing trip's details.
 */
class EditTripFragment : Fragment() {

    private var _binding: FragmentEditTripBinding? = null
    private val binding get() = _binding!!
    private lateinit var startDate: Calendar
    private lateinit var endDate: Calendar
    private lateinit var trip: Trip
    private var imageUri: Uri? = null
    private val viewModel: TripViewModel by viewModels {
        TripViewModel.TripsViewModelFactory(AuthRepositoryFirebase(), TripsRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTripBinding.inflate(inflater, container, false)
        startDate = Calendar.getInstance()
        endDate = Calendar.getInstance()

        binding.startDateButton.setOnClickListener { showDatePicker(startDate, true) }
        binding.endDateButton.setOnClickListener { showDatePicker(endDate, false) }

        binding.endDateButton.isEnabled = false // Initially disable the end date button

        binding.resultImageView.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }
        setupTextWatchers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the trip object passed from the arguments
        trip = requireArguments().getParcelable("trip")!!

        // Bind the trip data to the UI elements
        bindTripData(trip)

        // Set click listener for the "Delete Image" button
        binding.deleteImageButton.setOnClickListener {
            // Reset the image to the default "Add Image"
            binding.resultImageView.setImageResource(R.drawable.add_image)
            // Clear the imageUri variable and hide the delete button
            imageUri = null
            binding.deleteImageButton.visibility = View.GONE
        }

        // Set click listener for the "Save" button
        binding.saveBtn.setOnClickListener {
            // Retrieve the updated trip information from UI elements
            val updatedCountry = binding.countryEt.text.toString()
            val updatedCity = binding.cityEt.text.toString()
            val updatedStartDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate.time)
            val updatedEndDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate.time)
            val updatedDescription = binding.descriptionEt.text.toString()
            val updatedImageUrl = imageUri.toString()
            val updatedActivities = binding.activitiesEt.text.toString()
            val updatedHotelDetails = binding.hotelDetailsEt.text.toString()

            // Call the view model method to update the trip
            viewModel.updateTrip(trip.tripID, updatedCountry, updatedCity, updatedStartDate, updatedEndDate, updatedDescription, updatedImageUrl, updatedActivities, updatedHotelDetails).invokeOnCompletion {
                findNavController().navigateUp()
            }
        }
    }

    //Binds the existing trip data to the UI components for editing.
    private fun bindTripData(trip: Trip) {
        imageUri = trip.imageUrl?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
        binding.apply {
            // Set trip data to the views
            countryEt.setText(trip.country)
            cityEt.setText(trip.city)
            descriptionEt.setText(trip.description)
            activitiesEt.setText(trip.activities)
            hotelDetailsEt.setText(trip.hotelDetails)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            startDate.time = dateFormat.parse(trip.startDate) ?: Calendar.getInstance().time
            endDate.time = dateFormat.parse(trip.endDate) ?: Calendar.getInstance().time

            startDateButton.text = trip.startDate
            endDateButton.text = trip.endDate
        }

            // Check if imageUri is null or points to the default "Add Image" drawable
            if (imageUri.toString() == "null") {
                // Load trip.imageUrl if it exists, otherwise set a placeholder
                Glide.with(requireContext())
                    .load(trip.imageUrl ?: R.drawable.add_image)
                    .placeholder(R.drawable.add_image) // Placeholder image while loading
                    .error(R.drawable.add_image) // Error image if loading fails
                    .into(binding.resultImageView)

                // Hide delete button when image is the default "Add Image"
                binding.deleteImageButton.visibility = View.GONE
            } else {
                // Load the user provided image
                binding.resultImageView.setImageURI(imageUri)

                // Show delete button and attach click listener to delete image
                binding.deleteImageButton.visibility = View.VISIBLE
                binding.deleteImageButton.setOnClickListener {
                    // Reset the image to the default "Add Image"
                    binding.resultImageView.setImageResource(R.drawable.add_image)
                    // Clear the imageUri variable and hide the delete button
                    imageUri = null
                    binding.deleteImageButton.visibility = View.GONE
                }
            }

        // Enable end date button and set min date
        binding.endDateButton.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                Glide.with(requireContext())
                    .load(it)
                    .placeholder(R.drawable.add_image) // Placeholder image while loading
                    .error(R.drawable.add_image) // Error image if loading fails
                    .into(binding.resultImageView)
                requireActivity().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = it

                // Show the delete button if it's not visible and an image is selected
                if (binding.deleteImageButton.visibility != View.VISIBLE) {
                    binding.deleteImageButton.visibility = View.VISIBLE
                }
            }
        }

    //Displays a DatePickerDialog to select a date, updates the UI based on the selection.
    private fun showDatePicker(calendar: Calendar, isStartDate: Boolean) {
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            updateDateButton()
            if (isStartDate) {
                // Enable the end date button once a start date is picked
                binding.endDateButton.isEnabled = true
                // Reset the end date to be after the new start date
                endDate = calendar.clone() as Calendar
                endDate.add(Calendar.DAY_OF_MONTH, 0) // Default end date to one day after start date
                updateDateButton()
            } else {
                if (endDate.before(startDate)) {
                    val toast = getString(R.string.end_date_before_start)
                    Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
                }
            }
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            datePickerListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        if (!isStartDate) {
            // Set the minimum date for end date picker to be the day after the start date
            datePickerDialog.datePicker.minDate = startDate.timeInMillis + (24 * 60 * 60 * 1000)
        }

        datePickerDialog.show()
    }

    //Updates the date buttons to reflect the selected dates.
    private fun updateDateButton() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.startDateButton.text = dateFormat.format(startDate.time)
        binding.endDateButton.text = dateFormat.format(endDate.time)
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used, but must be implemented
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used, but must be implemented
            }

            override fun afterTextChanged(s: Editable?) {
                // Check if both fields are filled to enable the button
                binding.saveBtn.isEnabled =
                    binding.countryEt.text.toString().trim().isNotEmpty() &&
                            binding.cityEt.text.toString().trim().isNotEmpty()
            }
        }

        // Add text watchers to both EditText fields
        binding.countryEt.addTextChangedListener(textWatcher)
        binding.cityEt.addTextChangedListener(textWatcher)
    }
}
