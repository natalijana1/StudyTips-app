package com.natali.studytip.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.natali.studytip.data.local.dao.QuoteDao
import com.natali.studytip.data.local.entities.QuoteEntity
import com.natali.studytip.data.models.Quote
import com.natali.studytip.data.remote.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRepository(private val quoteDao: QuoteDao) {

    private val quoteApi = RetrofitInstance.quoteApi

    companion object {
        private val DEFAULT_QUOTE = Quote(
            text = "The expert in anything was once a beginner.",
            author = "Helen Hayes"
        )
    }

    // Get the latest quote from the database as LiveData
    fun getLatestQuote(): LiveData<Quote?> {
        return quoteDao.getLatestQuote().map { entity ->
            entity?.toQuote()
        }
    }

    // Fetch a new quote from API and cache it
    suspend fun fetchNewQuote(): Quote {
        return withContext(Dispatchers.IO) {
            try {
                val responseList = quoteApi.getRandomQuote()
                if (responseList.isEmpty()) {
                    throw Exception("No quotes returned from API")
                }
                val response = responseList.first()

                val quoteEntity = QuoteEntity(
                    text = response.quote,
                    author = response.author,
                    category = response.categories?.firstOrNull(),
                    fetchedAt = System.currentTimeMillis()
                )

                quoteDao.insertQuote(quoteEntity)

                quoteEntity.toQuote()
            } catch (e: Exception) {
                // Log detailed error for debugging
                android.util.Log.e("QuoteRepository", "Failed to fetch quote from API: ${e.javaClass.simpleName}: ${e.message}", e)

                // Try to return cached quote
                val cachedQuote = quoteDao.getLatestQuoteSync()?.toQuote()
                if (cachedQuote != null) {
                    android.util.Log.d("QuoteRepository", "Returning cached quote")
                    return@withContext cachedQuote
                }

                // If no cache, insert default quote into database and return it
                android.util.Log.d("QuoteRepository", "No cache available, inserting and returning default quote")
                val defaultQuoteEntity = QuoteEntity(
                    text = DEFAULT_QUOTE.text,
                    author = DEFAULT_QUOTE.author,
                    category = null,
                    fetchedAt = System.currentTimeMillis()
                )
                quoteDao.insertQuote(defaultQuoteEntity)
                DEFAULT_QUOTE
            }
        }
    }

    // Delete quotes older than specified timestamp
    suspend fun deleteOldQuotes(timestamp: Long) {
        quoteDao.deleteOldQuotes(timestamp)
    }

    // Mapper function: QuoteEntity -> Quote
    private fun QuoteEntity.toQuote() = Quote(
        text = text,
        author = author
    )
}
