package com.natali.studytip.ui.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.natali.studytip.R
import com.natali.studytip.StudyTipsApplication
import com.natali.studytip.databinding.FragmentProfileBinding
import com.natali.studytip.ui.ViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory(
            requireActivity().application,
            (requireActivity().application as StudyTipsApplication).tipRepository,
            (requireActivity().application as StudyTipsApplication).userRepository,
            (requireActivity().application as StudyTipsApplication).quoteRepository,
            (requireActivity().application as StudyTipsApplication).authRepository,
            (requireActivity().application as StudyTipsApplication).firebaseStorageManager,
            (requireActivity().application as StudyTipsApplication).firestoreManager
        )
    }

    private lateinit var profileTipsAdapter: ProfileTipsAdapter

    // Profile photo picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadProfilePhoto(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        profileTipsAdapter = ProfileTipsAdapter(
            onEditClick = { tip ->
                showEditTipBottomSheet(tip)
            },
            onDeleteClick = { tip ->
                showDeleteConfirmation(tip.id)
            }
        )

        binding.myTipsRecyclerView.apply {
            adapter = profileTipsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        // Observe current user
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.userName.text = user.name
                binding.userEmail.text = user.email
                binding.userBio.text = user.bio ?: getString(R.string.bio)
                binding.tipsCount.text = getString(R.string.tips_count, user.tipsCount)

                // Load profile photo
                if (user.photoUrl != null) {
                    Glide.with(binding.profilePhoto)
                        .load(user.photoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.profilePhoto)
                } else {
                    binding.profilePhoto.setImageResource(R.drawable.ic_person)
                }
            }
        }

        // Observe user's tips
        viewModel.userTips.observe(viewLifecycleOwner) { tips ->
            profileTipsAdapter.submitList(tips)

            // Show empty state if no tips
            binding.emptyState.isVisible = tips.isEmpty()
            binding.myTipsRecyclerView.isVisible = tips.isNotEmpty()

            // Update tips count
            binding.tipsCount.text = tips.size.toString()
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        // Observe profile update
        viewModel.profileUpdated.observe(viewLifecycleOwner) { updated ->
            if (updated) {
                Toast.makeText(requireContext(), getString(R.string.profile_updated), Toast.LENGTH_SHORT).show()
                viewModel.resetUpdatedState()
            }
        }

        // Observe errors
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Observe logout success
        viewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                navigateToLogin()
            }
        }
    }

    private fun setupClickListeners() {
        // Camera FAB for profile photo
        binding.cameraFab.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Toolbar menu items (Settings & Logout)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    showEditProfileBottomSheet()
                    true
                }
                R.id.action_logout -> {
                    showLogoutConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun showEditProfileBottomSheet() {
        val currentUserValue = viewModel.currentUser.value ?: return

        val bottomSheet = EditProfileBottomSheet(
            currentName = currentUserValue.name,
            currentBio = currentUserValue.bio,
            onSave = { name, bio ->
                viewModel.updateProfile(name, bio)
            }
        )

        bottomSheet.show(childFragmentManager, EditProfileBottomSheet.TAG)
    }

    private fun showEditTipBottomSheet(tip: com.natali.studytip.data.models.Tip) {
        val bottomSheet = EditTipBottomSheet(
            tipId = tip.id,
            currentTitle = tip.title,
            currentDescription = tip.description,
            onSave = { id, title, description ->
                viewModel.updateTip(id, title, description)
            }
        )

        bottomSheet.show(childFragmentManager, EditTipBottomSheet.TAG)
    }

    private fun showDeleteConfirmation(tipId: String) {
        // Inflate custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_confirmation, null)

        // Create dialog with custom view
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Set up button click listeners
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btn_delete).setOnClickListener {
            viewModel.deleteTip(tipId)
            Toast.makeText(requireContext(), getString(R.string.tip_deleted), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.logout))
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun navigateToLogin() {
        // Navigate to login screen and clear backstack
        findNavController().navigate(R.id.loginFragment)
    }

    private fun uploadProfilePhoto(uri: Uri) {
        // Upload the profile photo
        viewModel.uploadProfilePhoto(uri)

        // Show immediate preview
        Glide.with(binding.profilePhoto)
            .load(uri)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .circleCrop()
            .into(binding.profilePhoto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
