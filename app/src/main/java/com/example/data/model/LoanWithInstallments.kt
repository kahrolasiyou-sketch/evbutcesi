package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class LoanWithInstallments(
    @Embedded val loan: LoanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "loanId"
    )
    val installments: List<LoanInstallmentEntity>
) {
    val sortedInstallments: List<LoanInstallmentEntity>
        get() = installments.sortedBy { it.installmentNumber }

    val actualPaidCount: Int
        get() = installments.count { it.isPaid }

    val actualRemainingCount: Int
        get() = (loan.totalInstallments - actualPaidCount).coerceAtLeast(0)

    val remainingDebt: Double
        get() = actualRemainingCount * loan.monthlyInstallment

    val progressPercent: Float
        get() = if (loan.totalInstallments > 0) {
            (actualPaidCount.toFloat() / loan.totalInstallments.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val isCompleted: Boolean
        get() = actualPaidCount >= loan.totalInstallments
}
