package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedExpenseDao {

    @Query("SELECT * FROM saved_expenses WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllSavedExpenses(profileId: Long): Flow<List<SavedExpenseEntity>>

    @Query("SELECT * FROM saved_expenses WHERE profileId = :profileId ORDER BY id DESC")
    suspend fun getAllSavedExpensesList(profileId: Long): List<SavedExpenseEntity>

    @Query("SELECT * FROM saved_expenses WHERE profileId = :profileId AND title = :title LIMIT 1")
    suspend fun getByTitle(profileId: Long, title: String): SavedExpenseEntity?

    @Query("SELECT COUNT(*) FROM saved_expenses WHERE profileId = :profileId")
    suspend fun getCount(profileId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedExpense(savedExpense: SavedExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedExpenses(savedExpenses: List<SavedExpenseEntity>)

    @Delete
    suspend fun deleteSavedExpense(savedExpense: SavedExpenseEntity)

    @Query("DELETE FROM saved_expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_expenses WHERE profileId = :profileId")
    suspend fun deleteSavedExpensesByProfileId(profileId: Long)

    @Query("UPDATE saved_expenses SET profileId = :toProfileId WHERE profileId = :fromProfileId")
    suspend fun transferSavedExpenses(fromProfileId: Long, toProfileId: Long)
}

