package com.natali.studytip.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.natali.studytip.R
import com.natali.studytip.databinding.BottomSheetEditProfileBinding

class EditProfileBottomSheet(
    private val currentName: String,
    private val currentBio: String?,
    private val onSave: (name: String, bio: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill fields
        binding.nameInput.setText(currentName)
        binding.bioInput.setText(currentBio ?: "")

        // Setup buttons
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val bio = binding.bioInput.text.toString().trim()

            // Validate name
            if (name.isEmpty()) {
                binding.nameInputLayout.error = getString(R.string.name_required)
                return@setOnClickListener
            }

            // Clear error
            binding.nameInputLayout.error = null

            // Call the save callback
            onSave(name, bio)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditProfileBottomSheet"
    }
}
