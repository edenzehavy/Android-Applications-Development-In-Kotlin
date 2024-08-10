package com.example.trippie.Trips


import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trippie.databinding.FragmentTripListBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Repository.FirebaseImpl.TripsRepositoryFirebase
import com.example.trippie.Utils.Resource
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import java.io.IOException
import java.util.Locale

/**
 * A Fragment subclass that displays a list of trips and provides functionality to add new trips,
 */
class TripListFragment : Fragment(), TripAdapter.TripListener {
    private var _binding: FragmentTripListBinding? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    private val binding get() = _binding!!
    private val viewModel: TripViewModel by viewModels {
        TripViewModel.TripsViewModelFactory(AuthRepositoryFirebase(), TripsRepositoryFirebase())
    }
    private lateinit var tripAdapter: TripAdapter
    private var isUserInitiatedDeletion = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTripListBinding.inflate(inflater, container, false)
        binding.addTripButton.setOnClickListener {
            findNavController().navigate(R.id.action_tripListFragment_to_addTripFragment)
        }

        binding.getLocationButton.setOnClickListener(){
            checkLocationPermission()
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

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

        // Set up RecyclerView with LinearLayoutManager
        tripAdapter = TripAdapter(this)
        binding.TripRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tripAdapter
        }

        viewModel.tripStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.addTripButton.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.addTripButton.isEnabled = true
                    (binding.TripRecyclerView.adapter as TripAdapter).setTrips(it.data!!)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.addTripButton.isEnabled = true
                }
            }
        }

        viewModel.updateTripStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.addTripButton.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.addTripButton.isEnabled = true
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.addTripButton.isEnabled = true
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        }


        viewModel.deleteTripStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    if(isUserInitiatedDeletion) {
                        binding.progressBar.isVisible = false
                        val toast = getString(R.string.trip_deleted)
                        Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
                        isUserInitiatedDeletion = false;
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
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
        itemTouchHelper.attachToRecyclerView(binding.TripRecyclerView)
    }

    override fun onItemClick(index: Int) {
        val selectedTrip = tripAdapter.itemAt(index)
        val bundle = bundleOf("trip" to selectedTrip)
        findNavController().navigate(R.id.action_tripListFragment_to_detailedTripFragment, bundle)
    }

    override fun onItemLongClick(index: Int) {
        val selectedTrip = tripAdapter.itemAt(index)
        val bundle = bundleOf("trip" to selectedTrip)
        findNavController().navigate(R.id.action_tripListFragment_to_editTripFragment, bundle)
    }

    override fun onItemSwiped(position: Int) {
        val trip = tripAdapter.itemAt(position)
        val title = getString(R.string.delete_trip_title)
        val msg = getString(R.string.delete_trip_are_you_sure)
        val pos = getString(R.string.yes)
        val neg = getString(R.string.no)
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(pos) { dialog, which ->
                isUserInitiatedDeletion = true
                viewModel.deleteTrip(trip.tripID)
            }
            .setNegativeButton(neg) { dialog, which ->
                tripAdapter.notifyItemChanged(position)  // Restore the item if the user cancels
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
        viewModel.fetchTrips()
    }

    // Handles the confirmation dialog for disconnecting the user
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
                findNavController().navigate(R.id.action_tripListFragment_to_signInFragment)
            }
            .setNegativeButton(neg) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }


    //Checks if location permissions are granted and either requests permission or checks the GPS settings.
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                val title = getString(R.string.location_dialog_title)
                val msg = getString(R.string.location_dialog_msg)
                val pos = getString(R.string.ok)
                AlertDialog.Builder(requireContext())
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(pos) { _, _ ->
                        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            100)
                    }
                    .create()
                    .show()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
            }
        } else {
            checkGPS()
        }
    }

    //Checks if the GPS settings on the device are appropriate for high accuracy location requests.
    private fun checkGPS() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).setMaxUpdateDelayMillis(1000).setMinUpdateIntervalMillis(500).build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(this.requireActivity().applicationContext).checkLocationSettings(builder.build())
        result.addOnCompleteListener{ task ->
            try{
                val response = task.getResult(ApiException:: class.java)

                getUserLocation()
            } catch (e : ApiException){
                e.printStackTrace()

                when(e.statusCode){
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try{
                        val resolveApiException = e as ResolvableApiException
                        resolveApiException.startResolutionForResult(this.requireActivity(),200)
                    } catch(sendIntentException : IntentSender.SendIntentException){

                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->{

                    }
                }
            }
        }
    }

    //Attempts to fetch the current location of the device using the FusedLocationProviderClient.
    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val toast = getString(R.string.location_permissions_are_not_granted)
            Toast.makeText(requireActivity(), toast, Toast.LENGTH_LONG).show()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result
                try {
                    val geocoder = Geocoder(requireActivity(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null) {
                        if (addresses.isNotEmpty()) {
                            val addressLine = addresses[0].getAddressLine(0)
                            openLocation(addressLine)
                        } else {
                            val toast = getString(R.string.no_address_found)
                            Toast.makeText(requireActivity(), toast, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: IOException) {
                    Toast.makeText(requireActivity(), "Geocoder failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } else {
                val toast = getString(R.string.failed_to_get_location)
                Toast.makeText(requireActivity(), toast, Toast.LENGTH_LONG).show()
            }
        }
    }


    //Opens the specified location in Google Maps using a geo URI.
    private fun openLocation(location: String) {
        try {
            val uriString = "geo:0,0?q=${Uri.encode(location)}"
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to open map: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    // Handles the result of a runtime permission request.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted. Do the location-related task you need to do.
                    checkGPS()
                } else {
                    val toast = getString(R.string.permission_denied)
                    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}
