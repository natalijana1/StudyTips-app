package com.natali.studytip.ui.tip

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.natali.studytip.R
import com.natali.studytip.StudyTipsApplication
import com.natali.studytip.databinding.FragmentCreateTipBinding
import com.natali.studytip.ui.ViewModelFactory
import com.natali.studytip.utils.ImageHelper

class CreateTipFragment : Fragment() {
    private var _binding: FragmentCreateTipBinding? = null
    private val binding get() = _binding!!

    private val args: CreateTipFragmentArgs by navArgs()

    private val viewModel: TipViewModel by viewModels {
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

    private var isEditMode = false
    private var selectedImageUri: Uri? = null
    private var pendingTipCreation: Pair<String, String>? = null // title, description

    // Image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            displaySelectedImage(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupTextWatcher()
        setupObservers()
        setupClickListeners()
        loadTipIfEditing()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTextWatcher() {
        // Character counter for description
        binding.descriptionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val length = s?.length ?: 0
                binding.descriptionInputLayout.helperText = getString(R.string.character_count, length)
            }
        })
    }

    private fun setupObservers() {
        // Observe current tip (for edit mode)
        viewModel.currentTip.observe(viewLifecycleOwner) { tip ->
            tip?.let {
                isEditMode = true
                binding.toolbar.title = getString(R.string.edit)
                binding.shareTipButton.text = getString(R.string.save_changes)

                // Populate fields
                binding.titleInput.setText(it.title)
                binding.descriptionInput.setText(it.description)
                // TODO: Load image
            }
        }

        // Observe image upload completion
        viewModel.imageUrl.observe(viewLifecycleOwner) { imageUrl ->
            // If there's a pending tip creation and image upload is complete
            if (imageUrl != null && pendingTipCreation != null) {
                val (title, description) = pendingTipCreation!!
                if (isEditMode && args.tipId != null) {
                    viewModel.updateTip(args.tipId!!, title, description)
                } else {
                    viewModel.createTip(title, description)
                }
                pendingTipCreation = null
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.shareTipButton.isEnabled = !isLoading
        }

        // Observe save success
        viewModel.tipSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                val message = if (isEditMode) {
                    getString(R.string.tip_updated)
                } else {
                    getString(R.string.tip_created)
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                selectedImageUri = null  // Clear selected image
                viewModel.resetImageState()  // Reset image state in ViewModel
                findNavController().navigateUp()
                viewModel.resetSavedState()
            }
        }

        // Observe errors
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupClickListeners() {
        binding.imageUploadCard.setOnClickListener {
            // Open image picker
            imagePickerLauncher.launch("image/*")
        }

        binding.shareTipButton.setOnClickListener {
            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()

            // If image is selected, upload it first, then create/update tip
            if (selectedImageUri != null) {
                pendingTipCreation = title to description
                viewModel.uploadImage(selectedImageUri!!)
            } else {
                // No image, create/update tip directly
                if (isEditMode && args.tipId != null) {
                    viewModel.updateTip(args.tipId!!, title, description)
                } else {
                    viewModel.createTip(title, description)
                }
            }
        }
    }

    private fun displaySelectedImage(uri: Uri) {
        // Display image in ImageView
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_image)
            .into(binding.tipImagePreview)

        // Show image container, hide placeholder
        binding.tipImagePreview.isVisible = true
        binding.uploadPlaceholder.isVisible = false
    }

    private fun loadTipIfEditing() {
        args.tipId?.let { tipId ->
            viewModel.loadTip(tipId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
