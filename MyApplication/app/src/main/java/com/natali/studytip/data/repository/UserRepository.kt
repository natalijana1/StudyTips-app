package com.natali.studytip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.natali.studytip.data.local.dao.UserDao
import com.natali.studytip.data.local.entities.UserEntity
import com.natali.studytip.data.models.User

class UserRepository(private val userDao: UserDao) {

    // Get current user as LiveData
    fun getCurrentUser(): LiveData<User?> {
        return userDao.getCurrentUser().map { entity ->
            entity?.toUser()
        }
    }

    // Get user by ID as LiveData
    fun getUserById(userId: String): LiveData<User?> {
        return userDao.getUserById(userId).map { entity ->
            entity?.toUser()
        }
    }

    // Get current user synchronously
    suspend fun getCurrentUserSync(): User? {
        return userDao.getCurrentUserSync()?.toUser()
    }

    // Create or update user
    suspend fun saveUser(
        id: String,
        name: String,
        email: String,
        bio: String?,
        photoUrl: String?,
        localPhotoPath: String?
    ) {
        val userEntity = UserEntity(
            id = id,
            name = name,
            email = email,
            bio = bio,
            photoUrl = photoUrl,
            localPhotoPath = localPhotoPath,
            tipsCount = 0,
            lastSyncedAt = System.currentTimeMillis()
        )
        userDao.insertUser(userEntity)
    }

    // Update user profile
    suspend fun updateProfile(
        userId: String,
        name: String,
        bio: String?,
        photoUrl: String?,
        localPhotoPath: String?
    ) {
        val existing = userDao.getUserByIdSync(userId) ?: return

        val updated = existing.copy(
            name = name,
            bio = bio,
            photoUrl = photoUrl,
            localPhotoPath = localPhotoPath,
            lastSyncedAt = System.currentTimeMillis()
        )

        userDao.updateUser(updated)
    }

    // Update tips count
    suspend fun updateTipsCount(userId: String, count: Int) {
        val existing = userDao.getUserByIdSync(userId) ?: return
        userDao.updateUser(existing.copy(tipsCount = count))
    }

    // Delete user (for logout)
    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    // Delete all users (for logout)
    suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }

    // Create a mock user for testing (before Firebase is enabled)
    suspend fun createMockUser() {
        val mockUser = UserEntity(
            id = "mock-user-123",
            name = "Test User",
            email = "test@studytips.com",
            bio = "Love sharing study tips!",
            photoUrl = null,
            localPhotoPath = null,
            tipsCount = 0,
            lastSyncedAt = System.currentTimeMillis()
        )
        userDao.insertUser(mockUser)
    }

    // Mapper function: UserEntity -> User
    private fun UserEntity.toUser() = User(
        id = id,
        name = name,
        email = email,
        bio = bio,
        photoUrl = photoUrl ?: localPhotoPath,  // Use local path if no remote URL
        tipsCount = tipsCount
    )
}
