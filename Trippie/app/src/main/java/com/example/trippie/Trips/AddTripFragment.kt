package com.example.trippie.Trips

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*
import com.example.trippie.databinding.FragmentAddTripBinding
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Repository.FirebaseImpl.TripsRepositoryFirebase
import com.example.trippie.Utils.Resource


/**
 * Fragment for adding a new trip. It handles user inputs, validates them, and interacts with the ViewModel to store trip data.
 */
class AddTripFragment : Fragment() {

    private var _binding: FragmentAddTripBinding? = null
    private val binding get() = _binding!!
    private lateinit var startDate: Calendar
    private lateinit var endDate: Calendar
    private var imageUri: Uri? = null

    private val viewModel: TripViewModel by viewModels {
        TripViewModel.TripsViewModelFactory(AuthRepositoryFirebase(), TripsRepositoryFirebase())
    }

    private val pickLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                binding.resultImageView.setImageURI(it)
                requireActivity().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = it
                binding.deleteImageButton.isVisible = true
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTripBinding.inflate(inflater, container, false)
        val rootView = binding.root

        startDate = Calendar.getInstance()
        endDate = Calendar.getInstance()

        binding.startDateButton.setOnClickListener { showDatePicker(startDate, true) }
        binding.endDateButton.setOnClickListener { showDatePicker(endDate, false) }

        binding.endDateButton.isEnabled = false // Initially disable the end date button

        binding.resultImageView.setOnClickListener {
            pickLauncher.launch(arrayOf("image/*"))
        }

        binding.deleteImageButton.setOnClickListener {
            deleteSelectedImage()
        }

        binding.finishBtn.setOnClickListener {
            val country = binding.countryEt.text.toString()
            val city = binding.cityEt.text.toString()
            val startDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate.time)
            val endDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate.time)
            val description = binding.descriptionEt.text.toString()
            val imageUrl = imageUri.toString()
            val activities = binding.activitiesEt.text.toString()
            val hotelDetails = binding.hotelDetailsEt.text.toString()
                // Call view model to add trip
            viewModel.addTrip(
                country = country,
                city = city,
                startDate = startDate,
                endDate = endDate,
                description = description,
                imageUrl = imageUrl,
                activities = activities,
                hotelDetails = hotelDetails
            ).invokeOnCompletion {
                findNavController().navigateUp()
            }
        }
        observeAddTripStatus()
        binding.finishBtn.isEnabled = false
        setupTextWatchers()

        return rootView
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Deletes the selected image from the ImageView
    private fun deleteSelectedImage() {
        imageUri = null
        binding.deleteImageButton.isVisible = false
        binding.resultImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.add_image))
        val toast = getString(R.string.image_deleted)
        Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
    }

    // Displays a DatePickerDialog to select a date, and updates the UI based on the selection
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

    // Updates the labels of the date buttons to reflect the selected dates
    private fun updateDateButton() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.startDateButton.text = dateFormat.format(startDate.time)
        binding.endDateButton.text = dateFormat.format(endDate.time)
    }

    // Observes the status of adding a trip and updates the UI accordingly
    private fun observeAddTripStatus() {
        viewModel.addTripStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    val toast = getString(R.string.trip_Added)
                    Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Sets up text watchers for the country and city input fields to enable the finish button when they are both filled
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
                binding.finishBtn.isEnabled =
                    binding.countryEt.text.toString().trim().isNotEmpty() &&
                            binding.cityEt.text.toString().trim().isNotEmpty()
            }
        }

        // Add text watchers to both EditText fields
        binding.countryEt.addTextChangedListener(textWatcher)
        binding.cityEt.addTextChangedListener(textWatcher)
    }
}
