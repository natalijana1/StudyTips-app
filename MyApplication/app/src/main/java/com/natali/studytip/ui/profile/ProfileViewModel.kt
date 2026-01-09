package com.natali.studytip.ui.profile

import android.net.Uri
import androidx.lifecycle.*
import com.natali.studytip.data.local.entities.UserEntity
import com.natali.studytip.data.models.Tip
import com.natali.studytip.data.models.User
import com.natali.studytip.data.remote.firebase.FirebaseStorageManager
import com.natali.studytip.data.remote.firebase.FirestoreManager
import com.natali.studytip.data.repository.AuthRepository
import com.natali.studytip.data.repository.TipRepository
import com.natali.studytip.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val tipRepository: TipRepository,
    private val authRepository: AuthRepository,
    private val storageManager: FirebaseStorageManager? = null,
    private val firestoreManager: FirestoreManager? = null
) : ViewModel() {

    // Current user
    val currentUser: LiveData<User?> = userRepository.getCurrentUser()

    // User's tips
    val userTips: LiveData<List<Tip>> = currentUser.switchMap { user ->
        if (user != null) {
            tipRepository.getTipsByAuthorId(user.id)
        } else {
            MutableLiveData(emptyList())
        }
    }

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Profile updated event
    private val _profileUpdated = MutableLiveData<Boolean>()
    val profileUpdated: LiveData<Boolean> = _profileUpdated

    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Profile photo path
    private val _photoPath = MutableLiveData<String?>()
    val photoPath: LiveData<String?> = _photoPath

    init {
        // Create a mock user if none exists (for testing without Firebase)
        viewModelScope.launch {
            val user = userRepository.getCurrentUserSync()
            if (user == null) {
                userRepository.createMockUser()
            }
        }
    }

    // Set photo path
    fun setPhotoPath(path: String?) {
        _photoPath.value = path
    }

    // Upload profile photo
    fun uploadProfilePhoto(imageUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserValue = userRepository.getCurrentUserSync()
                if (currentUserValue == null) {
                    _errorMessage.value = "User not logged in"
                    return@launch
                }

                if (storageManager != null) {
                    // Step 1: Upload to Firebase Storage
                    val result = storageManager.uploadProfileImage(imageUri, currentUserValue.id)
                    if (result.isSuccess) {
                        val photoUrl = result.getOrNull()

                        // Step 2: Update Room database
                        userRepository.updateProfile(
                            userId = currentUserValue.id,
                            name = currentUserValue.name,
                            bio = currentUserValue.bio,
                            photoUrl = photoUrl,
                            localPhotoPath = null
                        )

                        // Step 3: Sync to Firestore - NEW CRITICAL STEP
                        if (firestoreManager != null) {
                            val updatedUser = userRepository.getCurrentUserSync()
                            if (updatedUser != null) {
                                val userEntity = UserEntity(
                                    id = updatedUser.id,
                                    name = updatedUser.name,
                                    email = updatedUser.email,
                                    bio = updatedUser.bio,
                                    photoUrl = photoUrl,
                                    localPhotoPath = null,
                                    tipsCount = updatedUser.tipsCount,
                                    lastSyncedAt = System.currentTimeMillis()
                                )
                                val firestoreResult = firestoreManager.saveUser(userEntity)
                                if (firestoreResult.isFailure) {
                                    android.util.Log.e("ProfileViewModel",
                                        "Failed to sync photo to Firestore",
                                        firestoreResult.exceptionOrNull())
                                }
                            }
                        }

                        _profileUpdated.value = true
                        _errorMessage.value = null
                    } else {
                        _errorMessage.value = "Failed to upload profile photo"
                    }
                } else {
                    // Fallback: save locally only
                    _photoPath.value = imageUri.toString()
                    userRepository.updateProfile(
                        userId = currentUserValue.id,
                        name = currentUserValue.name,
                        bio = currentUserValue.bio,
                        photoUrl = null,
                        localPhotoPath = imageUri.toString()
                    )
                    _profileUpdated.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to upload profile photo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update profile
    fun updateProfile(name: String, bio: String?) {
        if (name.isBlank()) {
            _errorMessage.value = "Name cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = userRepository.getCurrentUserSync()
                if (user == null) {
                    _errorMessage.value = "User not found"
                    return@launch
                }

                // Update Room database
                userRepository.updateProfile(
                    userId = user.id,
                    name = name.trim(),
                    bio = bio?.trim(),
                    photoUrl = user.photoUrl,  // Preserve existing photo
                    localPhotoPath = _photoPath.value
                )

                // Sync to Firestore - NEW STEP
                if (firestoreManager != null) {
                    val updatedUser = userRepository.getCurrentUserSync()
                    if (updatedUser != null) {
                        val userEntity = UserEntity(
                            id = updatedUser.id,
                            name = updatedUser.name,
                            email = updatedUser.email,
                            bio = updatedUser.bio,
                            photoUrl = updatedUser.photoUrl,
                            localPhotoPath = _photoPath.value,
                            tipsCount = updatedUser.tipsCount,
                            lastSyncedAt = System.currentTimeMillis()
                        )
                        val firestoreResult = firestoreManager.saveUser(userEntity)
                        if (firestoreResult.isFailure) {
                            android.util.Log.e("ProfileViewModel",
                                "Failed to sync profile to Firestore",
                                firestoreResult.exceptionOrNull())
                        }
                    }
                }

                _profileUpdated.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update profile: ${e.message}"
                _profileUpdated.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update tip
    fun updateTip(tipId: String, title: String, description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = tipRepository.updateTip(
                    tipId = tipId,
                    title = title,
                    description = description,
                    imageUrl = null,
                    localImagePath = null
                )

                if (result.isSuccess) {
                    _profileUpdated.value = true
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update tip"
                    _profileUpdated.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update tip: ${e.message}"
                _profileUpdated.value = false
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
                tipRepository.deleteTip(tipId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete tip: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Logout success event
    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    // Logout (clear all user data and sign out from Firebase)
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sign out from Firebase
                authRepository.signOut()
                // Clear local data
                userRepository.deleteAllUsers()
                _logoutSuccess.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to logout: ${e.message}"
                _logoutSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clear error
    fun clearError() {
        _errorMessage.value = null
    }

    // Reset updated state
    fun resetUpdatedState() {
        _profileUpdated.value = false
    }
}
