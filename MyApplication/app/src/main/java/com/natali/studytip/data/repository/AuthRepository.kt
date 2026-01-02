package com.natali.studytip.data.repository

import com.natali.studytip.data.local.dao.UserDao
import com.natali.studytip.data.local.entities.UserEntity
import com.natali.studytip.data.remote.firebase.FirebaseAuthManager
import com.natali.studytip.data.remote.firebase.FirestoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authManager: FirebaseAuthManager,
    private val firestoreManager: FirestoreManager,
    private val userDao: UserDao
) {

    /**
     * Sign up new user
     * Creates Firebase Auth account, saves user to Firestore and Room
     */
    suspend fun signUp(name: String, email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Create Firebase Auth account
                val authResult = authManager.signUp(email, password, name)
                if (authResult.isFailure) {
                    return@withContext Result.failure(
                        authResult.exceptionOrNull() ?: Exception("Sign up failed")
                    )
                }

                val userId = authResult.getOrThrow()

                // Create user entity
                val user = UserEntity(
                    id = userId,
                    name = name,
                    email = email,
                    bio = null,
                    photoUrl = null,
                    localPhotoPath = null,
                    tipsCount = 0,
                    lastSyncedAt = System.currentTimeMillis()
                )

                // Save to Firestore
                val firestoreResult = firestoreManager.saveUser(user)
                if (firestoreResult.isFailure) {
                    return@withContext Result.failure(
                        firestoreResult.exceptionOrNull() ?: Exception("Failed to save user to Firestore")
                    )
                }

                // Save to local Room database
                userDao.insertUser(user)

                Result.success(userId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Sign in existing user
     * Authenticates with Firebase and syncs user data to Room
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Sign in with Firebase Auth
                val authResult = authManager.signIn(email, password)
                if (authResult.isFailure) {
                    return@withContext Result.failure(
                        authResult.exceptionOrNull() ?: Exception("Sign in failed")
                    )
                }

                val userId = authResult.getOrThrow()

                // Fetch user data from Firestore
                val userResult = firestoreManager.getUser(userId)
                if (userResult.isFailure) {
                    return@withContext Result.failure(
                        userResult.exceptionOrNull() ?: Exception("Failed to fetch user data")
                    )
                }

                val user = userResult.getOrNull()
                if (user != null) {
                    // Save to local Room database
                    userDao.insertUser(user)
                }

                Result.success(userId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Sign out current user
     */
    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                authManager.signOut()
                // Optional: Clear local Room database
                // userDao.deleteAllUsers()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Check if user is currently logged in
     */
    fun isUserLoggedIn(): Boolean {
        return authManager.isUserLoggedIn()
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return authManager.getCurrentUser()?.uid
    }

    /**
     * Get current user display name
     */
    fun getCurrentUserDisplayName(): String? {
        return authManager.getCurrentUser()?.displayName
    }

    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return authManager.getCurrentUser()?.email
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            authManager.sendPasswordResetEmail(email)
        }
    }

    /**
     * Auto-login: Check if user is logged in and sync data
     */
    suspend fun autoLogin(): Result<String?> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                if (userId != null) {
                    // User is logged in, sync data from Firestore
                    val userResult = firestoreManager.getUser(userId)
                    if (userResult.isSuccess) {
                        val user = userResult.getOrNull()
                        if (user != null) {
                            userDao.insertUser(user)
                        }
                    }
                    Result.success(userId)
                } else {
                    Result.success(null)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
