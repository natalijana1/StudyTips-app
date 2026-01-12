package com.natali.studytip.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageHelper {

    /**
     * Compress image to reduce file size
     * @param context Context
     * @param imageUri Source image URI
     * @param quality Compression quality (0-100)
     * @return Compressed image file
     */
    fun compressImage(context: Context, imageUri: Uri, quality: Int = 80): File? {
        return try {
            // Decode the image
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Create temp file
            val tempFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)

            // Compress and save
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            // Recycle bitmap
            bitmap.recycle()

            tempFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save image to internal storage
     * @param context Context
     * @param imageUri Source image URI
     * @param fileName File name
     * @return Saved file path or null
     */
    fun saveImageLocally(context: Context, imageUri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Create app-specific directory
            val imagesDir = File(context.filesDir, "tip_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }

            val imageFile = File(imagesDir, fileName)
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            bitmap.recycle()

            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete local image file
     * @param filePath Absolute path to file
     */
    fun deleteLocalImage(filePath: String?) {
        if (filePath != null) {
            try {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
