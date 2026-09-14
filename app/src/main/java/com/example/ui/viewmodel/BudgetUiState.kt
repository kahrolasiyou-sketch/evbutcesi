package com.example.ui.viewmodel

import com.example.data.model.CategoryEntity
import com.example.data.model.LoanEntity
import com.example.data.model.LoanWithInstallments
import com.example.data.model.SavedExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithCategory
import com.example.data.model.UserProfileEntity
import java.util.Calendar

enum class AppDestination {
    DASHBOARD,
    HISTORY,
    LOANS,
    SETTINGS
}

data class CategorySpending(
    val category: CategoryEntity,
    val totalAmount: Double,
    val percentage: Float
)

data class BudgetUiState(
    val currentDestination: AppDestination = AppDestination.DASHBOARD,
    val selectedCalendar: Calendar = Calendar.getInstance(),
    val allCategories: List<CategoryEntity> = emptyList(),
    val currentMonthTransactions: List<TransactionWithCategory> = emptyList(),
    val savedExpenses: List<SavedExpenseEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val variableExpense: Double = 0.0,
    val monthlyLoansTotal: Double = 0.0,
    val netBalance: Double = 0.0,
    val categorySpendings: List<CategorySpending> = emptyList(),

    // Loans Module
    val allLoans: List<LoanWithInstallments> = emptyList(),
    val totalRemainingDebt: Double = 0.0,
    val activeLoansCount: Int = 0,
    val isAddLoanDialogVisible: Boolean = false,
    val addLoanBankName: String = "",
    val addLoanMonthlyAmount: String = "",
    val addLoanTotalInstallments: String = "12",
    val addLoanPaidInstallments: String = "0",
    val addLoanStartYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val addLoanStartMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val addLoanDueDay: Int = 15,
    val addLoanError: String? = null,
    val isDeleteLoanDialogVisible: Boolean = false,
    val loanToDelete: LoanEntity? = null,

    // User Profile & Multi-tenant State
    val users: List<UserProfileEntity> = emptyList(),
    val activeUser: UserProfileEntity? = null,

    // Mandatory Startup & Login/Lock Screen
    val isWelcomeScreenVisible: Boolean = true,
    val loginSelectedUser: UserProfileEntity? = null,
    val loginPinInput: String = "",
    val loginError: String? = null,
    val isProfileListDialogVisible: Boolean = false,

    // Create Profile Dialog
    val isCreateProfileDialogVisible: Boolean = false,
    val createProfileName: String = "",
    val createProfilePassword: String = "", // backwards-compatibility
    val createProfilePin: String = "",
    val createProfileAvatar: String = "person",
    val createProfileColor: Long = 0xFF28C76F,
    val createProfileMigrateGuestData: Boolean = true,
    val createProfileError: String? = null,

    // Switch Profile Dialog
    val isSwitchProfileDialogVisible: Boolean = false,
    val targetProfileToSwitch: UserProfileEntity? = null,
    val switchProfilePasswordInput: String = "",
    val switchProfilePinInput: String = "",
    val switchProfileError: String? = null,

    // Change PIN Dialog
    val isChangePasswordDialogVisible: Boolean = false,
    val currentPasswordInput: String = "",
    val currentPinInput: String = "",
    val newPasswordInput: String = "",
    val newPinInput: String = "",
    val confirmNewPinInput: String = "",
    val changePasswordError: String? = null,
    val changePinError: String? = null,

    // Delete Profile Dialog
    val isDeleteProfileDialogVisible: Boolean = false,
    val profileToDelete: UserProfileEntity? = null,
    val deleteProfilePasswordInput: String = "",
    val deleteProfilePinInput: String = "",
    val deleteProfileError: String? = null,

    // Export Data Dialog
    val isExportDialogVisible: Boolean = false,
    val exportFormat: String = "JSON", // "JSON" or "CSV"
    val exportDataText: String = "",
    val exportToastMessage: String? = null,

    // App Theme (false = Açık / Light, true = Koyu / Dark)
    val isDarkMode: Boolean = false,

    // History Filters
    val typeFilter: TransactionType? = null,
    val categoryFilterId: Long? = null,
    val searchQuery: String = "",
    val filterOnlySelectedMonth: Boolean = true,

    // Bottom Sheet (Add/Edit)
    val isBottomSheetVisible: Boolean = false,
    val editingTransactionId: Long? = null,
    val formType: TransactionType = TransactionType.EXPENSE,
    val formAmount: String = "",
    val formTitle: String = "",
    val formCategoryId: Long? = null,
    val formDateTimestamp: Long = System.currentTimeMillis(),
    val formNotes: String = "",
    val formSaveAsTemplate: Boolean = false,
    val formError: String? = null,

    // Category Management
    val isCategoryDialogVisible: Boolean = false,
    val categoryToDelete: CategoryEntity? = null,
    val categoryDeleteCount: Int = 0,
    val newCategoryName: String = "",
    val newCategoryType: TransactionType = TransactionType.EXPENSE,
    val newCategoryIcon: String = "cart",
    val newCategoryColor: Long = 0xFFEF4444,

    // Month Picker
    val isMonthPickerVisible: Boolean = false
)
