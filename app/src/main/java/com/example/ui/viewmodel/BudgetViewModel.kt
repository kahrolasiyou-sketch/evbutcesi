package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategoryEntity
import com.example.data.model.LoanEntity
import com.example.data.model.LoanInstallmentEntity
import com.example.data.model.LoanWithInstallments
import com.example.data.model.SavedExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithCategory
import com.example.data.model.UserProfileEntity
import com.example.data.repository.BudgetRepository
import com.example.ui.util.Formatters
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private var cachedCategories: List<CategoryEntity> = emptyList()
    private var cachedTransactions: List<TransactionWithCategory> = emptyList()
    private var cachedLoans: List<LoanWithInstallments> = emptyList()

    private var profileDataJob: Job? = null
    private var savedExpensesJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategories()
            repository.ensureInitialUser()
        }
        observeGlobalUsers()
    }

    private fun observeGlobalUsers() {
        repository.allUsers.onEach { usersList ->
            _uiState.update { current ->
                val active = current.activeUser ?: usersList.firstOrNull { it.isActive } ?: usersList.firstOrNull()
                val selected = current.loginSelectedUser ?: active
                current.copy(
                    users = usersList,
                    loginSelectedUser = selected
                )
            }
        }.launchIn(viewModelScope)

        repository.activeUser.onEach { active ->
            _uiState.update { current ->
                val selected = current.loginSelectedUser ?: active
                current.copy(
                    activeUser = active,
                    loginSelectedUser = selected
                )
            }
            if (active != null) {
                observeActiveProfileData(active.id)
            } else {
                profileDataJob?.cancel()
                savedExpensesJob?.cancel()
                cachedCategories = emptyList()
                cachedTransactions = emptyList()
                cachedLoans = emptyList()
                recomputeData(emptyList(), emptyList(), emptyList())
            }
        }.launchIn(viewModelScope)
    }

    private fun observeActiveProfileData(profileId: Long) {
        profileDataJob?.cancel()
        savedExpensesJob?.cancel()

        profileDataJob = combine(
            repository.getCategories(profileId),
            repository.getTransactions(profileId),
            repository.getLoans(profileId)
        ) { categories, allTransactions, allLoans ->
            cachedCategories = categories
            cachedTransactions = allTransactions
            cachedLoans = allLoans
            recomputeData(categories, allTransactions, allLoans)
        }.launchIn(viewModelScope)

        savedExpensesJob = repository.getSavedExpenses(profileId).onEach { savedList ->
            _uiState.update { it.copy(savedExpenses = savedList) }
        }.launchIn(viewModelScope)
    }

    private fun recomputeData(
        categories: List<CategoryEntity>,
        allTransactions: List<TransactionWithCategory>,
        allLoans: List<LoanWithInstallments> = cachedLoans
    ) {
        val calendar = _uiState.value.selectedCalendar
        val (startOfMonth, endOfMonth) = getMonthRange(calendar)

        val monthTransactions = allTransactions.filter { item ->
            item.transaction.dateTimestamp in startOfMonth..endOfMonth
        }

        var totalIncome = 0.0
        var variableExpense = 0.0
        val expenseMap = mutableMapOf<Long, Double>()

        monthTransactions.forEach { item ->
            val amount = item.transaction.amount
            if (item.transaction.type == TransactionType.INCOME.name) {
                totalIncome += amount
            } else {
                variableExpense += amount
                val catId = item.transaction.categoryId
                expenseMap[catId] = (expenseMap[catId] ?: 0.0) + amount
            }
        }

        val selYear = calendar.get(Calendar.YEAR)
        val selMonth = calendar.get(Calendar.MONTH) + 1

        var monthlyLoansTotal = 0.0
        var totalRemainingDebt = 0.0
        var activeLoansCount = 0

        allLoans.forEach { loanItem ->
            val hasRemaining = loanItem.actualRemainingCount > 0
            if (hasRemaining) {
                activeLoansCount++
                totalRemainingDebt += loanItem.remainingDebt
            }

            val matchingInstallment = loanItem.installments.find { inst ->
                inst.year == selYear && inst.month == selMonth
            }
            if (matchingInstallment != null) {
                monthlyLoansTotal += matchingInstallment.amount
            }
        }

        val totalExpense = variableExpense + monthlyLoansTotal
        val netBalance = totalIncome - totalExpense

        val categorySpendings = expenseMap.mapNotNull { (catId, amount) ->
            val cat = categories.find { it.id == catId } ?: return@mapNotNull null
            val percentage = if (variableExpense > 0) ((amount / variableExpense) * 100.0).toFloat() else 0f
            CategorySpending(category = cat, totalAmount = amount, percentage = percentage)
        }.sortedByDescending { it.totalAmount }

        _uiState.update {
            it.copy(
                allCategories = categories,
                currentMonthTransactions = monthTransactions,
                allLoans = allLoans,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                variableExpense = variableExpense,
                monthlyLoansTotal = monthlyLoansTotal,
                totalRemainingDebt = totalRemainingDebt,
                activeLoansCount = activeLoansCount,
                netBalance = netBalance,
                categorySpendings = categorySpendings
            )
        }
    }

    private fun getMonthRange(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    // Navigation
    fun setDestination(destination: AppDestination) {
        _uiState.update { it.copy(currentDestination = destination) }
    }

    // Month controls
    fun previousMonth() {
        val cal = _uiState.value.selectedCalendar.clone() as Calendar
        cal.add(Calendar.MONTH, -1)
        _uiState.update { it.copy(selectedCalendar = cal) }
        recomputeData(cachedCategories, cachedTransactions, cachedLoans)
    }

    fun nextMonth() {
        val cal = _uiState.value.selectedCalendar.clone() as Calendar
        cal.add(Calendar.MONTH, 1)
        _uiState.update { it.copy(selectedCalendar = cal) }
        recomputeData(cachedCategories, cachedTransactions, cachedLoans)
    }

    fun setMonth(year: Int, month: Int) {
        val cal = _uiState.value.selectedCalendar.clone() as Calendar
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        _uiState.update { it.copy(selectedCalendar = cal, isMonthPickerVisible = false) }
        recomputeData(cachedCategories, cachedTransactions, cachedLoans)
    }

    fun openMonthPicker() {
        _uiState.update { it.copy(isMonthPickerVisible = true) }
    }

    fun closeMonthPicker() {
        _uiState.update { it.copy(isMonthPickerVisible = false) }
    }

    // Bottom Sheet (Add/Edit Transaction)
    fun openAddTransaction(
        initialType: TransactionType = TransactionType.EXPENSE,
        presetTitle: String = "",
        presetCategoryId: Long? = null,
        presetAmount: Double? = null
    ) {
        val defaultCategory = if (presetCategoryId != null) {
            presetCategoryId
        } else {
            val matching = _uiState.value.allCategories.filter {
                it.type == initialType.name || it.type == "BOTH"
            }
            matching.firstOrNull { it.isDefault }?.id ?: matching.firstOrNull()?.id
        }

        val initialAmount = if (presetAmount != null && presetAmount > 0) {
            Formatters.formatAmountPlain(presetAmount)
        } else {
            ""
        }

        _uiState.update {
            it.copy(
                isBottomSheetVisible = true,
                editingTransactionId = null,
                formType = initialType,
                formAmount = initialAmount,
                formTitle = Formatters.capitalizeFirstChar(presetTitle),
                formCategoryId = defaultCategory,
                formDateTimestamp = System.currentTimeMillis(),
                formNotes = "",
                formSaveAsTemplate = false,
                formError = null
            )
        }
    }

    fun openEditTransaction(transactionWithCategory: TransactionWithCategory) {
        val t = transactionWithCategory.transaction
        val type = if (t.type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE
        val formattedAmount = Formatters.formatAmountPlain(t.amount)

        _uiState.update {
            it.copy(
                isBottomSheetVisible = true,
                editingTransactionId = t.id,
                formType = type,
                formAmount = formattedAmount,
                formTitle = t.title,
                formCategoryId = t.categoryId,
                formDateTimestamp = t.dateTimestamp,
                formNotes = t.notes,
                formSaveAsTemplate = false,
                formError = null
            )
        }
    }

    fun closeBottomSheet() {
        _uiState.update { it.copy(isBottomSheetVisible = false, editingTransactionId = null) }
    }

    fun updateFormType(type: TransactionType) {
        val currentCat = _uiState.value.allCategories.find { it.id == _uiState.value.formCategoryId }
        val isCompatible = currentCat != null && (currentCat.type == type.name || currentCat.type == "BOTH")

        val newCategoryId = if (isCompatible) {
            _uiState.value.formCategoryId
        } else {
            val matching = _uiState.value.allCategories.filter { it.type == type.name || it.type == "BOTH" }
            matching.firstOrNull { it.isDefault }?.id ?: matching.firstOrNull()?.id
        }

        _uiState.update {
            it.copy(
                formType = type,
                formCategoryId = newCategoryId
            )
        }
    }

    fun updateFormAmount(amount: String) {
        _uiState.update { it.copy(formAmount = amount, formError = null) }
    }

    fun updateFormTitle(title: String) {
        val capitalized = Formatters.capitalizeFirstChar(title)
        _uiState.update { it.copy(formTitle = capitalized, formError = null) }
    }

    fun updateFormCategory(categoryId: Long) {
        _uiState.update { it.copy(formCategoryId = categoryId, formError = null) }
    }

    fun updateFormDate(timestamp: Long) {
        _uiState.update { it.copy(formDateTimestamp = timestamp) }
    }

    fun updateFormNotes(notes: String) {
        val capitalizedNotes = Formatters.capitalizeFirstChar(notes)
        _uiState.update { it.copy(formNotes = capitalizedNotes) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amountDouble = Formatters.parseAmountInput(state.formAmount)

        if (amountDouble == null || amountDouble <= 0.0) {
            _uiState.update { it.copy(formError = "Lütfen geçerli bir tutar girin.") }
            return
        }

        val categoryId = state.formCategoryId
        if (categoryId == null) {
            _uiState.update { it.copy(formError = "Lütfen bir kategori seçin.") }
            return
        }

        if (state.formTitle.isBlank()) {
            _uiState.update { it.copy(formError = "Lütfen bir başlık giriniz") }
            return
        }

        val activeProfileId = state.activeUser?.id ?: 1L
        val title = state.formTitle.trim()

        val transaction = TransactionEntity(
            id = state.editingTransactionId ?: 0,
            profileId = activeProfileId,
            title = title,
            amount = amountDouble,
            type = state.formType.name,
            categoryId = categoryId,
            dateTimestamp = state.formDateTimestamp,
            notes = state.formNotes.trim()
        )

        viewModelScope.launch {
            if (state.editingTransactionId != null) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }

            if (state.formSaveAsTemplate && title.isNotBlank()) {
                repository.saveExpenseTemplate(
                    profileId = activeProfileId,
                    title = title,
                    categoryId = categoryId,
                    defaultAmount = amountDouble,
                    type = state.formType.name
                )
            }

            closeBottomSheet()
        }
    }

    fun deleteCurrentEditingTransaction() {
        val id = _uiState.value.editingTransactionId ?: return
        viewModelScope.launch {
            repository.deleteTransactionById(id)
            closeBottomSheet()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Filter controls
    fun setTypeFilter(type: TransactionType?) {
        _uiState.update { it.copy(typeFilter = type) }
    }

    fun setCategoryFilter(categoryId: Long?) {
        _uiState.update { it.copy(categoryFilterId = categoryId) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilterOnlySelectedMonth(onlyMonth: Boolean) {
        _uiState.update { it.copy(filterOnlySelectedMonth = onlyMonth) }
    }

    // Category Management
    fun openAddCategoryDialog() {
        _uiState.update {
            it.copy(
                isCategoryDialogVisible = true,
                newCategoryName = "",
                newCategoryType = TransactionType.EXPENSE,
                newCategoryIcon = "cart",
                newCategoryColor = 0xFFEF4444
            )
        }
    }

    fun closeAddCategoryDialog() {
        _uiState.update { it.copy(isCategoryDialogVisible = false) }
    }

    fun updateNewCategoryName(name: String) {
        val capitalized = Formatters.capitalizeFirstChar(name)
        _uiState.update { it.copy(newCategoryName = capitalized) }
    }

    fun updateNewCategoryType(type: TransactionType) {
        val defaultColor = if (type == TransactionType.INCOME) 0xFF10B981 else 0xFFEF4444
        val defaultIcon = if (type == TransactionType.INCOME) "salary" else "cart"
        _uiState.update {
            it.copy(
                newCategoryType = type,
                newCategoryColor = defaultColor,
                newCategoryIcon = defaultIcon
            )
        }
    }

    fun updateNewCategoryIcon(iconKey: String) {
        _uiState.update { it.copy(newCategoryIcon = iconKey) }
    }

    fun updateNewCategoryColor(colorHex: Long) {
        _uiState.update { it.copy(newCategoryColor = colorHex) }
    }

    fun saveCategory() {
        val state = _uiState.value
        val name = state.newCategoryName.trim()
        if (name.isBlank()) return

        val profileId = state.activeUser?.id ?: 1L

        val category = CategoryEntity(
            profileId = profileId,
            name = name,
            type = state.newCategoryType.name,
            iconName = state.newCategoryIcon,
            colorHex = state.newCategoryColor,
            isDefault = false
        )

        viewModelScope.launch {
            repository.insertCategory(category)
            closeAddCategoryDialog()
        }
    }

    fun requestDeleteCategory(category: CategoryEntity) {
        val profileId = _uiState.value.activeUser?.id ?: 1L
        viewModelScope.launch {
            val count = repository.getTransactionCountForCategory(profileId, category.id)
            _uiState.update {
                it.copy(
                    categoryToDelete = category,
                    categoryDeleteCount = count
                )
            }
        }
    }

    fun confirmDeleteCategory() {
        val cat = _uiState.value.categoryToDelete ?: return
        val profileId = _uiState.value.activeUser?.id ?: 1L
        viewModelScope.launch {
            repository.deleteCategory(cat, profileId)
            _uiState.update { it.copy(categoryToDelete = null, categoryDeleteCount = 0) }
        }
    }

    fun cancelDeleteCategory() {
        _uiState.update { it.copy(categoryToDelete = null, categoryDeleteCount = 0) }
    }

    // Theme Management
    fun setDarkMode(isDark: Boolean) {
        _uiState.update { it.copy(isDarkMode = isDark) }
    }

    // ==================== MULTI-PROFILE & ONBOARDING ====================

    fun selectLoginUser(user: UserProfileEntity) {
        _uiState.update {
            it.copy(
                loginSelectedUser = user,
                loginPinInput = "",
                loginError = null,
                isProfileListDialogVisible = false
            )
        }
    }

    fun openProfileListDialog() {
        _uiState.update { it.copy(isProfileListDialogVisible = true) }
    }

    fun closeProfileListDialog() {
        _uiState.update { it.copy(isProfileListDialogVisible = false) }
    }

    fun appendLoginPinDigit(digit: Char) {
        val current = _uiState.value.loginPinInput
        if (current.length >= 4) return
        val next = current + digit
        _uiState.update { it.copy(loginPinInput = next, loginError = null) }
        if (next.length == 4) {
            verifyLoginPin(next)
        }
    }

    fun deleteLoginPinDigit() {
        val current = _uiState.value.loginPinInput
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(loginPinInput = current.dropLast(1), loginError = null) }
        }
    }

    fun clearLoginPin() {
        _uiState.update { it.copy(loginPinInput = "", loginError = null) }
    }

    fun verifyLoginPin(enteredPin: String) {
        val selectedUser = _uiState.value.loginSelectedUser ?: _uiState.value.activeUser
        if (selectedUser == null) {
            _uiState.update { it.copy(loginError = "Lütfen bir profil seçin.") }
            return
        }

        if (selectedUser.isGuest || !selectedUser.isProtected) {
            unlockToUser(selectedUser)
            return
        }

        val isMatch = (selectedUser.pin.isNotBlank() && selectedUser.pin == enteredPin) ||
                (selectedUser.password.isNotBlank() && selectedUser.password == enteredPin)

        if (isMatch) {
            unlockToUser(selectedUser)
        } else {
            _uiState.update {
                it.copy(
                    loginPinInput = "",
                    loginError = "Hatalı 4 Haneli PIN! Lütfen tekrar deneyin."
                )
            }
        }
    }

    fun submitLogin() {
        val selectedUser = _uiState.value.loginSelectedUser ?: _uiState.value.activeUser
        if (selectedUser == null) {
            _uiState.update { it.copy(loginError = "Lütfen bir profil seçin.") }
            return
        }
        if (!selectedUser.isProtected) {
            unlockToUser(selectedUser)
        } else {
            verifyLoginPin(_uiState.value.loginPinInput)
        }
    }

    private fun unlockToUser(user: UserProfileEntity) {
        viewModelScope.launch {
            repository.switchProfile(user.id)
            _uiState.update {
                it.copy(
                    isWelcomeScreenVisible = false,
                    loginPinInput = "",
                    loginError = null,
                    activeUser = user
                )
            }
        }
    }

    fun dismissWelcomeScreen() {
        if (_uiState.value.activeUser != null) {
            _uiState.update { it.copy(isWelcomeScreenVisible = false) }
        }
    }

    fun activateGuestMode() {
        viewModelScope.launch {
            repository.activateGuestMode()
            _uiState.update {
                it.copy(
                    isWelcomeScreenVisible = false,
                    loginPinInput = "",
                    loginError = null
                )
            }
        }
    }

    fun logoutToWelcome() {
        _uiState.update {
            it.copy(
                isWelcomeScreenVisible = true,
                loginPinInput = "",
                loginError = null,
                loginSelectedUser = it.activeUser
            )
        }
    }

    // Create Profile Dialog
    fun openCreateProfileDialog(fromGuestMigration: Boolean = false) {
        _uiState.update {
            it.copy(
                isCreateProfileDialogVisible = true,
                createProfileName = "",
                createProfilePassword = "",
                createProfilePin = "",
                createProfileAvatar = "person",
                createProfileColor = 0xFF28C76F,
                createProfileMigrateGuestData = fromGuestMigration || (it.activeUser?.isGuest == true),
                createProfileError = null
            )
        }
    }

    fun closeCreateProfileDialog() {
        _uiState.update { it.copy(isCreateProfileDialogVisible = false, createProfileError = null) }
    }

    fun updateCreateProfileName(name: String) {
        _uiState.update { it.copy(createProfileName = Formatters.capitalizeFirstChar(name), createProfileError = null) }
    }

    fun updateCreateProfilePassword(password: String) {
        _uiState.update { it.copy(createProfilePassword = password, createProfileError = null) }
    }

    fun updateCreateProfilePin(pin: String) {
        val filtered = pin.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(createProfilePin = filtered, createProfileError = null) }
    }

    fun updateCreateProfileAvatar(avatar: String) {
        _uiState.update { it.copy(createProfileAvatar = avatar) }
    }

    fun updateCreateProfileColor(colorHex: Long) {
        _uiState.update { it.copy(createProfileColor = colorHex) }
    }

    fun updateCreateProfileMigrateGuestData(migrate: Boolean) {
        _uiState.update { it.copy(createProfileMigrateGuestData = migrate) }
    }

    fun confirmCreateProfile() {
        val state = _uiState.value
        val name = state.createProfileName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(createProfileError = "Lütfen bir profil adı belirleyin.") }
            return
        }

        val pin = state.createProfilePin.trim()
        if (pin.isNotEmpty() && pin.length != 4) {
            _uiState.update { it.copy(createProfileError = "PIN kodu tam olarak 4 haneli olmalıdır.") }
            return
        }

        val currentGuestId = if (state.activeUser?.isGuest == true) state.activeUser.id else null
        val shouldMigrate = state.createProfileMigrateGuestData && currentGuestId != null

        viewModelScope.launch {
            val newProfileId = repository.createProfile(
                name = name,
                password = "",
                pin = pin,
                avatarName = state.createProfileAvatar,
                colorHex = state.createProfileColor,
                setAsActive = true
            )

            if (shouldMigrate && currentGuestId != null) {
                repository.transferGuestDataToProfile(currentGuestId, newProfileId)
            }

            closeCreateProfileDialog()
            _uiState.update {
                it.copy(
                    isWelcomeScreenVisible = false,
                    loginPinInput = "",
                    loginError = null
                )
            }
        }
    }

    // Switch Profile Dialog
    fun openSwitchProfileDialog(targetUser: UserProfileEntity? = null) {
        _uiState.update {
            it.copy(
                isSwitchProfileDialogVisible = true,
                targetProfileToSwitch = targetUser,
                switchProfilePasswordInput = "",
                switchProfilePinInput = "",
                switchProfileError = null
            )
        }
    }

    fun closeSwitchProfileDialog() {
        _uiState.update {
            it.copy(
                isSwitchProfileDialogVisible = false,
                targetProfileToSwitch = null,
                switchProfilePasswordInput = "",
                switchProfilePinInput = "",
                switchProfileError = null
            )
        }
    }

    fun selectTargetProfileToSwitch(user: UserProfileEntity) {
        if (!user.isProtected) {
            // Unprotected profile: switch immediately
            viewModelScope.launch {
                repository.switchProfile(user.id)
                closeSwitchProfileDialog()
                _uiState.update {
                    it.copy(
                        isWelcomeScreenVisible = false,
                        activeUser = user,
                        loginSelectedUser = user
                    )
                }
            }
        } else {
            // Protected profile: prompt for 4-digit PIN
            _uiState.update {
                it.copy(
                    isSwitchProfileDialogVisible = true,
                    targetProfileToSwitch = user,
                    switchProfilePasswordInput = "",
                    switchProfilePinInput = "",
                    switchProfileError = null
                )
            }
        }
    }

    fun updateSwitchProfilePassword(input: String) {
        val filtered = input.filter { it.isDigit() }.take(4)
        _uiState.update {
            it.copy(
                switchProfilePasswordInput = filtered,
                switchProfilePinInput = filtered,
                switchProfileError = null
            )
        }
    }

    fun updateSwitchProfilePin(input: String) {
        updateSwitchProfilePassword(input)
    }

    fun confirmSwitchProfile() {
        val state = _uiState.value
        val target = state.targetProfileToSwitch ?: return
        val entered = state.switchProfilePinInput.ifBlank { state.switchProfilePasswordInput }.trim()

        if (target.isProtected) {
            val matchesPassword = target.password.isNotBlank() && target.password == entered
            val matchesPin = target.pin.isNotBlank() && target.pin == entered

            if (!matchesPassword && !matchesPin) {
                _uiState.update { it.copy(switchProfileError = "Hatalı 4 Haneli PIN! Lütfen tekrar deneyin.") }
                return
            }
        }

        viewModelScope.launch {
            repository.switchProfile(target.id)
            closeSwitchProfileDialog()
            _uiState.update {
                it.copy(
                    isWelcomeScreenVisible = false,
                    activeUser = target,
                    loginSelectedUser = target,
                    loginPinInput = ""
                )
            }
        }
    }

    // Change PIN / Password Dialog
    fun openChangePasswordDialog() {
        _uiState.update {
            it.copy(
                isChangePasswordDialogVisible = true,
                currentPasswordInput = "",
                currentPinInput = "",
                newPasswordInput = "",
                newPinInput = "",
                confirmNewPinInput = "",
                changePasswordError = null,
                changePinError = null
            )
        }
    }

    fun closeChangePasswordDialog() {
        _uiState.update {
            it.copy(
                isChangePasswordDialogVisible = false,
                changePasswordError = null,
                changePinError = null
            )
        }
    }

    fun updateCurrentPasswordInput(input: String) {
        val filtered = input.filter { it.isDigit() }.take(4)
        _uiState.update {
            it.copy(
                currentPasswordInput = filtered,
                currentPinInput = filtered,
                changePasswordError = null
            )
        }
    }

    fun updateCurrentPinInput(input: String) {
        updateCurrentPasswordInput(input)
    }

    fun updateNewPasswordInput(input: String) {
        val filtered = input.filter { it.isDigit() }.take(4)
        _uiState.update {
            it.copy(
                newPasswordInput = filtered,
                newPinInput = filtered,
                changePasswordError = null
            )
        }
    }

    fun updateNewPinInput(input: String) {
        updateNewPasswordInput(input)
    }

    fun updateConfirmNewPinInput(input: String) {
        val filtered = input.filter { it.isDigit() }.take(4)
        _uiState.update {
            it.copy(
                confirmNewPinInput = filtered,
                changePasswordError = null
            )
        }
    }

    fun confirmChangePassword() {
        val state = _uiState.value
        val active = state.activeUser ?: return
        val currentEntered = state.currentPinInput.ifBlank { state.currentPasswordInput }.trim()
        val newPin = state.newPinInput.ifBlank { state.newPasswordInput }.trim()
        val confirmPin = state.confirmNewPinInput.trim()

        if (active.isProtected) {
            val matchesPassword = active.password.isNotBlank() && active.password == currentEntered
            val matchesPin = active.pin.isNotBlank() && active.pin == currentEntered

            if (!matchesPassword && !matchesPin) {
                _uiState.update { it.copy(changePasswordError = "Mevcut 4 haneli PIN hatalı.") }
                return
            }
        }

        if (newPin.isNotEmpty() && newPin.length != 4) {
            _uiState.update { it.copy(changePasswordError = "Yeni PIN tam olarak 4 haneli olmalıdır.") }
            return
        }

        if (confirmPin.isNotEmpty() && newPin != confirmPin) {
            _uiState.update { it.copy(changePasswordError = "Girdiğiniz PIN kodları uyuşmuyor.") }
            return
        }

        viewModelScope.launch {
            repository.updatePassword(
                userId = active.id,
                newPassword = "",
                newPin = newPin
            )
            closeChangePasswordDialog()
        }
    }

    // Delete Profile Dialog
    fun openDeleteProfileDialog(user: UserProfileEntity) {
        _uiState.update {
            it.copy(
                isDeleteProfileDialogVisible = true,
                profileToDelete = user,
                deleteProfilePasswordInput = "",
                deleteProfilePinInput = "",
                deleteProfileError = null
            )
        }
    }

    fun closeDeleteProfileDialog() {
        _uiState.update {
            it.copy(
                isDeleteProfileDialogVisible = false,
                profileToDelete = null,
                deleteProfilePasswordInput = "",
                deleteProfilePinInput = "",
                deleteProfileError = null
            )
        }
    }

    fun updateDeleteProfilePasswordInput(input: String) {
        val filtered = input.filter { it.isDigit() }.take(4)
        _uiState.update {
            it.copy(
                deleteProfilePasswordInput = filtered,
                deleteProfilePinInput = filtered,
                deleteProfileError = null
            )
        }
    }

    fun updateDeleteProfilePinInput(input: String) {
        updateDeleteProfilePasswordInput(input)
    }

    fun confirmDeleteProfile() {
        val state = _uiState.value
        val target = state.profileToDelete ?: return
        val entered = state.deleteProfilePinInput.ifBlank { state.deleteProfilePasswordInput }.trim()

        if (target.isProtected) {
            val matchesPassword = target.password.isNotBlank() && target.password == entered
            val matchesPin = target.pin.isNotBlank() && target.pin == entered

            if (!matchesPassword && !matchesPin) {
                _uiState.update { it.copy(deleteProfileError = "Hatalı 4 haneli PIN! Profil silinemedi.") }
                return
            }
        }

        viewModelScope.launch {
            repository.deleteProfileAndAllData(target)
            closeDeleteProfileDialog()
        }
    }

    // Export Data Dialog
    fun openExportDialog() {
        val activeId = _uiState.value.activeUser?.id ?: 1L
        viewModelScope.launch {
            val format = _uiState.value.exportFormat
            val content = if (format == "CSV") {
                repository.exportDataCsv(activeId)
            } else {
                repository.exportDataJson(activeId)
            }
            _uiState.update {
                it.copy(
                    isExportDialogVisible = true,
                    exportDataText = content,
                    exportToastMessage = null
                )
            }
        }
    }

    fun setExportFormat(format: String) {
        val activeId = _uiState.value.activeUser?.id ?: 1L
        viewModelScope.launch {
            val content = if (format == "CSV") {
                repository.exportDataCsv(activeId)
            } else {
                repository.exportDataJson(activeId)
            }
            _uiState.update {
                it.copy(
                    exportFormat = format,
                    exportDataText = content
                )
            }
        }
    }

    fun closeExportDialog() {
        _uiState.update { it.copy(isExportDialogVisible = false, exportToastMessage = null) }
    }

    // Loans Module Functions
    fun openAddLoanDialog() {
        val now = Calendar.getInstance()
        _uiState.update {
            it.copy(
                isAddLoanDialogVisible = true,
                addLoanBankName = "",
                addLoanMonthlyAmount = "",
                addLoanTotalInstallments = "12",
                addLoanPaidInstallments = "0",
                addLoanStartYear = now.get(Calendar.YEAR),
                addLoanStartMonth = now.get(Calendar.MONTH) + 1,
                addLoanDueDay = 15,
                addLoanError = null
            )
        }
    }

    fun closeAddLoanDialog() {
        _uiState.update { it.copy(isAddLoanDialogVisible = false, addLoanError = null) }
    }

    fun updateAddLoanBankName(name: String) {
        _uiState.update { it.copy(addLoanBankName = name, addLoanError = null) }
    }

    fun updateAddLoanMonthlyAmount(amount: String) {
        _uiState.update { it.copy(addLoanMonthlyAmount = amount, addLoanError = null) }
    }

    fun updateAddLoanTotalInstallments(total: String) {
        _uiState.update { it.copy(addLoanTotalInstallments = total, addLoanError = null) }
    }

    fun updateAddLoanPaidInstallments(paid: String) {
        _uiState.update { it.copy(addLoanPaidInstallments = paid, addLoanError = null) }
    }

    fun updateAddLoanDueDay(day: Int) {
        _uiState.update { it.copy(addLoanDueDay = day) }
    }

    fun updateAddLoanStartDate(year: Int, month: Int) {
        _uiState.update { it.copy(addLoanStartYear = year, addLoanStartMonth = month) }
    }

    fun submitAddLoan() {
        saveLoan()
    }

    fun setMonthAndYear(year: Int, month: Int) {
        setMonth(year, month)
    }

    fun updateFormSaveAsTemplate(save: Boolean) {
        _uiState.update { it.copy(formSaveAsTemplate = save) }
    }

    fun selectSavedExpense(saved: SavedExpenseEntity) {
        val currentAmountStr = _uiState.value.formAmount
        val hasEnteredAmount = currentAmountStr.any { it.isDigit() && it != '0' }
        val type = if (saved.type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE

        _uiState.update { current ->
            // Eğer kullanıcı formda önceden bir tutar girdiyse (0'dan farklı rakam varsa), o tutarı kesinlikle koru!
            // Sadece formdaki tutar boşsa veya 0 ise ve şablonda varsayılan bir tutar tanımlıysa şablon tutarını yükle.
            val resolvedAmount = if (hasEnteredAmount) {
                current.formAmount
            } else if (saved.defaultAmount != null && saved.defaultAmount > 0.0) {
                Formatters.formatAmountFromDouble(saved.defaultAmount)
            } else {
                current.formAmount
            }

            current.copy(
                formTitle = saved.title,
                formAmount = resolvedAmount,
                formCategoryId = saved.categoryId ?: current.formCategoryId,
                formType = type
            )
        }
    }

    fun deleteSavedExpense(savedId: Long) {
        viewModelScope.launch {
            repository.deleteSavedExpenseById(savedId)
        }
    }

    fun deleteSavedExpense(saved: SavedExpenseEntity) {
        viewModelScope.launch {
            repository.deleteSavedExpenseById(saved.id)
        }
    }

    fun openAddUserDialog() {
        openCreateProfileDialog()
    }

    fun closeAddUserDialog() {
        closeCreateProfileDialog()
    }

    fun switchActiveUser(userId: Long) {
        val target = _uiState.value.users.find { it.id == userId }
        if (target != null) {
            selectTargetProfileToSwitch(target)
        }
    }

    fun requestDeleteUser(user: UserProfileEntity) {
        openDeleteProfileDialog(user)
    }

    fun saveLoan() {
        val state = _uiState.value
        val bankName = state.addLoanBankName.trim()
        if (bankName.isBlank()) {
            _uiState.update { it.copy(addLoanError = "Lütfen banka veya kredi adını girin.") }
            return
        }

        val amount = Formatters.parseAmountInput(state.addLoanMonthlyAmount)
        if (amount == null || amount <= 0.0) {
            _uiState.update { it.copy(addLoanError = "Lütfen geçerli bir aylık taksit tutarı girin.") }
            return
        }

        val totalInstallments = state.addLoanTotalInstallments.toIntOrNull()
        if (totalInstallments == null || totalInstallments <= 0) {
            _uiState.update { it.copy(addLoanError = "Toplam taksit sayısı en az 1 olmalıdır.") }
            return
        }

        val paidInstallments = state.addLoanPaidInstallments.toIntOrNull() ?: 0
        if (paidInstallments > totalInstallments) {
            _uiState.update { it.copy(addLoanError = "Ödenen taksit sayısı toplam taksit sayısından fazla olamaz.") }
            return
        }

        val activeProfileId = state.activeUser?.id ?: 1L
        val dueDay = state.addLoanDueDay.coerceIn(1, 31)

        val newLoan = LoanEntity(
            profileId = activeProfileId,
            bankName = bankName,
            monthlyInstallment = amount,
            totalInstallments = totalInstallments,
            paidInstallmentsCount = paidInstallments,
            startYear = state.addLoanStartYear,
            startMonth = state.addLoanStartMonth,
            dueDayOfMonth = dueDay
        )

        viewModelScope.launch {
            repository.addLoanWithInstallments(newLoan, paidInstallments)
            closeAddLoanDialog()
        }
    }

    fun toggleInstallmentPaid(installment: LoanInstallmentEntity) {
        viewModelScope.launch {
            repository.toggleInstallmentPaid(
                installmentId = installment.id,
                loanId = installment.loanId,
                isPaid = !installment.isPaid
            )
        }
    }

    fun requestDeleteLoan(loan: LoanEntity) {
        _uiState.update {
            it.copy(
                isDeleteLoanDialogVisible = true,
                loanToDelete = loan
            )
        }
    }

    fun closeDeleteLoanDialog() {
        _uiState.update {
            it.copy(
                isDeleteLoanDialogVisible = false,
                loanToDelete = null
            )
        }
    }

    fun confirmDeleteLoan() {
        val loan = _uiState.value.loanToDelete ?: return
        viewModelScope.launch {
            repository.deleteLoan(loan)
            closeDeleteLoanDialog()
        }
    }
}

class BudgetViewModelFactory(
    private val repository: BudgetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
