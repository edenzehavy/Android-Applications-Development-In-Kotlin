package com.example.trippie.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.trippie.MainActivity
import com.example.trippie.R
import com.example.trippie.databinding.FragmentSplashScreenBinding

/**
 * Fragment to display a splash screen when the application launches.
 */
@SuppressLint("CustomSplashScreen")
class SplashScreenFragment : Fragment() {

    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure this runs on the main thread
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)

        binding.logo.animate().translationY(4000f).setDuration(1000).setStartDelay(2000).start()
        binding.appName.animate().translationY(4000f).setDuration(1000).setStartDelay(2000).start()
        binding.splashBg.animate().translationY(-4000f).setDuration(1000).setStartDelay(2000).start()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).setupMenu(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && !hasNavigated) {
                hasNavigated = true
                findNavController().navigate(R.id.action_splashScreenFragment_to_signInFragment)
            }
        }, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
