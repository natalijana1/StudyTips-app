package com.natali.studytip.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tips")
data class TipEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val localImagePath: String?,  // For offline caching
    val authorId: String,
    val authorName: String,
    val authorPhotoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = true,  // Manual sync tracking
    val isDeleted: Boolean = false // Soft delete for sync
)
