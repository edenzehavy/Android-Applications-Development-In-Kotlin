package com.example.trippie.Fragments

import android.os.Bundle
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.trippie.MainActivity
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Utils.Resource
import com.example.trippie.databinding.FragmentSignInBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * Fragment to manage user sign-in functionality.
 */
class SignInFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val viewModel : SignInViewModel by viewModels {
        SignInViewModel.SignInViewModelFactory(AuthRepositoryFirebase())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        val view = binding.root
        auth = FirebaseAuth.getInstance()

        setupPasswordEditText(binding.passwordSignInEt)

        binding.signInButton.setOnClickListener {
            val email = binding.emailSignInEt.text.toString()
            val password = binding.passwordSignInEt.text.toString()
            viewModel.signInUser(email, password)
        }

        binding.notRegisteredButton.setOnClickListener {
            navigateToSignUpFragment()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setupMenu(this)

        // Apply animation to CardView
        val cardView = view.findViewById<View>(R.id.sign_in_cardview)
        val slideInTop = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_top)
        cardView.startAnimation(slideInTop)

        // Handle back button press to exit the app
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        })

        viewModel.userSignInStatus.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> showProgressBar()
                is Resource.Success -> {
                    val toast = getString(R.string.login_successful)
                    hideProgressBar()
                    Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signInFragment_to_tripListFragment)
                }
                is Resource.Error -> {
                    hideProgressBar()
                    val toast = getString(R.string.unkown_error_occured)
                    Toast.makeText(requireContext(), resource.message ?: toast, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> showProgressBar()
                is Resource.Success -> {
                    hideProgressBar()
                    findNavController().navigate(R.id.action_signInFragment_to_tripListFragment)
                }
                is Resource.Error -> {
                    hideProgressBar()
                    // Do not show a toast here since it's initial state check
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Navigates to the sign-up fragment
    private fun navigateToSignUpFragment() {
        findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
    }

    private fun setupPasswordEditText(editText: EditText) {
        editText.transformationMethod = PasswordTransformationMethod.getInstance()
        val maxLength = 16
        editText.filters = arrayOf(InputFilter.LengthFilter(maxLength))
    }

    // Shows the progress bar and disables the sign-in button
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.signInButton.isEnabled = false
    }

    // Hides the progress bar and enables the sign-in button
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.signInButton.isEnabled = true
    }
}