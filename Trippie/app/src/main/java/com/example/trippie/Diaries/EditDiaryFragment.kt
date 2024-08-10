package com.example.trippie.Diaries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.trippie.R
import com.example.trippie.databinding.FragmentAddDiaryBinding
import com.example.trippie.databinding.FragmentEditDiaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.trippie.MainActivity
import com.example.trippie.Repository.DiaryRepository
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.RoomDataBase.DiaryDatabase

class EditDiaryFragment : Fragment(R.layout.fragment_edit_diary), MenuProvider {

    // Variable to hold the binding object for the fragment's layout
    private var _binding: FragmentEditDiaryBinding? = null

    // Property to access the binding object, ensuring it is not null
    private val binding get() = _binding!!

    // Variable to hold the current diary object passed as an argument
    private lateinit var currentDiary: Diary

    // Variable to retrieve arguments passed to the fragment
    private val args: EditDiaryFragmentArgs by navArgs()

    // Firebase Authentication instance
    private lateinit var auth: FirebaseAuth

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
        // Inflate the layout for this fragment using view binding
        _binding = FragmentEditDiaryBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Adding the MenuProvider to handle menu operations in the fragment
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Retrieve the current diary from the arguments
        currentDiary = args.diary!!
        // Set the current diary's title and content in the EditText fields
        binding.editDiaryTitle.setText(currentDiary.title)
        binding.editDiaryContent.setText(currentDiary.content)

        // Set a click listener for the save button
        binding.editDiaryButton.setOnClickListener {
            // Retrieve the title and content entered by the user
            val title = binding.editDiaryTitle.text.toString().trim()
            val content = binding.editDiaryContent.text.toString().trim()
            val negtoast = getString(R.string.please_enter_diary_title)
            val postoast = getString(R.string.diary_saved)
            if (title.isNotEmpty()) {
                // Create a new Diary object with the updated information
                val diary = Diary(currentDiary.diaryID, title, "", content)
                diaryViewModel.updateDiary(diary)
                Toast.makeText(context, postoast, Toast.LENGTH_SHORT).show()
                // Navigate back to the diary list fragment
                view.findNavController().popBackStack(R.id.diaryListFragment, false)
            } else {
                // Show a toast message if the title is empty
                Toast.makeText(context, negtoast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to delete the current diary
    private fun deleteDiary() {
        val title = getString(R.string.delete_diary_title)
        val msg = getString(R.string.delete_diary_are_you_sure)
        val pos = getString(R.string.yes)
        val neg = getString(R.string.no)
        val toast = getString(R.string.diary_deleted_toast)
        AlertDialog.Builder(requireActivity()).apply {
            setTitle(title)
            setMessage(msg)
            setPositiveButton(pos) { _, _ ->
                // Delete the diary from the ViewModel
                diaryViewModel.deleteDiary(currentDiary)
                // Show a toast message confirming the deletion
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
                // Navigate back to the diary list fragment
                view?.findNavController()?.popBackStack(R.id.diaryListFragment, false)
            }
            setNegativeButton(neg, null)
        }.create().show()
    }

    // Inflate the menu for the fragment
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.edit_diary_menu, menu)
    }

    // Handle menu item selections
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.deleteMenu -> {
                // Delete the diary when the delete menu item is selected
                deleteDiary()
                true
            }
            else -> false
        }
    }

    // Clean up the binding when the fragment is destroyed
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
