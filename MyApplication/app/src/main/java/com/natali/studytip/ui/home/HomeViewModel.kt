package com.natali.studytip.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.natali.studytip.data.models.Author
import com.natali.studytip.data.models.Quote
import com.natali.studytip.data.models.Tip
import com.natali.studytip.data.repository.QuoteRepository
import com.natali.studytip.data.repository.TipRepository
import com.natali.studytip.utils.NetworkUtils
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class HomeViewModel(
    application: Application,
    private val tipRepository: TipRepository,
    private val quoteRepository: QuoteRepository
) : AndroidViewModel(application) {

    // All tips from repository
    private val allTips: LiveData<List<Tip>> = tipRepository.getAllTips()

    // Selected author filter
    private val _selectedAuthorId = MutableLiveData<String?>(null)
    val selectedAuthorId: LiveData<String?> = _selectedAuthorId

    // Selected author details for filter bar
    val selectedAuthor: LiveData<Author?> = MediatorLiveData<Author?>().apply {
        addSource(allTips) { tips ->
            val authorId = _selectedAuthorId.value
            if (authorId != null) {
                val tip = tips.firstOrNull { it.authorId == authorId }
                value = tip?.let {
                    Author(
                        id = it.authorId,
                        name = it.authorName,
                        photoUrl = it.authorPhotoUrl
                    )
                }
            } else {
                value = null
            }
        }
        addSource(_selectedAuthorId) { authorId ->
            if (authorId != null) {
                val tips = allTips.value ?: emptyList()
                val tip = tips.firstOrNull { it.authorId == authorId }
                value = tip?.let {
                    Author(
                        id = it.authorId,
                        name = it.authorName,
                        photoUrl = it.authorPhotoUrl
                    )
                }
            } else {
                value = null
            }
        }
    }

    // Filter active state
    val isFilterActive: LiveData<Boolean> = selectedAuthorId.map { it != null }

    // Filtered tips based on author selection
    val filteredTips: LiveData<List<Tip>> = MediatorLiveData<List<Tip>>().apply {
        addSource(allTips) { tips ->
            value = filterTips(tips, _selectedAuthorId.value)
        }
        addSource(_selectedAuthorId) { authorId ->
            value = filterTips(allTips.value, authorId)
        }
    }

    // Latest quote from repository
    val quote: LiveData<Quote?> = quoteRepository.getLatestQuote()

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Error message
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Get unique authors for filter
    val authors: LiveData<List<Author>> = allTips.map { tips ->
        android.util.Log.d("HomeViewModel", "Processing ${tips.size} tips for authors")

        val authorList = tips.map { tip ->
            android.util.Log.d("HomeViewModel", "Tip: id=${tip.id}, authorId='${tip.authorId}', authorName='${tip.authorName}'")
            Author(
                id = tip.authorId,
                name = tip.authorName,
                photoUrl = tip.authorPhotoUrl
            )
        }
        .distinctBy { it.id }
        .sortedBy { it.name }

        android.util.Log.d("HomeViewModel", "Extracted ${authorList.size} unique authors")
        authorList.forEach { author ->
            android.util.Log.d("HomeViewModel", "Author: id='${author.id}', name='${author.name}'")
        }

        authorList
    }

    init {
        // Fetch a quote when ViewModel is created
        refreshQuote()
        // Sync tips from Firestore
        syncTips()

        // Fix any tips with missing author data after a delay to ensure user is loaded
        viewModelScope.launch {
            try {
                // Wait for sync to complete
                kotlinx.coroutines.delay(2000)

                val result = tipRepository.fixMissingAuthorData()
                if (result.isSuccess) {
                    android.util.Log.d("HomeViewModel", "Successfully fixed missing author data")
                } else {
                    android.util.Log.w("HomeViewModel", "Migration result: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Failed to fix missing author data", e)
            }
        }
    }

    // Filter tips by author
    private fun filterTips(tips: List<Tip>?, authorId: String?): List<Tip> {
        if (tips == null) return emptyList()
        return if (authorId == null) {
            tips
        } else {
            tips.filter { it.authorId == authorId }
        }
    }

    // Set author filter
    fun filterByAuthor(authorId: String?) {
        _selectedAuthorId.value = authorId
    }

    // Clear author filter
    fun clearFilter() {
        _selectedAuthorId.value = null
    }

    // Refresh quote from API
    fun refreshQuote() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null  // Clear previous errors

            try {
                // Check network connectivity first
                if (!NetworkUtils.isNetworkAvailable(getApplication())) {
                    _errorMessage.value = "No internet connection. Showing cached quote."
                }

                quoteRepository.fetchNewQuote()
                // Success - error message cleared above

            } catch (e: UnknownHostException) {
                _errorMessage.value = "Cannot reach quote service. Showing cached quote."
                android.util.Log.e("HomeViewModel", "DNS/Host error fetching quote", e)

            } catch (e: SocketTimeoutException) {
                _errorMessage.value = "Connection timeout. Please try again."
                android.util.Log.e("HomeViewModel", "Timeout fetching quote", e)

            } catch (e: IOException) {
                _errorMessage.value = "Network error. Showing cached quote."
                android.util.Log.e("HomeViewModel", "Network error fetching quote", e)

            } catch (e: Exception) {
                _errorMessage.value = "Unable to fetch new quote. Showing cached quote."
                android.util.Log.e("HomeViewModel", "Unexpected error fetching quote", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Sync tips from Firestore
    fun syncTips() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                tipRepository.syncTipsFromFirestore()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to sync tips: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clear error message
    fun clearError() {
        _errorMessage.value = null
    }
}
