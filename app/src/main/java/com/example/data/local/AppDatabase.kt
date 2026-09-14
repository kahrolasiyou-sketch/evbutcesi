package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.LoanEntity
import com.example.data.model.LoanInstallmentEntity
import com.example.data.model.SavedExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfileEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        SavedExpenseEntity::class,
        UserProfileEntity::class,
        LoanEntity::class,
        LoanInstallmentEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savedExpenseDao(): SavedExpenseDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun loanDao(): LoanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ev_butcesi_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
