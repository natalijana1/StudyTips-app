package com.natali.studytip.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Author(
    val id: String,
    val name: String,
    val photoUrl: String?
) : Parcelable {
    companion object {
        const val ALL_AUTHORS_ID = "__ALL_AUTHORS__"

        fun createAllAuthorsOption() = Author(
            id = ALL_AUTHORS_ID,
            name = "All Authors",
            photoUrl = null
        )
    }

    fun isAllAuthors(): Boolean = id == ALL_AUTHORS_ID
}
