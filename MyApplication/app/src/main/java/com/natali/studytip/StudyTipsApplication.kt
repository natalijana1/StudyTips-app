package com.natali.studytip

import android.app.Application
import com.natali.studytip.data.local.database.StudyTipsDatabase
import com.natali.studytip.data.remote.firebase.FirebaseAuthManager
import com.natali.studytip.data.remote.firebase.FirebaseStorageManager
import com.natali.studytip.data.remote.firebase.FirestoreManager
import com.natali.studytip.data.repository.AuthRepository
import com.natali.studytip.data.repository.QuoteRepository
import com.natali.studytip.data.repository.TipRepository
import com.natali.studytip.data.repository.UserRepository

class StudyTipsApplication : Application() {

    companion object {
        // TODO: Replace this with your Firebase Storage bucket URL after creating it
        // Example: "gs://your-project-name.appspot.com" or "gs://your-bucket-name.europe-west1.firebasestorage.app"
        // Leave as null to use default bucket (if available)
        private val STORAGE_BUCKET_URL: String? = null
    }

    // Database instance
    val database: StudyTipsDatabase by lazy {
        StudyTipsDatabase.getDatabase(this)
    }

    // Firebase managers
    val firebaseAuthManager: FirebaseAuthManager by lazy {
        FirebaseAuthManager()
    }

    val firestoreManager: FirestoreManager by lazy {
        FirestoreManager()
    }

    val firebaseStorageManager: FirebaseStorageManager by lazy {
        FirebaseStorageManager(STORAGE_BUCKET_URL)
    }

    // Repositories
    val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }

    val tipRepository: TipRepository by lazy {
        TipRepository(database.tipDao(), firestoreManager, userRepository)
    }

    val quoteRepository: QuoteRepository by lazy {
        QuoteRepository(database.quoteDao())
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuthManager, firestoreManager, database.userDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Database is initialized lazily when first accessed
    }
}
