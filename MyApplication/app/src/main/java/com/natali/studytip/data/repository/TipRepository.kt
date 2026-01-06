package com.natali.studytip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.natali.studytip.data.local.dao.TipDao
import com.natali.studytip.data.local.entities.TipEntity
import com.natali.studytip.data.models.Tip
import com.natali.studytip.data.remote.firebase.FirestoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class TipRepository(
    private val tipDao: TipDao,
    private val firestoreManager: FirestoreManager? = null,
    private val userRepository: UserRepository? = null
) {

    // Get all tips as LiveData
    fun getAllTips(): LiveData<List<Tip>> {
        return tipDao.getAllTips().map { entities ->
            entities.map { it.toTip() }
        }
    }

    // Get tips by author
    fun getTipsByAuthorId(authorId: String): LiveData<List<Tip>> {
        return tipDao.getTipsByAuthorId(authorId).map { entities ->
            entities.map { it.toTip() }
        }
    }

    // Get tip by ID
    suspend fun getTipById(tipId: String): Tip? {
        return tipDao.getTipById(tipId)?.toTip()
    }

    // Create a new tip with Firestore sync
    suspend fun createTip(
        title: String,
        description: String,
        imageUrl: String?,
        localImagePath: String?,
        authorId: String,
        authorName: String,
        authorPhotoUrl: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val tipId = UUID.randomUUID().toString()
            val currentTime = System.currentTimeMillis()

            val tipEntity = TipEntity(
                id = tipId,
                title = title,
                description = description,
                imageUrl = imageUrl,
                localImagePath = localImagePath,
                authorId = authorId,
                authorName = authorName,
                authorPhotoUrl = authorPhotoUrl,
                createdAt = currentTime,
                updatedAt = currentTime,
                isSynced = firestoreManager == null  // If no Firestore, mark as synced
            )

            // Save to local Room database
            tipDao.insertTip(tipEntity)

            // Sync to Firestore if available
            if (firestoreManager != null) {
                val syncResult = firestoreManager.saveTip(tipEntity)
                if (syncResult.isSuccess) {
                    // Mark as synced
                    tipDao.updateTip(tipEntity.copy(isSynced = true))
                }
            }

            Result.success(tipId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update a tip with Firestore sync
    suspend fun updateTip(
        tipId: String,
        title: String,
        description: String,
        imageUrl: String?,
        localImagePath: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = tipDao.getTipById(tipId) ?: return@withContext Result.failure(Exception("Tip not found"))

            val updated = existing.copy(
                title = title,
                description = description,
                imageUrl = imageUrl,
                localImagePath = localImagePath,
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )

            // Update in local Room database
            tipDao.updateTip(updated)

            // Sync to Firestore if available
            if (firestoreManager != null) {
                val syncResult = firestoreManager.saveTip(updated)
                if (syncResult.isSuccess) {
                    tipDao.updateTip(updated.copy(isSynced = true))
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete a tip with Firestore sync
    suspend fun deleteTip(tipId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Soft delete in Room
            tipDao.softDeleteTip(tipId)

            // Delete from Firestore if available
            firestoreManager?.deleteTip(tipId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sync tips from Firestore to Room
    suspend fun syncTipsFromFirestore(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (firestoreManager == null) {
                return@withContext Result.failure(Exception("Firestore not initialized"))
            }

            val result = firestoreManager.getAllTips()
            if (result.isSuccess) {
                val tips = result.getOrThrow()
                tipDao.insertTips(tips)
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to sync tips"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sync unsynced tips to Firestore
    suspend fun syncUnsyncedTipsToFirestore(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (firestoreManager == null) {
                return@withContext Result.failure(Exception("Firestore not initialized"))
            }

            val unsyncedTips = tipDao.getUnsyncedTips()
            for (tip in unsyncedTips) {
                val result = firestoreManager.saveTip(tip)
                if (result.isSuccess) {
                    tipDao.updateTip(tip.copy(isSynced = true))
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fix missing author data by populating with current user info
    suspend fun fixMissingAuthorData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Get current user
            val currentUser = userRepository?.getCurrentUserSync()
            if (currentUser == null) {
                android.util.Log.w("TipRepository", "Cannot fix author data: No current user")
                return@withContext Result.failure(Exception("No current user"))
            }

            android.util.Log.d("TipRepository", "Current user: ${currentUser.name} (${currentUser.id})")

            // Get all tips
            val allTips = tipDao.getAllTipsSync()
            android.util.Log.d("TipRepository", "Total tips in database: ${allTips.size}")

            // Log all tip author info
            allTips.forEachIndexed { index, tip ->
                android.util.Log.d("TipRepository", "Tip $index: id=${tip.id}, title='${tip.title}', authorId='${tip.authorId}', authorName='${tip.authorName}', isDeleted=${tip.isDeleted}")
            }

            // Find tips with missing author data
            val tipsToFix = allTips.filter {
                it.authorId.isBlank() || it.authorName.isBlank()
            }

            android.util.Log.d("TipRepository", "Tips needing fix: ${tipsToFix.size}")

            if (tipsToFix.isEmpty()) {
                android.util.Log.d("TipRepository", "No tips need fixing")
                return@withContext Result.success(Unit)
            }

            // Update each tip with current user's data
            tipsToFix.forEach { tip ->
                android.util.Log.d("TipRepository", "Fixing tip: ${tip.id} - '${tip.title}'")
                val fixed = tip.copy(
                    authorId = currentUser.id,
                    authorName = currentUser.name,
                    authorPhotoUrl = currentUser.photoUrl,
                    isSynced = false  // Mark for re-sync to Firestore
                )
                tipDao.updateTip(fixed)
            }

            android.util.Log.d("TipRepository", "Fixed ${tipsToFix.size} tips, syncing to Firestore...")

            // Sync to Firestore
            syncUnsyncedTipsToFirestore()

            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("TipRepository", "Error fixing author data", e)
            Result.failure(e)
        }
    }

    // Insert tips from remote
    suspend fun insertTips(tips: List<TipEntity>) {
        tipDao.insertTips(tips)
    }

    // Get unsynced tips
    suspend fun getUnsyncedTips(): List<TipEntity> {
        return tipDao.getUnsyncedTips()
    }

    // Mapper function: TipEntity -> Tip
    private fun TipEntity.toTip() = Tip(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl ?: localImagePath,
        authorId = authorId,
        authorName = authorName,
        authorPhotoUrl = authorPhotoUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
