package com.natali.studytip.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val bio: String?,
    val photoUrl: String?,
    val localPhotoPath: String?,
    val tipsCount: Int = 0,
    val lastSyncedAt: Long = 0
)
