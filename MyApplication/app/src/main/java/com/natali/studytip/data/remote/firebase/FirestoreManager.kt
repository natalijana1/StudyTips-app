package com.natali.studytip.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.natali.studytip.data.local.entities.TipEntity
import com.natali.studytip.data.local.entities.UserEntity
import kotlinx.coroutines.tasks.await

class FirestoreManager {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val TIPS_COLLECTION = "tips"
    }

    // ==================== User Operations ====================

    /**
     * Create or update user in Firestore
     */
    suspend fun saveUser(user: UserEntity): Result<Unit> {
        return try {
            val userMap = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "bio" to (user.bio ?: ""),
                "photoUrl" to (user.photoUrl ?: ""),
                "tipsCount" to user.tipsCount,
                "lastSyncedAt" to System.currentTimeMillis()
            )
            db.collection(USERS_COLLECTION)
                .document(user.id)
                .set(userMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get user from Firestore
     */
    suspend fun getUser(userId: String): Result<UserEntity?> {
        return try {
            val snapshot = db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.success(null)
            }

            val user = UserEntity(
                id = snapshot.id,
                name = snapshot.getString("name") ?: "",
                email = snapshot.getString("email") ?: "",
                bio = snapshot.getString("bio"),
                photoUrl = snapshot.getString("photoUrl"),
                localPhotoPath = null,
                tipsCount = snapshot.getLong("tipsCount")?.toInt() ?: 0,
                lastSyncedAt = snapshot.getLong("lastSyncedAt") ?: 0
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Tip Operations ====================

    /**
     * Create or update tip in Firestore
     */
    suspend fun saveTip(tip: TipEntity): Result<Unit> {
        return try {
            val tipMap = hashMapOf(
                "title" to tip.title,
                "description" to tip.description,
                "imageUrl" to (tip.imageUrl ?: ""),
                "authorId" to tip.authorId,
                "authorName" to tip.authorName,
                "authorPhotoUrl" to (tip.authorPhotoUrl ?: ""),
                "createdAt" to tip.createdAt,
                "updatedAt" to tip.updatedAt
            )
            db.collection(TIPS_COLLECTION)
                .document(tip.id)
                .set(tipMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all tips from Firestore
     */
    suspend fun getAllTips(): Result<List<TipEntity>> {
        return try {
            val snapshot = db.collection(TIPS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val tips = snapshot.documents.mapNotNull { doc ->
                try {
                    TipEntity(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        localImagePath = null,
                        authorId = doc.getString("authorId") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        authorPhotoUrl = doc.getString("authorPhotoUrl"),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                        isSynced = true,
                        isDeleted = false
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(tips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get tips by author
     */
    suspend fun getTipsByAuthor(authorId: String): Result<List<TipEntity>> {
        return try {
            val snapshot = db.collection(TIPS_COLLECTION)
                .whereEqualTo("authorId", authorId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val tips = snapshot.documents.mapNotNull { doc ->
                try {
                    TipEntity(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        localImagePath = null,
                        authorId = doc.getString("authorId") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        authorPhotoUrl = doc.getString("authorPhotoUrl"),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                        isSynced = true,
                        isDeleted = false
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(tips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete tip from Firestore
     */
    suspend fun deleteTip(tipId: String): Result<Unit> {
        return try {
            db.collection(TIPS_COLLECTION)
                .document(tipId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user's tip count
     */
    suspend fun updateUserTipCount(userId: String, newCount: Int): Result<Unit> {
        return try {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update("tipsCount", newCount)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
