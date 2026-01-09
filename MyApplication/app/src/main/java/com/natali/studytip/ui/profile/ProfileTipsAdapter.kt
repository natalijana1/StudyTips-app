package com.natali.studytip.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.natali.studytip.R
import com.natali.studytip.data.models.Tip
import com.natali.studytip.databinding.ItemProfileTipBinding
import android.content.res.Resources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class ProfileTipsAdapter(
    private val onEditClick: (Tip) -> Unit,
    private val onDeleteClick: (Tip) -> Unit
) : ListAdapter<Tip, ProfileTipsAdapter.TipViewHolder>(TipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val binding = ItemProfileTipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TipViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TipViewHolder(
        private val binding: ItemProfileTipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tip: Tip) {
            binding.apply {
                // Tip title and description
                tipTitle.text = tip.title
                tipDescription.text = tip.description

                // Author info
                authorName.text = tip.authorName

                // Load author avatar
                if (tip.authorPhotoUrl != null) {
                    Glide.with(authorAvatar)
                        .load(tip.authorPhotoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(authorAvatar)
                } else {
                    authorAvatar.setImageResource(R.drawable.ic_person)
                }

                // Load tip image and adjust constraints
                android.util.Log.d("ProfileTipsAdapter", "Tip: '${tip.title}', imageUrl='${tip.imageUrl}'")

                if (tip.imageUrl != null && tip.imageUrl.isNotBlank()) {
                    tipImage.isVisible = true

                    // Get ConstraintLayout from parent
                    val parent = tipImage.parent
                    android.util.Log.d("ProfileTipsAdapter", "WITH image - tipImage.parent type: ${parent?.javaClass?.simpleName}")
                    (parent as? ConstraintLayout)?.let { constraintLayout ->
                        android.util.Log.d("ProfileTipsAdapter", "ConstraintLayout found, updating constraints")
                        updateAuthorAvatarConstraints(
                            constrainToImage = true,
                            constraintLayout = constraintLayout
                        )
                    } ?: android.util.Log.w("ProfileTipsAdapter", "ConstraintLayout NOT found for image")

                    Glide.with(tipImage)
                        .load(tip.imageUrl)
                        .placeholder(R.color.background)
                        .into(tipImage)
                } else {
                    tipImage.isVisible = false

                    // Get ConstraintLayout from parent
                    val parent = authorAvatar.parent
                    android.util.Log.d("ProfileTipsAdapter", "WITHOUT image - authorAvatar.parent type: ${parent?.javaClass?.simpleName}")
                    (parent as? ConstraintLayout)?.let { constraintLayout ->
                        android.util.Log.d("ProfileTipsAdapter", "ConstraintLayout found, updating constraints")
                        updateAuthorAvatarConstraints(
                            constrainToImage = false,
                            constraintLayout = constraintLayout
                        )
                    } ?: android.util.Log.w("ProfileTipsAdapter", "ConstraintLayout NOT found for no image")
                }

                // Floating action buttons - always visible for profile tips
                editButton.setOnClickListener {
                    onEditClick(tip)
                }

                deleteButton.setOnClickListener {
                    onDeleteClick(tip)
                }
            }
        }

        private fun updateAuthorAvatarConstraints(
            constrainToImage: Boolean,
            constraintLayout: ConstraintLayout
        ) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            if (constrainToImage) {
                // Connect author_avatar top to tip_image bottom with 12dp margin
                constraintSet.connect(
                    binding.authorAvatar.id,
                    ConstraintSet.TOP,
                    binding.tipImage.id,
                    ConstraintSet.BOTTOM,
                    12.dpToPx()
                )
            } else {
                // Connect author_avatar top to parent top with 0dp margin
                constraintSet.connect(
                    binding.authorAvatar.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    0
                )
            }

            constraintSet.applyTo(constraintLayout)
        }
    }

    class TipDiffCallback : DiffUtil.ItemCallback<Tip>() {
        override fun areItemsTheSame(oldItem: Tip, newItem: Tip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tip, newItem: Tip): Boolean {
            return oldItem == newItem
        }
    }

    // Helper extension function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (this * density).toInt()
    }
}
