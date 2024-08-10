package com.example.trippie.Diaries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.example.trippie.R
import com.example.trippie.Repository.DiaryRepository
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.RoomDataBase.DiaryDatabase
import com.example.trippie.databinding.FragmentAddDiaryBinding

class AddDiaryFragment : Fragment(R.layout.fragment_add_diary), MenuProvider{

    // Variable to hold the binding object for the fragment's layout
    private var _binding: FragmentAddDiaryBinding? = null

    // Property to access the binding object, ensuring it is not null
    private val binding get() = _binding!!

    private lateinit var addDiaryView: View

    private val diaryViewModel: DiaryViewModel by viewModels {
        DiaryViewModelFactory(
            requireActivity().application,
            AuthRepositoryFirebase(),
            DiaryRepository(DiaryDatabase(this.requireActivity()))
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add a MenuProvider to handle menu-related interactions
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        addDiaryView = view
    }

    // Saves the diary entry to the database
    private fun saveDiary(view: View){
        val diaryTitle = binding.addDiaryTitle.text.toString().trim()
        val diaryContent = binding.addDiaryContent.text.toString().trim()

        if (diaryTitle.isNotEmpty()) {
            val toast = getString(R.string.diary_added_toast)
            val diary = Diary(0, diaryTitle, "", diaryContent)
            diaryViewModel.addDiary(diary)
            Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
            view.findNavController().popBackStack(R.id.diaryListFragment, false)
        } else {
            val toast = getString(R.string.please_enter_diary_title)
            Toast.makeText(addDiaryView.context, toast, Toast.LENGTH_SHORT).show()
            view.findNavController().popBackStack(R.id.diaryListFragment, false)

        }

    }

    // Creates the fragment menu by inflating the menu layout.
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.add_diary_menu, menu)
    }

    // Handles menu item selection, specifically the save action.
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.saveMenu -> {
                saveDiary(addDiaryView)
                true
            }
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}