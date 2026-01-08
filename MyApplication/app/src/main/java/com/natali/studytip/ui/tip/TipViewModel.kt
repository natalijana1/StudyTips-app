package com.natali.studytip.ui.tip

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.natali.studytip.data.models.Tip
import com.natali.studytip.data.repository.TipRepository
import com.natali.studytip.data.repository.UserRepository
import com.natali.studytip.data.remote.firebase.FirebaseStorageManager
import kotlinx.coroutines.launch

class TipViewModel(
    private val tipRepository: TipRepository,
    private val userRepository: UserRepository,
    private val storageManager: FirebaseStorageManager? = null
) : ViewModel() {

    // Current tip being edited (null for new tip)
    private val _currentTip = MutableLiveData<Tip?>()
    val currentTip: LiveData<Tip?> = _currentTip

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Success event
    private val _tipSaved = MutableLiveData<Boolean>()
    val tipSaved: LiveData<Boolean> = _tipSaved

    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Image path (local)
    private val _imagePath = MutableLiveData<String?>()
    val imagePath: LiveData<String?> = _imagePath

    // Image upload URL (remote)
    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: LiveData<String?> = _imageUrl

    // Load tip for editing
    fun loadTip(tipId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tip = tipRepository.getTipById(tipId)
                _currentTip.value = tip
                _imagePath.value = tip?.imageUrl
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load tip: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Set image path
    fun setImagePath(path: String?) {
        _imagePath.value = path
    }

    // Upload image to Firebase Storage
    fun uploadImage(imageUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = userRepository.getCurrentUserSync()
                if (currentUser == null) {
                    _errorMessage.value = "User not logged in"
                    _isLoading.value = false
                    return@launch
                }

                if (storageManager != null) {
                    // Upload to Firebase Storage
                    android.util.Log.d("TipViewModel", "Starting image upload for user: ${currentUser.id}")
                    val result = storageManager.uploadTipImage(imageUri, currentUser.id)
                    if (result.isSuccess) {
                        val downloadUrl = result.getOrNull()
                        android.util.Log.d("TipViewModel", "Image upload successful: $downloadUrl")
                        _imageUrl.value = downloadUrl
                    } else {
                        val exception = result.exceptionOrNull()
                        android.util.Log.e("TipViewModel", "Image upload failed", exception)
                        _errorMessage.value = "Failed to upload image: ${exception?.message ?: "Unknown error"}"
                    }
                } else {
                    android.util.Log.w("TipViewModel", "No storage manager available, saving locally")
                    // No storage manager, just save locally
                    _imagePath.value = imageUri.toString()
                }
            } catch (e: Exception) {
                android.util.Log.e("TipViewModel", "Exception during image upload", e)
                _errorMessage.value = "Failed to upload image: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Create a new tip
    fun createTip(title: String, description: String) {
        if (!validateInput(title, description)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get current user
                val currentUser = userRepository.getCurrentUserSync()

                // Validate user has required data
                if (currentUser == null || currentUser.id.isBlank() || currentUser.name.isBlank()) {
                    _errorMessage.value = "User profile incomplete. Please log out and log back in."
                    _isLoading.value = false
                    return@launch
                }

                // Create tip
                val result = tipRepository.createTip(
                    title = title.trim(),
                    description = description.trim(),
                    imageUrl = _imageUrl.value,
                    localImagePath = _imagePath.value,
                    authorId = currentUser.id,
                    authorName = currentUser.name,
                    authorPhotoUrl = currentUser.photoUrl
                )

                if (result.isSuccess) {
                    _tipSaved.value = true
                    _errorMessage.value = null
                    _imageUrl.value = null  // Reset image URL after successful creation
                } else {
                    _errorMessage.value = "Failed to create tip: ${result.exceptionOrNull()?.message}"
                    _tipSaved.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create tip: ${e.message}"
                _tipSaved.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update existing tip
    fun updateTip(tipId: String, title: String, description: String) {
        if (!validateInput(title, description)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = tipRepository.updateTip(
                    tipId = tipId,
                    title = title.trim(),
                    description = description.trim(),
                    imageUrl = _imageUrl.value,
                    localImagePath = _imagePath.value
                )

                if (result.isSuccess) {
                    _tipSaved.value = true
                    _errorMessage.value = null
                    _imageUrl.value = null  // Reset image URL after successful update
                } else {
                    _errorMessage.value = "Failed to update tip: ${result.exceptionOrNull()?.message}"
                    _tipSaved.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update tip: ${e.message}"
                _tipSaved.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete tip
    fun deleteTip(tipId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = tipRepository.deleteTip(tipId)
                if (result.isSuccess) {
                    _tipSaved.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Failed to delete tip: ${result.exceptionOrNull()?.message}"
                    _tipSaved.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete tip: ${e.message}"
                _tipSaved.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Validate input
    private fun validateInput(title: String, description: String): Boolean {
        return when {
            title.isBlank() -> {
                _errorMessage.value = "Title cannot be empty"
                false
            }
            description.isBlank() -> {
                _errorMessage.value = "Description cannot be empty"
                false
            }
            description.length > 500 -> {
                _errorMessage.value = "Description must be 500 characters or less"
                false
            }
            else -> true
        }
    }

    // Clear error
    fun clearError() {
        _errorMessage.value = null
    }

    // Reset saved state
    fun resetSavedState() {
        _tipSaved.value = false
    }

    // Reset image state for cleanup
    fun resetImageState() {
        _imageUrl.value = null
        _imagePath.value = null
    }
}
