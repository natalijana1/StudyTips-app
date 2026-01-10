package com.natali.studytip.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.natali.studytip.R
import com.natali.studytip.databinding.BottomSheetEditTipBinding

class EditTipBottomSheet(
    private val tipId: String,
    private val currentTitle: String,
    private val currentDescription: String,
    private val onSave: (tipId: String, title: String, description: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditTipBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditTipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill fields
        binding.titleInput.setText(currentTitle)
        binding.descriptionInput.setText(currentDescription)

        // Setup buttons
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            val title = binding.titleInput.text.toString().trim()
            val description = binding.descriptionInput.text.toString().trim()

            // Validate title
            if (title.isEmpty()) {
                binding.titleInputLayout.error = getString(R.string.title_required)
                return@setOnClickListener
            }

            // Validate description
            if (description.isEmpty()) {
                binding.descriptionInputLayout.error = getString(R.string.description_required)
                return@setOnClickListener
            }

            // Clear errors
            binding.titleInputLayout.error = null
            binding.descriptionInputLayout.error = null

            // Call the save callback
            onSave(tipId, title, description)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditTipBottomSheet"
    }
}
