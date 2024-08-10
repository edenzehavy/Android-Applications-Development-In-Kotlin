package com.example.trippie.Package

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Repository.FirebaseImpl.PackageRepositoryFirebase
import com.example.trippie.Utils.Resource
import com.example.trippie.databinding.FragmentAddPackageItemBinding

/**
 * Dialog Fragment to add a new package item to the list.
 */
class AddPackageItemFragment : DialogFragment() {

    private var _binding: FragmentAddPackageItemBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PackageItemViewModel by viewModels {
        PackageItemViewModel.PackageItemViewModelFactory(AuthRepositoryFirebase(), PackageRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPackageItemBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.finishBtn.setOnClickListener {
            val etCount = if (binding.countEt.text.toString().isNotEmpty()) {
                binding.countEt.text.toString().toInt()
            } else {
                1
            }

            viewModel.addPackageItem(
                description = binding.descriptionEt.text.toString(),
                count = etCount,
                isChecked = 0
            ).invokeOnCompletion {
                dismiss()
            }
        }

        addTextWatchers()
        validateForm()
        observeAddPackageItemStatus()

        return rootView
    }


    //Sets the dialog dimensions when the fragment starts.
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //Adds text change listeners to input fields to enable or disable the finish button based on input validity.
    private fun addTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        binding.descriptionEt.addTextChangedListener(textWatcher)
        binding.countEt.addTextChangedListener(textWatcher)
    }

    //Validates the input form to enable the finish button only if all required fields are filled.
    private fun validateForm() {
        val isDescriptionFilled = binding.descriptionEt.text.toString().isNotEmpty()
        val isCountFilled = binding.countEt.text.toString().isNotEmpty()

        binding.finishBtn.isEnabled = isDescriptionFilled && isCountFilled
    }

    //Observes the status of adding a package item and updates the UI accordingly.
    private fun observeAddPackageItemStatus() {
        viewModel.addPackageItemStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.finishBtn.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    val toast = getString(R.string.package_item_added)
                    Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
