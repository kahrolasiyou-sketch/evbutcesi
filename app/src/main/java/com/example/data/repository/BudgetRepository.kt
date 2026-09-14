package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.LoanDao
import com.example.data.local.SavedExpenseDao
import com.example.data.local.TransactionDao
import com.example.data.local.UserProfileDao
import com.example.data.model.CategoryEntity
import com.example.data.model.LoanEntity
import com.example.data.model.LoanInstallmentEntity
import com.example.data.model.LoanWithInstallments
import com.example.data.model.SavedExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionWithCategory
import com.example.data.model.UserProfileEntity
import com.example.ui.util.Formatters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BudgetRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val savedExpenseDao: SavedExpenseDao,
    private val userProfileDao: UserProfileDao,
    private val loanDao: LoanDao
) {

    val allRegisteredUsers: Flow<List<UserProfileEntity>> = userProfileDao.getAllRegisteredUsers()
    val allUsers: Flow<List<UserProfileEntity>> = userProfileDao.getAllUsers()
    val activeUser: Flow<UserProfileEntity?> = userProfileDao.getActiveUser()

    fun getCategories(profileId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories(profileId)
    }

    fun getTransactions(profileId: Long): Flow<List<TransactionWithCategory>> {
        return transactionDao.getAllTransactions(profileId)
    }

    fun getTransactionsForRange(profileId: Long, start: Long, end: Long): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsBetween(profileId, start, end)
    }

    fun getSavedExpenses(profileId: Long): Flow<List<SavedExpenseEntity>> {
        return savedExpenseDao.getAllSavedExpenses(profileId)
    }

    fun getLoans(profileId: Long): Flow<List<LoanWithInstallments>> {
        return loanDao.getAllLoansWithInstallments(profileId)
    }

    fun getCategoriesByType(profileId: Long, type: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(profileId, type)
    }

    suspend fun ensureDefaultCategories() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaults = listOf(
                CategoryEntity(
                    profileId = 0L,
                    name = "Faturalar",
                    type = "EXPENSE",
                    iconName = "receipt",
                    colorHex = 0xFFEF4444, // Red
                    isDefault = false
                ),
                CategoryEntity(
                    profileId = 0L,
                    name = "Borçlar",
                    type = "EXPENSE",
                    iconName = "debt",
                    colorHex = 0xFFE11D48, // Rose
                    isDefault = false
                ),
                CategoryEntity(
                    profileId = 0L,
                    name = "Market",
                    type = "EXPENSE",
                    iconName = "cart",
                    colorHex = 0xFFF97316, // Orange
                    isDefault = false
                ),
                CategoryEntity(
                    profileId = 0L,
                    name = "Diğer",
                    type = "BOTH",
                    iconName = "other",
                    colorHex = 0xFF64748B, // Slate
                    isDefault = true
                ),
                CategoryEntity(
                    profileId = 0L,
                    name = "Maaş",
                    type = "INCOME",
                    iconName = "salary",
                    colorHex = 0xFF10B981, // Emerald green
                    isDefault = false
                ),
                CategoryEntity(
                    profileId = 0L,
                    name = "Ek Gelir",
                    type = "INCOME",
                    iconName = "bonus",
                    colorHex = 0xFF06B6D4, // Cyan
                    isDefault = false
                )
            )
            categoryDao.insertCategories(defaults)
        }
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity, profileId: Long) {
        val fallback = categoryDao.getCategoryByName("Diğer")
            ?: categoryDao.getAllCategories(profileId).first().firstOrNull { it.id != category.id }

        if (fallback != null) {
            transactionDao.reassignCategory(category.id, fallback.id)
        }
        categoryDao.deleteCategory(category)
    }

    suspend fun getTransactionCountForCategory(profileId: Long, categoryId: Long): Int {
        return transactionDao.getCountForCategory(profileId, categoryId)
    }

    suspend fun ensureDefaultSavedExpenses(profileId: Long) {
        val billsCategory = categoryDao.getCategoryByName("Faturalar")
        val otherCategory = categoryDao.getCategoryByName("Diğer") ?: billsCategory
        val defaultBillsCatId = billsCategory?.id
        val otherCatId = otherCategory?.id ?: defaultBillsCatId

        if (savedExpenseDao.getCount(profileId) == 0) {
            val templates = listOf(
                SavedExpenseEntity(
                    profileId = profileId,
                    title = "Elektrik Faturası",
                    categoryId = defaultBillsCatId,
                    type = "EXPENSE"
                ),
                SavedExpenseEntity(
                    profileId = profileId,
                    title = "Su Faturası",
                    categoryId = defaultBillsCatId,
                    type = "EXPENSE"
                ),
                SavedExpenseEntity(
                    profileId = profileId,
                    title = "Doğalgaz Faturası",
                    categoryId = defaultBillsCatId,
                    type = "EXPENSE"
                ),
                SavedExpenseEntity(
                    profileId = profileId,
                    title = "İnternet Faturası",
                    categoryId = defaultBillsCatId,
                    type = "EXPENSE"
                ),
                SavedExpenseEntity(
                    profileId = profileId,
                    title = "Kira Ödemesi",
                    categoryId = otherCatId,
                    type = "EXPENSE"
                )
            )
            savedExpenseDao.insertSavedExpenses(templates)
        }
    }

    suspend fun saveExpenseTemplate(
        profileId: Long,
        title: String,
        categoryId: Long?,
        defaultAmount: Double? = null,
        type: String = "EXPENSE"
    ): Long {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return -1L

        val existing = savedExpenseDao.getByTitle(profileId, trimmed)
        return if (existing != null) {
            val updated = existing.copy(
                categoryId = categoryId ?: existing.categoryId,
                defaultAmount = defaultAmount ?: existing.defaultAmount,
                type = type
            )
            savedExpenseDao.insertSavedExpense(updated)
            updated.id
        } else {
            val newEntity = SavedExpenseEntity(
                profileId = profileId,
                title = trimmed,
                categoryId = categoryId,
                defaultAmount = defaultAmount,
                type = type
            )
            savedExpenseDao.insertSavedExpense(newEntity)
        }
    }

    suspend fun deleteSavedExpenseById(id: Long) {
        savedExpenseDao.deleteById(id)
    }

    suspend fun deleteSavedExpense(savedExpense: SavedExpenseEntity) {
        savedExpenseDao.deleteById(savedExpense.id)
    }

    suspend fun ensureInitialUser() {
        if (userProfileDao.getUserCount() == 0) {
            // First time launch: create default profile
            userProfileDao.insertUser(
                UserProfileEntity(
                    name = "Varsayılan Kullanıcı",
                    password = "",
                    pin = "",
                    avatarName = "person",
                    colorHex = 0xFF28C76F,
                    isActive = true,
                    isGuest = false
                )
            )
        }
    }

    suspend fun createProfile(
        name: String,
        password: String,
        pin: String,
        avatarName: String,
        colorHex: Long,
        setAsActive: Boolean = true
    ): Long {
        if (setAsActive) {
            userProfileDao.deactivateAll()
        }
        val id = userProfileDao.insertUser(
            UserProfileEntity(
                name = name.trim(),
                password = password.trim(),
                pin = pin.trim(),
                avatarName = avatarName,
                colorHex = colorHex,
                isActive = setAsActive,
                isGuest = false
            )
        )
        ensureDefaultSavedExpenses(id)
        return id
    }

    suspend fun activateGuestMode(): Long {
        userProfileDao.deactivateAll()
        val existingGuest = userProfileDao.getGuestUser()
        return if (existingGuest != null) {
            userProfileDao.setActiveUser(existingGuest.id)
            existingGuest.id
        } else {
            val guestId = userProfileDao.insertUser(
                UserProfileEntity(
                    name = "Misafir",
                    password = "",
                    pin = "",
                    avatarName = "person",
                    colorHex = 0xFF6C757D,
                    isActive = true,
                    isGuest = true
                )
            )
            ensureDefaultSavedExpenses(guestId)
            guestId
        }
    }

    suspend fun switchProfile(userId: Long) {
        userProfileDao.deactivateAll()
        userProfileDao.setActiveUser(userId)
        ensureDefaultSavedExpenses(userId)
    }

    suspend fun logoutToWelcome() {
        userProfileDao.deactivateAll()
    }

    suspend fun updatePassword(userId: Long, newPassword: String, newPin: String) {
        val user = userProfileDao.getUserById(userId) ?: return
        userProfileDao.updateUser(
            user.copy(
                password = newPassword.trim(),
                pin = newPin.trim()
            )
        )
    }

    suspend fun transferGuestDataToProfile(guestProfileId: Long, newProfileId: Long) {
        transactionDao.transferTransactions(guestProfileId, newProfileId)
        loanDao.transferLoans(guestProfileId, newProfileId)
        savedExpenseDao.transferSavedExpenses(guestProfileId, newProfileId)
    }

    suspend fun deleteProfileAndAllData(user: UserProfileEntity) {
        val profileId = user.id
        // Delete all transactions, loans, installments, saved expenses, and custom categories
        transactionDao.deleteTransactionsByProfileId(profileId)
        loanDao.deleteInstallmentsByProfileId(profileId)
        loanDao.deleteLoansByProfileId(profileId)
        savedExpenseDao.deleteSavedExpensesByProfileId(profileId)
        categoryDao.deleteCategoriesByProfileId(profileId)
        userProfileDao.deleteUser(user)

        // If this was the active user, activate another registered user or guest
        if (user.isActive) {
            val nextUser = userProfileDao.getFirstUser()
            if (nextUser != null) {
                userProfileDao.setActiveUser(nextUser.id)
            }
        }
    }

    suspend fun addLoanWithInstallments(loan: LoanEntity, initialPaidCount: Int): Long {
        val loanId = loanDao.insertLoan(loan.copy(paidInstallmentsCount = initialPaidCount.coerceAtMost(loan.totalInstallments)))
        val installments = mutableListOf<LoanInstallmentEntity>()

        for (i in 0 until loan.totalInstallments) {
            val offsetMonths = (loan.startMonth - 1) + i
            val installmentYear = loan.startYear + (offsetMonths / 12)
            val installmentMonth = (offsetMonths % 12) + 1
            val isPaid = i < initialPaidCount
            val paidAt = if (isPaid) System.currentTimeMillis() else null

            installments.add(
                LoanInstallmentEntity(
                    loanId = loanId,
                    installmentNumber = i + 1,
                    year = installmentYear,
                    month = installmentMonth,
                    dueDay = loan.dueDayOfMonth,
                    amount = loan.monthlyInstallment,
                    isPaid = isPaid,
                    paidAtTimestamp = paidAt
                )
            )
        }

        loanDao.insertInstallments(installments)
        val paidCount = loanDao.getPaidCountForLoan(loanId)
        loanDao.updatePaidCount(loanId, paidCount)
        return loanId
    }

    suspend fun toggleInstallmentPaid(installmentId: Long, loanId: Long, isPaid: Boolean) {
        val paidAt = if (isPaid) System.currentTimeMillis() else null
        loanDao.setInstallmentPaid(installmentId, isPaid, paidAt)
        val paidCount = loanDao.getPaidCountForLoan(loanId)
        loanDao.updatePaidCount(loanId, paidCount)
    }

    suspend fun deleteLoan(loan: LoanEntity) {
        loanDao.deleteInstallmentsForLoan(loan.id)
        loanDao.deleteLoan(loan)
    }

    suspend fun deleteLoanById(loanId: Long) {
        loanDao.deleteInstallmentsForLoan(loanId)
        loanDao.deleteLoanById(loanId)
    }

    suspend fun exportDataJson(profileId: Long): String {
        val user = userProfileDao.getUserById(profileId)
        val transactions = transactionDao.getAllTransactionsSync(profileId)
        val loans = loanDao.getAllLoansWithInstallmentsSync(profileId)

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"profil\": {\n")
        sb.append("    \"id\": $profileId,\n")
        sb.append("    \"ad\": \"${user?.name ?: "Bilinmiyor"}\",\n")
        sb.append("    \"tur\": \"${if (user?.isGuest == true) "Misafir" else "Kullanıcı"}\"\n")
        sb.append("  },\n")

        sb.append("  \"islemler\": [\n")
        transactions.forEachIndexed { index, item ->
            sb.append("    {\n")
            sb.append("      \"id\": ${item.transaction.id},\n")
            sb.append("      \"baslik\": \"${item.transaction.title.replace("\"", "\\\"")}\",\n")
            sb.append("      \"tutar\": ${item.transaction.amount},\n")
            sb.append("      \"tur\": \"${item.transaction.type}\",\n")
            sb.append("      \"kategori\": \"${item.category?.name ?: "Diğer"}\",\n")
            sb.append("      \"tarih\": \"${Formatters.formatDate(item.transaction.dateTimestamp)}\"\n")
            sb.append("    }${if (index < transactions.size - 1) "," else ""}\n")
        }
        sb.append("  ],\n")

        sb.append("  \"krediler\": [\n")
        loans.forEachIndexed { index, item ->
            sb.append("    {\n")
            sb.append("      \"id\": ${item.loan.id},\n")
            sb.append("      \"banka\": \"${item.loan.bankName.replace("\"", "\\\"")}\",\n")
            sb.append("      \"aylikTaksit\": ${item.loan.monthlyInstallment},\n")
            sb.append("      \"toplamTaksit\": ${item.loan.totalInstallments},\n")
            sb.append("      \"odenenTaksit\": ${item.actualPaidCount},\n")
            sb.append("      \"kalanBorc\": ${item.remainingDebt}\n")
            sb.append("    }${if (index < loans.size - 1) "," else ""}\n")
        }
        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    suspend fun exportDataCsv(profileId: Long): String {
        val user = userProfileDao.getUserById(profileId)
        val transactions = transactionDao.getAllTransactionsSync(profileId)
        val loans = loanDao.getAllLoansWithInstallmentsSync(profileId)

        val sb = StringBuilder()
        sb.append("EV BÜTÇESİ FİNANSAL GEÇMİŞ RAPORU\n")
        sb.append("Profil:;${user?.name ?: "Bilinmiyor"};Tür:;${if (user?.isGuest == true) "Misafir" else "Kullanıcı"}\n")
        sb.append("Oluşturulma Tarihi:;${Formatters.formatDate(System.currentTimeMillis())}\n\n")

        sb.append("GELİRLER VE GİDERLER\n")
        sb.append("Tarih;Tür;Kategori;Başlık;Tutar (TL);Not\n")
        transactions.forEach { item ->
            sb.append("${Formatters.formatDate(item.transaction.dateTimestamp)};")
            sb.append("${if (item.transaction.type == "INCOME") "Gelir" else "Gider"};")
            sb.append("${item.category?.name ?: "Diğer"};")
            sb.append("\"${item.transaction.title.replace("\"", "\"\"")}\";")
            sb.append("${item.transaction.amount};")
            sb.append("\"${item.transaction.notes.replace("\"", "\"\"")}\"\n")
        }

        sb.append("\nKREDİLER VE TAKSİTLER\n")
        sb.append("Banka / Kredi Adı;Aylık Taksit;Toplam Taksit;Ödenen Taksit;Kalan Taksit;Kalan Borç (TL)\n")
        loans.forEach { item ->
            sb.append("\"${item.loan.bankName.replace("\"", "\"\"")}\";")
            sb.append("${item.loan.monthlyInstallment};")
            sb.append("${item.loan.totalInstallments};")
            sb.append("${item.actualPaidCount};")
            sb.append("${item.actualRemainingCount};")
            sb.append("${item.remainingDebt}\n")
        }

        return sb.toString()
    }
}
