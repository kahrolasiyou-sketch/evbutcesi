package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.LoanEntity
import com.example.data.model.LoanInstallmentEntity
import com.example.data.model.LoanWithInstallments
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Transaction
    @Query("SELECT * FROM loans WHERE profileId = :profileId ORDER BY id DESC")
    fun getAllLoansWithInstallments(profileId: Long): Flow<List<LoanWithInstallments>>

    @Transaction
    @Query("SELECT * FROM loans WHERE profileId = :profileId ORDER BY id DESC")
    suspend fun getAllLoansWithInstallmentsSync(profileId: Long): List<LoanWithInstallments>

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getLoanById(id: Long): LoanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallments(installments: List<LoanInstallmentEntity>)

    @Update
    suspend fun updateLoan(loan: LoanEntity)

    @Delete
    suspend fun deleteLoan(loan: LoanEntity)

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteLoanById(id: Long)

    @Query("DELETE FROM loan_installments WHERE loanId = :loanId")
    suspend fun deleteInstallmentsForLoan(loanId: Long)

    @Query("DELETE FROM loan_installments WHERE loanId IN (SELECT id FROM loans WHERE profileId = :profileId)")
    suspend fun deleteInstallmentsByProfileId(profileId: Long)

    @Query("DELETE FROM loans WHERE profileId = :profileId")
    suspend fun deleteLoansByProfileId(profileId: Long)

    @Query("UPDATE loans SET profileId = :toProfileId WHERE profileId = :fromProfileId")
    suspend fun transferLoans(fromProfileId: Long, toProfileId: Long)

    @Update
    suspend fun updateInstallment(installment: LoanInstallmentEntity)

    @Query("UPDATE loan_installments SET isPaid = :isPaid, paidAtTimestamp = :paidAt WHERE id = :installmentId")
    suspend fun setInstallmentPaid(installmentId: Long, isPaid: Boolean, paidAt: Long?)

    @Query("SELECT COUNT(*) FROM loan_installments WHERE loanId = :loanId AND isPaid = 1")
    suspend fun getPaidCountForLoan(loanId: Long): Int

    @Query("UPDATE loans SET paidInstallmentsCount = :count WHERE id = :loanId")
    suspend fun updatePaidCount(loanId: Long, count: Int)

    @Query("SELECT * FROM loan_installments WHERE loanId IN (SELECT id FROM loans WHERE profileId = :profileId) AND year = :year AND month = :month")
    suspend fun getInstallmentsForMonth(profileId: Long, year: Int, month: Int): List<LoanInstallmentEntity>
}

