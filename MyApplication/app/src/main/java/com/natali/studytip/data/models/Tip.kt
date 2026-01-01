package com.natali.studytip.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tip(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val authorId: String,
    val authorName: String,
    val authorPhotoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
) : Parcelable
