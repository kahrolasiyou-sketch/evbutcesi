package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Transaction
    @Query("SELECT * FROM transactions WHERE profileId = :profileId ORDER BY dateTimestamp DESC, id DESC")
    fun getAllTransactions(profileId: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE profileId = :profileId AND dateTimestamp >= :startTimestamp AND dateTimestamp <= :endTimestamp ORDER BY dateTimestamp DESC, id DESC")
    fun getTransactionsBetween(profileId: Long, startTimestamp: Long, endTimestamp: Long): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE profileId = :profileId ORDER BY dateTimestamp DESC, id DESC")
    suspend fun getAllTransactionsSync(profileId: Long): List<TransactionWithCategory>

    @Query("SELECT COUNT(*) FROM transactions WHERE profileId = :profileId AND categoryId = :categoryId")
    suspend fun getCountForCategory(profileId: Long, categoryId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE profileId = :profileId")
    suspend fun deleteTransactionsByProfileId(profileId: Long)

    @Query("UPDATE transactions SET profileId = :toProfileId WHERE profileId = :fromProfileId")
    suspend fun transferTransactions(fromProfileId: Long, toProfileId: Long)

    @Query("UPDATE transactions SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long)
}

