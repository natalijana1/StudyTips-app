package com.natali.studytip.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.natali.studytip.R
import com.natali.studytip.data.models.Tip
import com.natali.studytip.databinding.ItemTipBinding
import com.bumptech.glide.Glide
import com.natali.studytip.utils.TimeUtils
import android.content.res.Resources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import java.text.SimpleDateFormat
import java.util.*

class TipsAdapter(
    private val currentUserId: String?,
    private val onTipClick: (Tip) -> Unit = {},
    private val onEditClick: (Tip) -> Unit = {},
    private val onDeleteClick: (Tip) -> Unit = {}
) : ListAdapter<Tip, TipsAdapter.TipViewHolder>(TipDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val binding = ItemTipBinding.inflate(
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
        private val binding: ItemTipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tip: Tip) {
            binding.apply {
                // Tip title and description
                tipTitle.text = tip.title
                tipDescription.text = tip.description

                // Author info
                authorName.text = tip.authorName
                tipTimestamp.text = TimeUtils.getRelativeTimeString(tip.createdAt)

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
                android.util.Log.d("TipsAdapter", "Tip: '${tip.title}', imageUrl='${tip.imageUrl}'")

                if (tip.imageUrl != null && tip.imageUrl.isNotBlank()) {
                    tipImage.isVisible = true

                    // Get ConstraintLayout from parent
                    val parent = tipImage.parent
                    android.util.Log.d("TipsAdapter", "WITH image - tipImage.parent type: ${parent?.javaClass?.simpleName}")
                    (parent as? ConstraintLayout)?.let { constraintLayout ->
                        android.util.Log.d("TipsAdapter", "ConstraintLayout found, updating constraints")
                        updateAuthorAvatarConstraints(
                            constrainToImage = true,
                            constraintLayout = constraintLayout
                        )
                    } ?: android.util.Log.w("TipsAdapter", "ConstraintLayout NOT found for image")

                    Glide.with(tipImage)
                        .load(tip.imageUrl)
                        .placeholder(R.color.background)
                        .into(tipImage)
                } else {
                    tipImage.isVisible = false

                    // Get ConstraintLayout from parent
                    val parent = authorAvatar.parent
                    android.util.Log.d("TipsAdapter", "WITHOUT image - authorAvatar.parent type: ${parent?.javaClass?.simpleName}")
                    (parent as? ConstraintLayout)?.let { constraintLayout ->
                        android.util.Log.d("TipsAdapter", "ConstraintLayout found, updating constraints")
                        updateAuthorAvatarConstraints(
                            constrainToImage = false,
                            constraintLayout = constraintLayout
                        )
                    } ?: android.util.Log.w("TipsAdapter", "ConstraintLayout NOT found for no image")
                }

                // Show menu button only for current user's tips
                val isOwnTip = currentUserId != null && tip.authorId == currentUserId
                menuButton.isVisible = isOwnTip

                // Click listeners
                root.setOnClickListener {
                    onTipClick(tip)
                }

                menuButton.setOnClickListener {
                    showPopupMenu(it, tip)
                }
            }
        }

        private fun showPopupMenu(view: View, tip: Tip) {
            val popup = android.widget.PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.tip_item_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onEditClick(tip)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(tip)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        private fun updateAuthorAvatarConstraints(
            constrainToImage: Boolean,
            constraintLayout: ConstraintLayout
        ) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            if (constrainToImage) {
                // Connect author_avatar top to tip_image bottom with 12dp margin
                android.util.Log.d("TipsAdapter", "Setting constraint: avatar to image bottom with 12dp")
                constraintSet.connect(
                    binding.authorAvatar.id,
                    ConstraintSet.TOP,
                    binding.tipImage.id,
                    ConstraintSet.BOTTOM,
                    12.dpToPx()
                )
            } else {
                // Connect author_avatar top to parent top with 0dp margin
                android.util.Log.d("TipsAdapter", "Setting constraint: avatar to parent top with 0dp")
                constraintSet.connect(
                    binding.authorAvatar.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    0
                )
            }

            constraintSet.applyTo(constraintLayout)
            android.util.Log.d("TipsAdapter", "Constraints applied")
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
