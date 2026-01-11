package com.natali.studytip.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.natali.studytip.data.models.Author
import com.natali.studytip.databinding.BottomSheetFilterBinding

class FilterBottomSheet(
    private val authors: List<Author>,
    private val selectedAuthorId: String?,
    private val onAuthorSelected: (Author) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup authors list with "All Authors" option at top
        val adapter = AuthorFilterAdapter(
            selectedAuthorId = selectedAuthorId,
            onAuthorClick = { author ->
                onAuthorSelected(author)
                dismiss()
            }
        )

        binding.authorsRecyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Prepend "All Authors" option to the list
        val authorsWithAllOption = listOf(Author.createAllAuthorsOption()) + authors
        adapter.submitList(authorsWithAllOption)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
