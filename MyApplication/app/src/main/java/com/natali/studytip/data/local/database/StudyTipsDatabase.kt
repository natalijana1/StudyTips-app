package com.natali.studytip.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.natali.studytip.data.local.dao.QuoteDao
import com.natali.studytip.data.local.dao.TipDao
import com.natali.studytip.data.local.dao.UserDao
import com.natali.studytip.data.local.entities.QuoteEntity
import com.natali.studytip.data.local.entities.TipEntity
import com.natali.studytip.data.local.entities.UserEntity

@Database(
    entities = [TipEntity::class, UserEntity::class, QuoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudyTipsDatabase : RoomDatabase() {

    abstract fun tipDao(): TipDao
    abstract fun userDao(): UserDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: StudyTipsDatabase? = null

        fun getDatabase(context: Context): StudyTipsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyTipsDatabase::class.java,
                    "study_tips_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
