package com.natali.studytip.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.natali.studytip.R
import com.natali.studytip.data.models.Author
import com.natali.studytip.StudyTipsApplication
import com.natali.studytip.databinding.FragmentHomeBinding
import com.natali.studytip.ui.ViewModelFactory

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
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

    private lateinit var tipsAdapter: TipsAdapter
    private var cachedAuthors: List<Author> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSwipeRefreshBehavior()
    }

    private fun setupRecyclerView() {
        // Get current user ID from Firebase Auth
        val app = requireActivity().application as StudyTipsApplication
        val currentUserId = app.firebaseAuthManager.getCurrentUserId()

        tipsAdapter = TipsAdapter(
            currentUserId = currentUserId,
            onTipClick = { tip ->
                // TODO: Navigate to tip details if needed
            },
            onEditClick = { tip ->
                // Navigate to edit tip screen
                val action = HomeFragmentDirections.actionHomeToCreateTip(tip.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { tip ->
                // Show delete confirmation dialog
                showDeleteConfirmation(tip.id)
            }
        )

        binding.tipsRecyclerView.apply {
            adapter = tipsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        // Observe filtered tips
        viewModel.filteredTips.observe(viewLifecycleOwner) { tips ->
            tipsAdapter.submitList(tips)

            // Show empty state if no tips
            binding.emptyState.isVisible = tips.isEmpty()
            binding.tipsRecyclerView.isVisible = tips.isNotEmpty()
        }

        // Observe quote
        viewModel.quote.observe(viewLifecycleOwner) { quote ->
            if (quote != null) {
                binding.quoteText.text = "\"${quote.text}\""
                binding.quoteAuthor.text = "- ${quote.author}"
            } else {
                binding.quoteText.text = getString(R.string.loading_quote)
                binding.quoteAuthor.text = ""
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            // Stop swipe refresh animation when loading is done
            if (!isLoading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        // Observe errors
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        // Observe filter state for badge and filter bar
        viewModel.isFilterActive.observe(viewLifecycleOwner) { isActive ->
            binding.filterBadge.isVisible = isActive
            binding.filterBar.isVisible = isActive
        }

        // Observe selected author for filter bar content
        viewModel.selectedAuthor.observe(viewLifecycleOwner) { author ->
            author?.let {
                binding.filterText.text = getString(R.string.showing_tips_by, it.name)

                // Load author avatar
                if (it.photoUrl != null) {
                    Glide.with(this)
                        .load(it.photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.filterAuthorAvatar)
                } else {
                    Glide.with(this)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.filterAuthorAvatar)
                }
            }
        }

        // Observe authors list to cache it for filter bottom sheet
        viewModel.authors.observe(viewLifecycleOwner) { authors ->
            cachedAuthors = authors
            android.util.Log.d("HomeFragment", "Authors updated: ${authors.size} authors")
        }
    }

    private fun setupClickListeners() {
        binding.refreshQuote.setOnClickListener {
            // detailed instruction: Animate the refresh button
            it.animate().rotationBy(360f).setDuration(500).start()
            Toast.makeText(requireContext(), "Refreshing quote...", Toast.LENGTH_SHORT).show()
            viewModel.refreshQuote()
        }

        binding.filterButton.setOnClickListener {
            showFilterBottomSheet()
        }

        binding.clearFilterButton.setOnClickListener {
            viewModel.clearFilter()
        }

        // Setup pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.syncTips()
            viewModel.refreshQuote()
        }
    }

    private fun setupSwipeRefreshBehavior() {
        binding.appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                // Enable refresh only when AppBarLayout is fully expanded (quote visible)
                binding.swipeRefresh.isEnabled = (verticalOffset == 0)
            }
        )
    }

    private fun showFilterBottomSheet() {
        android.util.Log.d("HomeFragment", "Opening filter with ${cachedAuthors.size} cached authors")

        val filterBottomSheet = FilterBottomSheet(
            authors = cachedAuthors,
            selectedAuthorId = viewModel.selectedAuthorId.value,
            onAuthorSelected = { author ->
                if (author.isAllAuthors()) {
                    viewModel.clearFilter()
                } else {
                    viewModel.filterByAuthor(author.id)
                }
            }
        )

        filterBottomSheet.show(childFragmentManager, "FilterBottomSheet")
    }

    private fun showDeleteConfirmation(tipId: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_tip))
            .setMessage(getString(R.string.delete_tip_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                // TODO: Delete via TipViewModel
                Toast.makeText(requireContext(), "Delete feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
