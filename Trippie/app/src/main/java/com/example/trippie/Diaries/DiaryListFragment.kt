package com.example.trippie.Diaries

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.trippie.Diaries.Diary
import com.example.trippie.Diaries.DiaryAdapter
import com.example.trippie.Diaries.DiaryViewModel
import com.example.trippie.MainActivity
import com.example.trippie.R
import com.example.trippie.Repository.AuthRepository
import com.example.trippie.Repository.DiaryRepository
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Repository.FirebaseImpl.TripsRepositoryFirebase
import com.example.trippie.RoomDataBase.DiaryDatabase
import com.example.trippie.Trips.TripViewModel
import com.example.trippie.databinding.FragmentDiaryListBinding
import java.util.Locale

class DiaryListFragment : Fragment(R.layout.fragment_diary_list), SearchView.OnQueryTextListener, MenuProvider{

    // Variable to hold the binding object for the fragment's layout
    private var _binding: FragmentDiaryListBinding? = null

    // Property to access the binding object, ensuring it is not null
    private val binding get() = _binding!!

    // Adapter for the RecyclerView to manage diary items
    private lateinit var diaryAdapter: DiaryAdapter

    private val diaryViewModel: DiaryViewModel by viewModels {
        DiaryViewModelFactory(
            requireActivity().application,
            AuthRepositoryFirebase(),
            DiaryRepository(DiaryDatabase(this.requireActivity()))
        )
    }

    // Inflate the fragment's layout and return the root view
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiaryListBinding.inflate(inflater, container, false)

        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDisconnectConfirmationDialog()
            }
        })
        return binding.root
    }

    // Called immediately after onCreateView, used for setting up the UI
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add a MenuProvider to handle menu-related interactions
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupDiaryRecyclerView()

        // Set a click listener on the button to navigate to the add diary fragment
        binding.addDiaryButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_diaryListFragment_to_addDiaryFragment)
        }

    }

    // Update the UI based on the list of diaries
    private fun updateUI(diary: List<Diary>?) {
        if (diary != null) {
            if (diary.isNotEmpty()) {
                binding.emptyNotesImage.visibility = View.GONE
                binding.diaryRecyclerView.visibility = View.VISIBLE
            } else {
                binding.emptyNotesImage.visibility = View.VISIBLE
                binding.diaryRecyclerView.visibility = View.GONE
            }
        }
    }

    // Setup the RecyclerView with an adapter and layout manager
    private fun setupDiaryRecyclerView() {
        diaryAdapter = DiaryAdapter()
        binding.diaryRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = diaryAdapter
        }

        // Observe the LiveData from the ViewModel to update the list of diaries
        activity?.let {
            diaryViewModel.getAllDiaries().observe(viewLifecycleOwner) { diary ->
                diaryAdapter.differ.submitList(diary)
                updateUI(diary)
            }
        }
    }

    // Search for diaries based on the query string
    private fun searchDiary(query: String?) {
        val searchQuery = "%$query%"
        diaryViewModel.searchDiary(searchQuery).observe(this) { list ->
            diaryAdapter.differ.submitList(list)
        }
    }

    // Handle query text submission (not used here)
    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    // Handle text changes in the search view to filter diaries
    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchDiary(newText)
        }
        return true
    }

    // Clean up the binding object when the fragment is destroyed
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Inflate the menu for the fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.search_diary_menu, menu)

        // Set up the search view in the menu
        val menuSearch = menu.findItem(R.id.searchMenu).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false
        menuSearch.setOnQueryTextListener(this)
    }

    // Handle menu item selections (not used here)
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    private fun showDisconnectConfirmationDialog() {
        val title = getString(R.string.disconnect_title)
        val msg = getString(R.string.disconnect_are_you_sure)
        val pos = getString(R.string.yes)
        val neg = getString(R.string.no)
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(pos) { dialog, which ->
                diaryViewModel.signOut()
                findNavController().navigate(R.id.action_diaryListFragment_to_signInFragment)
            }
            .setNegativeButton(neg) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}
