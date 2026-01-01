package com.natali.studytip.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val name: String,
    val email: String,
    val bio: String?,
    val photoUrl: String?,
    val tipsCount: Int = 0
) : Parcelable
