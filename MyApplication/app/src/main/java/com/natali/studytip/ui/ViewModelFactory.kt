package com.natali.studytip.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.natali.studytip.data.remote.firebase.FirebaseStorageManager
import com.natali.studytip.data.remote.firebase.FirestoreManager
import com.natali.studytip.data.repository.AuthRepository
import com.natali.studytip.data.repository.QuoteRepository
import com.natali.studytip.data.repository.TipRepository
import com.natali.studytip.data.repository.UserRepository
import com.natali.studytip.ui.auth.AuthViewModel
import com.natali.studytip.ui.home.HomeViewModel
import com.natali.studytip.ui.profile.ProfileViewModel
import com.natali.studytip.ui.tip.TipViewModel

class ViewModelFactory(
    private val application: Application,
    private val tipRepository: TipRepository,
    private val userRepository: UserRepository,
    private val quoteRepository: QuoteRepository,
    private val authRepository: AuthRepository,
    private val storageManager: FirebaseStorageManager,
    private val firestoreManager: FirestoreManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(application, tipRepository, quoteRepository) as T
            }
            modelClass.isAssignableFrom(TipViewModel::class.java) -> {
                TipViewModel(tipRepository, userRepository, storageManager) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(userRepository, tipRepository, authRepository, storageManager, firestoreManager) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(authRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
