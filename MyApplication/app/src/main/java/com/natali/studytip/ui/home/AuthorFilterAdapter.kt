package com.natali.studytip.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.natali.studytip.R
import com.natali.studytip.data.models.Author
import com.natali.studytip.databinding.ItemAuthorFilterBinding

class AuthorFilterAdapter(
    private val selectedAuthorId: String?,
    private val onAuthorClick: (Author) -> Unit
) : ListAdapter<Author, AuthorFilterAdapter.AuthorViewHolder>(AuthorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewHolder {
        val binding = ItemAuthorFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AuthorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AuthorViewHolder, position: Int) {
        val author = getItem(position)
        val isSelected = author.id == selectedAuthorId
        holder.bind(author, isSelected, onAuthorClick)
    }

    class AuthorViewHolder(
        private val binding: ItemAuthorFilterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(author: Author, isSelected: Boolean, onAuthorClick: (Author) -> Unit) {
            binding.authorName.text = author.name

            // Handle "All Authors" special case
            if (author.isAllAuthors()) {
                binding.authorAvatar.isVisible = false
                binding.allAuthorsIcon.isVisible = true
            } else {
                binding.allAuthorsIcon.isVisible = false
                binding.authorAvatar.isVisible = true

                // Load avatar with Glide
                if (author.photoUrl != null) {
                    Glide.with(binding.authorAvatar.context)
                        .load(author.photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.authorAvatar)
                } else {
                    Glide.with(binding.authorAvatar.context)
                        .load(R.drawable.ic_person)
                        .circleCrop()
                        .into(binding.authorAvatar)
                }
            }

            // Apply selection background
            if (isSelected) {
                binding.authorItemRoot.setBackgroundResource(R.drawable.author_item_selected_bg)
            } else {
                binding.authorItemRoot.background = null
            }

            binding.root.setOnClickListener {
                onAuthorClick(author)
            }
        }
    }

    class AuthorDiffCallback : DiffUtil.ItemCallback<Author>() {
        override fun areItemsTheSame(oldItem: Author, newItem: Author): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Author, newItem: Author): Boolean {
            return oldItem == newItem
        }
    }
}
