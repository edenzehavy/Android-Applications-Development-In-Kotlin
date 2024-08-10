package com.example.trippie.Package

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trippie.R
import com.example.trippie.Repository.FirebaseImpl.AuthRepositoryFirebase
import com.example.trippie.Repository.FirebaseImpl.PackageRepositoryFirebase
import com.example.trippie.Utils.Resource
import com.example.trippie.databinding.FragmentPackageListBinding

/**
 * Fragment for displaying and managing a list of package items.
 */
class PackageListFragment : Fragment(), PackageItemAdapter.PackageItemListener {

    private var _binding: FragmentPackageListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PackageItemViewModel by activityViewModels {
        PackageItemViewModel.PackageItemViewModelFactory(AuthRepositoryFirebase(), PackageRepositoryFirebase())
    }
    private lateinit var packageItemAdapter: PackageItemAdapter
    private var isUserInitiatedDeletion = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPackageListBinding.inflate(inflater, container, false)
        binding.addPackageItemButton.setOnClickListener {
            val dialogFragment = AddPackageItemFragment()
            dialogFragment.show(parentFragmentManager, "addPackageItemFragment")
        }

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
        packageItemAdapter = PackageItemAdapter(this)
        binding.PackageItemRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = packageItemAdapter
        }

        // Observe package items from ViewModel
        viewModel.packageItemsStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.addPackageItemButton.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.isVisible = false
                    binding.addPackageItemButton.isEnabled = true
                    packageItemAdapter.setPackageItems(it.data!!)
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
                    binding.addPackageItemButton.isEnabled = true
                }
            }
        }

        viewModel.deletePackageItemStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Loading -> {
                    binding.progressBar.isVisible = true
                }
                is Resource.Success -> {
                    if(isUserInitiatedDeletion) {
                        binding.progressBar.isVisible = false
                        val toast = getString(R.string.package_item_deleted)
                        Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.isVisible = false
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
        itemTouchHelper.attachToRecyclerView(binding.PackageItemRecyclerView)
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchPackageItems()
    }

    override fun onItemClick(index: Int) {
    }

    override fun onItemLongClick(index: Int) {
    }

    override fun onItemSwiped(position: Int) {
        val packageItem = packageItemAdapter.itemAt(position)
        val title = getString(R.string.delete_package_title)
        val msg = getString(R.string.delete_package_are_you_sure)
        val pos = getString(R.string.yes)
        val neg = getString(R.string.no)
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(msg)
            .setPositiveButton(pos) { dialog, which ->
                isUserInitiatedDeletion = true
                viewModel.deletePackageItem(packageItem.packageItemID)
            }
            .setNegativeButton(neg) { dialog, which ->
                packageItemAdapter.notifyItemChanged(position) // Restore the item if the user cancels
                dialog.dismiss()
            }
            .show()
    }

    override fun onCheckPackageItem(packageItem: PackageItem) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                viewModel.signOut()
                findNavController().navigate(R.id.action_checklistFragment_to_signInFragment)
            }
            .setNegativeButton(neg) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}
