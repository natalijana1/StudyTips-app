package com.natali.studytip.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.natali.studytip.data.local.entities.TipEntity

@Dao
interface TipDao {

    @Query("SELECT * FROM tips WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllTips(): LiveData<List<TipEntity>>

    @Query("SELECT * FROM tips WHERE isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllTipsSync(): List<TipEntity>

    @Query("SELECT * FROM tips WHERE authorId = :authorId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getTipsByAuthorId(authorId: String): LiveData<List<TipEntity>>

    @Query("SELECT * FROM tips WHERE id = :tipId")
    suspend fun getTipById(tipId: String): TipEntity?

    @Query("SELECT * FROM tips WHERE isSynced = 0 AND isDeleted = 0")
    suspend fun getUnsyncedTips(): List<TipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: TipEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: List<TipEntity>)

    @Update
    suspend fun updateTip(tip: TipEntity)

    @Query("UPDATE tips SET isDeleted = 1, isSynced = 0 WHERE id = :tipId")
    suspend fun softDeleteTip(tipId: String)

    @Query("DELETE FROM tips WHERE id = :tipId")
    suspend fun hardDeleteTip(tipId: String)

    @Query("DELETE FROM tips WHERE isDeleted = 1")
    suspend fun deleteAllSoftDeleted()
}
