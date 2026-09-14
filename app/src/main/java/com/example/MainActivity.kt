package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.TransactionType
import com.example.data.repository.BudgetRepository
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.AddLoanDialog
import com.example.ui.components.BudgetTopBar
import com.example.ui.components.ChangePasswordDialog
import com.example.ui.components.CreateProfileDialog
import com.example.ui.components.DeleteCategoryConfirmDialog
import com.example.ui.components.DeleteLoanConfirmDialog
import com.example.ui.components.DeleteProfileDialog
import com.example.ui.components.ExportDataDialog
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.components.ProfileListDialog
import com.example.ui.components.SwitchProfileDialog
import com.example.ui.components.TransactionBottomSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoansScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.BudgetUiState
import com.example.ui.viewmodel.BudgetViewModel
import com.example.ui.viewmodel.BudgetViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = BudgetRepository(
                categoryDao = database.categoryDao(),
                transactionDao = database.transactionDao(),
                savedExpenseDao = database.savedExpenseDao(),
                userProfileDao = database.userProfileDao(),
                loanDao = database.loanDao()
            )
            val viewModel: BudgetViewModel = viewModel(
                factory = BudgetViewModelFactory(repository)
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = state.isDarkMode, dynamicColor = false) {
                BudgetApp(viewModel = viewModel, state = state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetApp(viewModel: BudgetViewModel, state: BudgetUiState) {
    if (state.isWelcomeScreenVisible) {
        WelcomeScreen(viewModel = viewModel, state = state)
        CreateProfileDialog(viewModel = viewModel, state = state)
        SwitchProfileDialog(viewModel = viewModel, state = state)
        ProfileListDialog(viewModel = viewModel, state = state)
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        topBar = {
            BudgetTopBar(viewModel = viewModel, state = state)
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = state.currentDestination == AppDestination.DASHBOARD,
                    onClick = { viewModel.setDestination(AppDestination.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentDestination == AppDestination.DASHBOARD)
                                Icons.Filled.PieChart else Icons.Outlined.PieChart,
                            contentDescription = "Özet"
                        )
                    },
                    label = {
                        Text(
                            text = "Özet",
                            fontWeight = if (state.currentDestination == AppDestination.DASHBOARD)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )

                NavigationBarItem(
                    selected = state.currentDestination == AppDestination.HISTORY,
                    onClick = { viewModel.setDestination(AppDestination.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentDestination == AppDestination.HISTORY)
                                Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "Kayıtlar"
                        )
                    },
                    label = {
                        Text(
                            text = "Kayıtlar",
                            fontWeight = if (state.currentDestination == AppDestination.HISTORY)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("nav_item_history")
                )

                NavigationBarItem(
                    selected = state.currentDestination == AppDestination.LOANS,
                    onClick = { viewModel.setDestination(AppDestination.LOANS) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentDestination == AppDestination.LOANS)
                                Icons.Filled.AccountBalance else Icons.Outlined.AccountBalance,
                            contentDescription = "Krediler"
                        )
                    },
                    label = {
                        Text(
                            text = "Krediler",
                            fontWeight = if (state.currentDestination == AppDestination.LOANS)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("nav_item_loans")
                )

                NavigationBarItem(
                    selected = state.currentDestination == AppDestination.SETTINGS,
                    onClick = { viewModel.setDestination(AppDestination.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (state.currentDestination == AppDestination.SETTINGS)
                                Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Ayarlar"
                        )
                    },
                    label = {
                        Text(
                            text = "Ayarlar",
                            fontWeight = if (state.currentDestination == AppDestination.SETTINGS)
                                FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        },
        floatingActionButton = {
            if (state.currentDestination != AppDestination.SETTINGS) {
                FloatingActionButton(
                    onClick = {
                        if (state.currentDestination == AppDestination.LOANS) {
                            viewModel.openAddLoanDialog()
                        } else {
                            viewModel.openAddTransaction(TransactionType.EXPENSE)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.testTag("main_add_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (state.currentDestination == AppDestination.LOANS) "Kredi Ekle" else "Kayıt Ekle",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentDestination) {
                AppDestination.DASHBOARD -> {
                    DashboardScreen(
                        state = state,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onMonthClick = { viewModel.openMonthPicker() },
                        onAddExpense = { viewModel.openAddTransaction(TransactionType.EXPENSE) },
                        onAddIncome = { viewModel.openAddTransaction(TransactionType.INCOME) },
                        onSelectSavedExpense = { preset ->
                            viewModel.openAddTransaction(
                                initialType = TransactionType.EXPENSE,
                                presetTitle = preset.title,
                                presetCategoryId = preset.categoryId,
                                presetAmount = preset.defaultAmount
                            )
                        },
                        onItemClick = { item -> viewModel.openEditTransaction(item) },
                        onViewAllClick = { viewModel.setDestination(AppDestination.HISTORY) },
                        onNavigateToLoans = { viewModel.setDestination(AppDestination.LOANS) }
                    )
                }

                AppDestination.HISTORY -> {
                    HistoryScreen(
                        state = state,
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onMonthClick = { viewModel.openMonthPicker() },
                        onTypeFilterChange = { type -> viewModel.setTypeFilter(type) },
                        onCategoryFilterChange = { catId -> viewModel.setCategoryFilter(catId) },
                        onSearchChange = { query -> viewModel.setSearchQuery(query) },
                        onFilterMonthToggle = { onlyMonth -> viewModel.setFilterOnlySelectedMonth(onlyMonth) },
                        onItemClick = { item -> viewModel.openEditTransaction(item) }
                    )
                }

                AppDestination.LOANS -> {
                    LoansScreen(
                        loans = state.allLoans,
                        totalRemainingDebt = state.totalRemainingDebt,
                        monthlyLoansTotal = state.monthlyLoansTotal,
                        activeLoansCount = state.activeLoansCount,
                        onAddNewLoanClick = { viewModel.openAddLoanDialog() },
                        onToggleInstallmentPaid = { installment -> viewModel.toggleInstallmentPaid(installment) },
                        onDeleteLoanClick = { loan -> viewModel.requestDeleteLoan(loan) }
                    )
                }

                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        state = state,
                        onOpenCreateProfile = { viewModel.openCreateProfileDialog() },
                        onOpenSwitchProfile = { viewModel.openSwitchProfileDialog() },
                        onChangePassword = { viewModel.openChangePasswordDialog() },
                        onDeleteProfile = { user -> viewModel.openDeleteProfileDialog(user) },
                        onExportData = { viewModel.openExportDialog() },
                        onLogout = { viewModel.logoutToWelcome() },
                        onToggleDarkMode = { darkMode -> viewModel.setDarkMode(darkMode) },
                        onAddCategoryClick = { viewModel.openAddCategoryDialog() },
                        onDeleteCategoryClick = { category -> viewModel.requestDeleteCategory(category) }
                    )
                }
            }
        }
    }

    // Profile Management Dialogs
    CreateProfileDialog(viewModel = viewModel, state = state)
    SwitchProfileDialog(viewModel = viewModel, state = state)
    ChangePasswordDialog(viewModel = viewModel, state = state)
    DeleteProfileDialog(viewModel = viewModel, state = state)
    ExportDataDialog(viewModel = viewModel, state = state)
    ProfileListDialog(viewModel = viewModel, state = state)

    // Add Loan Dialog
    if (state.isAddLoanDialogVisible) {
        AddLoanDialog(
            isVisible = state.isAddLoanDialogVisible,
            bankName = state.addLoanBankName,
            monthlyAmount = state.addLoanMonthlyAmount,
            totalInstallments = state.addLoanTotalInstallments,
            paidInstallments = state.addLoanPaidInstallments,
            startYear = state.addLoanStartYear,
            startMonth = state.addLoanStartMonth,
            dueDay = state.addLoanDueDay,
            errorMessage = state.addLoanError,
            onBankNameChange = { viewModel.updateAddLoanBankName(it) },
            onMonthlyAmountChange = { viewModel.updateAddLoanMonthlyAmount(it) },
            onTotalInstallmentsChange = { viewModel.updateAddLoanTotalInstallments(it) },
            onPaidInstallmentsChange = { viewModel.updateAddLoanPaidInstallments(it) },
            onStartDateChange = { year, month -> viewModel.updateAddLoanStartDate(year, month) },
            onDueDayChange = { viewModel.updateAddLoanDueDay(it) },
            onConfirm = { viewModel.submitAddLoan() },
            onDismiss = { viewModel.closeAddLoanDialog() }
        )
    }

    // Delete Loan Confirmation Dialog
    if (state.isDeleteLoanDialogVisible) {
        DeleteLoanConfirmDialog(
            isVisible = state.isDeleteLoanDialogVisible,
            loan = state.loanToDelete,
            onConfirm = { viewModel.confirmDeleteLoan() },
            onDismiss = { viewModel.closeDeleteLoanDialog() }
        )
    }

    // Modal Bottom Sheet for Adding / Editing Transaction
    if (state.isBottomSheetVisible) {
        TransactionBottomSheet(
            sheetState = sheetState,
            isEditing = state.editingTransactionId != null,
            type = state.formType,
            amount = state.formAmount,
            title = state.formTitle,
            categoryId = state.formCategoryId,
            dateTimestamp = state.formDateTimestamp,
            notes = state.formNotes,
            errorMessage = state.formError,
            categories = state.allCategories,
            savedExpenses = state.savedExpenses,
            saveAsTemplate = state.formSaveAsTemplate,
            onSaveAsTemplateChange = { viewModel.updateFormSaveAsTemplate(it) },
            onSelectSavedExpense = { viewModel.selectSavedExpense(it) },
            onDeleteSavedExpense = { viewModel.deleteSavedExpense(it) },
            onTypeChange = { viewModel.updateFormType(it) },
            onAmountChange = { viewModel.updateFormAmount(it) },
            onTitleChange = { viewModel.updateFormTitle(it) },
            onCategoryChange = { viewModel.updateFormCategory(it) },
            onDateChange = { viewModel.updateFormDate(it) },
            onNotesChange = { viewModel.updateFormNotes(it) },
            onSave = { viewModel.saveTransaction() },
            onDelete = { viewModel.deleteCurrentEditingTransaction() },
            onDismiss = { viewModel.closeBottomSheet() }
        )
    }

    // Month & Year Picker Dialog
    if (state.isMonthPickerVisible) {
        MonthYearPickerDialog(
            currentCalendar = state.selectedCalendar,
            onSelect = { year, month -> viewModel.setMonthAndYear(year, month) },
            onDismiss = { viewModel.closeMonthPicker() }
        )
    }

    // Add New Category Dialog
    if (state.isCategoryDialogVisible) {
        AddCategoryDialog(
            name = state.newCategoryName,
            type = state.newCategoryType,
            selectedIcon = state.newCategoryIcon,
            selectedColor = state.newCategoryColor,
            onNameChange = { viewModel.updateNewCategoryName(it) },
            onTypeChange = { viewModel.updateNewCategoryType(it) },
            onIconChange = { viewModel.updateNewCategoryIcon(it) },
            onColorChange = { viewModel.updateNewCategoryColor(it) },
            onConfirm = { viewModel.saveCategory() },
            onDismiss = { viewModel.closeAddCategoryDialog() }
        )
    }

    // Delete Category Confirmation Dialog
    state.categoryToDelete?.let { category ->
        DeleteCategoryConfirmDialog(
            category = category,
            transactionCount = state.categoryDeleteCount,
            onConfirm = { viewModel.confirmDeleteCategory() },
            onDismiss = { viewModel.cancelDeleteCategory() }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
