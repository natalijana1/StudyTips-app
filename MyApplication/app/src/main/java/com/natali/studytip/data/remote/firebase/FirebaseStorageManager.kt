package com.natali.studytip.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageManager(
    private val storageBucketUrl: String? = null
) {
    private val storage: FirebaseStorage = if (storageBucketUrl != null) {
        android.util.Log.d("FirebaseStorageManager", "Using custom bucket: $storageBucketUrl")
        FirebaseStorage.getInstance(storageBucketUrl)
    } else {
        android.util.Log.d("FirebaseStorageManager", "Using default Firebase Storage instance")
        FirebaseStorage.getInstance()
    }

    companion object {
        private const val TIP_IMAGES_PATH = "tip_images"
        private const val PROFILE_IMAGES_PATH = "profile_images"
    }

    /**
     * Upload tip image to Firebase Storage
     * @param uri Local image URI
     * @param userId User ID (for organizing images)
     * @return Download URL of uploaded image
     */
    suspend fun uploadTipImage(uri: Uri, userId: String): Result<String> {
        return try {
            android.util.Log.d("FirebaseStorageManager", "=== Starting Tip Image Upload ===")
            android.util.Log.d("FirebaseStorageManager", "URI: $uri")
            android.util.Log.d("FirebaseStorageManager", "UserId: $userId")

            val fileName = "${UUID.randomUUID()}.jpg"
            val path = "$TIP_IMAGES_PATH/$userId/$fileName"
            android.util.Log.d("FirebaseStorageManager", "Full storage path: $path")

            val storageRef = storage.reference
                .child(TIP_IMAGES_PATH)
                .child(userId)
                .child(fileName)

            android.util.Log.d("FirebaseStorageManager", "Storage reference created: ${storageRef.path}")
            android.util.Log.d("FirebaseStorageManager", "Storage bucket: ${storageRef.bucket}")

            // Upload the file
            android.util.Log.d("FirebaseStorageManager", "Starting upload...")
            val uploadTaskSnapshot = storageRef.putFile(uri).await()

            android.util.Log.d("FirebaseStorageManager", "Upload completed!")
            android.util.Log.d("FirebaseStorageManager", "Bytes transferred: ${uploadTaskSnapshot.bytesTransferred}")
            android.util.Log.d("FirebaseStorageManager", "Total bytes: ${uploadTaskSnapshot.totalByteCount}")
            android.util.Log.d("FirebaseStorageManager", "Upload metadata path: ${uploadTaskSnapshot.metadata?.path}")

            // Get download URL
            android.util.Log.d("FirebaseStorageManager", "Getting download URL...")
            val downloadUrl = storageRef.downloadUrl.await()
            android.util.Log.d("FirebaseStorageManager", "=== Upload SUCCESS ===")
            android.util.Log.d("FirebaseStorageManager", "Download URL: $downloadUrl")

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            android.util.Log.e("FirebaseStorageManager", "=== Upload FAILED ===")
            android.util.Log.e("FirebaseStorageManager", "Exception type: ${e.javaClass.name}")
            android.util.Log.e("FirebaseStorageManager", "Error message: ${e.message}")
            android.util.Log.e("FirebaseStorageManager", "Stack trace:", e)
            Result.failure(e)
        }
    }

    /**
     * Upload profile image to Firebase Storage
     * @param uri Local image URI
     * @param userId User ID
     * @return Download URL of uploaded image
     */
    suspend fun uploadProfileImage(uri: Uri, userId: String): Result<String> {
        return try {
            android.util.Log.d("FirebaseStorageManager", "Uploading profile image - URI: $uri, UserId: $userId")

            val fileName = "$userId.jpg"
            val path = "$PROFILE_IMAGES_PATH/$fileName"
            val storageRef = storage.reference
                .child(PROFILE_IMAGES_PATH)
                .child(fileName)

            android.util.Log.d("FirebaseStorageManager", "Storage path: $path")

            val uploadTask = storageRef.putFile(uri).await()
            android.util.Log.d("FirebaseStorageManager", "Upload task completed, getting download URL")

            // Get download URL from the storage reference, not from the upload task
            val downloadUrl = storageRef.downloadUrl.await()
            android.util.Log.d("FirebaseStorageManager", "Profile upload successful - Download URL: $downloadUrl")

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            android.util.Log.e("FirebaseStorageManager", "Profile upload failed: ${e.javaClass.simpleName}: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete tip image from Firebase Storage
     * @param imageUrl Full download URL of the image
     */
    suspend fun deleteTipImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete profile image from Firebase Storage
     * @param imageUrl Full download URL of the image
     */
    suspend fun deleteProfileImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
