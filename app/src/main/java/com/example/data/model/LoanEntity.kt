package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loans",
    indices = [
        Index(value = ["profileId"])
    ]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1L,
    val bankName: String,
    val monthlyInstallment: Double,
    val totalInstallments: Int,
    val paidInstallmentsCount: Int = 0,
    val startYear: Int,
    val startMonth: Int, // 1 to 12
    val dueDayOfMonth: Int, // 1 to 31
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalDebt: Double
        get() = monthlyInstallment * totalInstallments

    val remainingInstallments: Int
        get() = (totalInstallments - paidInstallmentsCount).coerceAtLeast(0)

    val remainingDebt: Double
        get() = monthlyInstallment * remainingInstallments

    val isCompleted: Boolean
        get() = paidInstallmentsCount >= totalInstallments

    val progressPercent: Float
        get() = if (totalInstallments > 0) {
            (paidInstallmentsCount.toFloat() / totalInstallments.toFloat()).coerceIn(0f, 1f)
        } else 0f
}
