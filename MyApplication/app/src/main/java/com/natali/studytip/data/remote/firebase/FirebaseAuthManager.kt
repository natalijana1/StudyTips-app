package com.natali.studytip.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Get current logged-in user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Sign up with email and password
     * @return userId on success
     */
    suspend fun signUp(email: String, password: String, displayName: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("User creation failed"))

            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            Result.success(user.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with email and password
     * @return userId on success
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Sign in failed"))
            Result.success(user.uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user display name
     */
    suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
