package com.natali.studytip.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.natali.studytip.data.local.entities.QuoteEntity

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes ORDER BY fetchedAt DESC LIMIT 1")
    fun getLatestQuote(): LiveData<QuoteEntity?>

    @Query("SELECT * FROM quotes ORDER BY fetchedAt DESC LIMIT 1")
    suspend fun getLatestQuoteSync(): QuoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE fetchedAt < :timestamp")
    suspend fun deleteOldQuotes(timestamp: Long)

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()
}
