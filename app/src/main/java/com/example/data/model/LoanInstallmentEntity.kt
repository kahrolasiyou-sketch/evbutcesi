package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loan_installments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["loanId"])]
)
data class LoanInstallmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val loanId: Long,
    val installmentNumber: Int, // 1, 2, ...
    val year: Int,
    val month: Int, // 1 to 12
    val dueDay: Int, // 1 to 31
    val amount: Double,
    val isPaid: Boolean = false,
    val paidAtTimestamp: Long? = null
)
