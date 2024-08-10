package com.example.trippie

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trippie.Diaries.DiaryViewModel
import com.example.trippie.Diaries.DiaryViewModelFactory
import com.example.trippie.Repository.DiaryRepository
import com.example.trippie.RoomDataBase.DiaryDatabase
import com.example.trippie.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        setupNavigation()
        FirebaseApp.initializeApp(this)
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // Add an OnDestinationChangedListener to control visibility of bottom navigation view
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val noNavBarFragList = listOf(
                R.id.splashScreenFragment,
                R.id.signInFragment,
                R.id.signUpFragment,
                R.id.addTripFragment,
                R.id.addDiaryFragment,
                R.id.addExpenseFragment,
                R.id.editTripFragment,
                R.id.editDiaryFragment,
                R.id.editExpenseFragment,
                R.id.detailedTripFragment,
                R.id.detailedExpenseFragment
            )
            if (destination.id in noNavBarFragList) {
                binding.bottomNavView.visibility = View.GONE
            } else {
                binding.bottomNavView.visibility = View.VISIBLE
            }
        }
        binding.bottomNavView.setupWithNavController(navController)
    }


    // Define the setupMenu function
    fun setupMenu(currentFragment: Fragment) {
        val navController = currentFragment.findNavController()
        val currentDestination = navController.currentDestination

        // Check if the current destination is DiaryListFragment
        val isInDiaryListFragment = currentDestination?.id == R.id.diaryListFragment

        // Hide or show menu based on whether we are in DiaryListFragment
        if (!isInDiaryListFragment) {
            // Assuming you have a menu to hide, for example:
            currentFragment.requireActivity().invalidateOptionsMenu()
        }
    }
}

