package com.example.trippie.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Utils.Resource
import com.example.trippie.databinding.FragmentSignUpBinding


/**
 * Fragment for managing user registration.
 */
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel : SignUpViewModel by viewModels() {
        SignUpViewModel.SignUpViewModelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.alreadyHaveAccountBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.signUpButton.setOnClickListener {
            signUp()
        }

        // Initialize sign-up button state
        binding.signUpButton.isEnabled = false

        // Add text change listeners to EditText fields
        binding.usernameSignUpEt.addTextChangedListener(textWatcher)
        binding.passwordSignUpEt.addTextChangedListener(textWatcher)
        binding.confirmPasswordSignUpEt.addTextChangedListener(textWatcher)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)        // Apply animation to CardView
        val cardView = view.findViewById<View>(R.id.sign_up_cardview)
        val slideInTop = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_top)
        cardView.startAnimation(slideInTop)

        viewModel.userRegistrationStatus.observe(viewLifecycleOwner) {

            when(it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.signUpButton.isEnabled = false
                }
                is Resource.Success -> {
                    val toast = getString(R.string.registration_successful)
                    Toast.makeText(requireContext(),toast,Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signUpFragment_to_signInFragment)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.signUpButton.isEnabled = true
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Initiates the user registration with the entered credentials
    private fun signUp() {
        val email = binding.usernameSignUpEt.text.toString()
        val password = binding.passwordSignUpEt.text.toString()
        val confirmPassword = binding.confirmPasswordSignUpEt.text.toString()

        if (password != confirmPassword) {
            val toast = getString(R.string.passwords_dont_match)
            Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.createUser(email, password)
    }

    // TextWatcher to monitor text changes in EditText fields
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            // Enable sign-up button if all fields are not empty
            val username = binding.usernameSignUpEt.text.toString()
            val password = binding.passwordSignUpEt.text.toString()
            val confirmPassword = binding.confirmPasswordSignUpEt.text.toString()
            binding.signUpButton.isEnabled = username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
        }
    }
}
