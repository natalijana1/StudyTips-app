package com.natali.studytip.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.natali.studytip.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> = _authSuccess

    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        // Validate inputs
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty"
            return
        }

        if (!isValidEmail(email)) {
            _errorMessage.value = "Invalid email format"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            _isLoading.value = false

            if (result.isSuccess) {
                _authSuccess.value = true
            } else {
                _errorMessage.value = getErrorMessage(result.exceptionOrNull())
            }
        }
    }

    /**
     * Sign up new user
     */
    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        // Validate inputs
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _errorMessage.value = "All fields are required"
            return
        }

        if (!isValidEmail(email)) {
            _errorMessage.value = "Invalid email format"
            return
        }

        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = authRepository.signUp(name, email, password)
            _isLoading.value = false

            if (result.isSuccess) {
                _authSuccess.value = true
            } else {
                _errorMessage.value = getErrorMessage(result.exceptionOrNull())
            }
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    /**
     * Auto-login if user is already logged in
     */
    fun checkAutoLogin(onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (authRepository.isUserLoggedIn()) {
            _isLoading.value = true
            viewModelScope.launch {
                val result = authRepository.autoLogin()
                _isLoading.value = false

                if (result.isSuccess && result.getOrNull() != null) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
        } else {
            onFailure()
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Validate email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Get user-friendly error message from exception
     */
    private fun getErrorMessage(exception: Throwable?): String {
        return when {
            exception?.message?.contains("badly formatted") == true -> "Invalid email format"
            exception?.message?.contains("no user record") == true -> "No account found with this email"
            exception?.message?.contains("password is invalid") == true -> "Incorrect password"
            exception?.message?.contains("email address is already") == true -> "Email already in use"
            exception?.message?.contains("network") == true -> "Network error. Please check your connection"
            else -> exception?.message ?: "Authentication failed"
        }
    }
}
