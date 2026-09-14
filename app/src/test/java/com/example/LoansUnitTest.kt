package com.example

import com.example.data.model.LoanEntity
import com.example.data.model.LoanInstallmentEntity
import com.example.data.model.LoanWithInstallments
import com.example.ui.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoansUnitTest {

    @Test
    fun testLoanCalculations() {
        val loan = LoanEntity(
            id = 1L,
            bankName = "Garanti Taşıt Kredisi",
            monthlyInstallment = 5000.0,
            totalInstallments = 12,
            paidInstallmentsCount = 3,
            startYear = 2026,
            startMonth = 1,
            dueDayOfMonth = 15
        )

        val installments = (1..12).map { i ->
            LoanInstallmentEntity(
                id = i.toLong(),
                loanId = 1L,
                installmentNumber = i,
                year = 2026,
                month = i,
                amount = 5000.0,
                dueDay = 15,
                isPaid = i <= 3,
                paidAtTimestamp = if (i <= 3) 1700000000000L else null
            )
        }

        val loanWithInstallments = LoanWithInstallments(
            loan = loan,
            installments = installments
        )

        assertEquals(3, loanWithInstallments.actualPaidCount)
        assertEquals(9, loanWithInstallments.actualRemainingCount)
        assertEquals(45000.0, loanWithInstallments.remainingDebt, 0.01)
        assertEquals(0.25f, loanWithInstallments.progressPercent, 0.01f)
    }

    @Test
    fun testAtmCurrencyFormatting() {
        // ATM Kuralları:
        // 1 -> 0,01
        // 18 -> 0,18
        // 1865035 -> 18.650,35
        assertEquals("0,01", Formatters.formatCentsToTurkishLira(1L))
        assertEquals("0,18", Formatters.formatCentsToTurkishLira(18L))
        assertEquals("18.650,35", Formatters.formatCentsToTurkishLira(1865035L))
        assertEquals("1.250,25", Formatters.formatCentsToTurkishLira(125025L))
    }
}
