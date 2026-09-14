package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionWithCategory
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.ExpenseRedText
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.IncomeGreenText
import com.example.ui.util.CategoryIcons
import com.example.ui.util.Formatters
import com.example.ui.viewmodel.BudgetUiState

@Composable
fun HistoryScreen(
    state: BudgetUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthClick: () -> Unit,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onCategoryFilterChange: (Long?) -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterMonthToggle: (Boolean) -> Unit,
    onItemClick: (TransactionWithCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter items according to active filters
    val filteredTransactions = remember(
        state.currentMonthTransactions,
        state.typeFilter,
        state.categoryFilterId,
        state.searchQuery
    ) {
        state.currentMonthTransactions.filter { item ->
            val matchesType = state.typeFilter == null || item.transaction.type == state.typeFilter.name
            val matchesCategory = state.categoryFilterId == null || item.transaction.categoryId == state.categoryFilterId
            val matchesSearch = state.searchQuery.isBlank() ||
                    item.transaction.title.contains(state.searchQuery, ignoreCase = true) ||
                    item.transaction.notes.contains(state.searchQuery, ignoreCase = true) ||
                    (item.category?.name?.contains(state.searchQuery, ignoreCase = true) == true)
            matchesType && matchesCategory && matchesSearch
        }
    }

    // Expense transactions
    val expenseItems = remember(filteredTransactions) {
        filteredTransactions
            .filter { it.transaction.type == TransactionType.EXPENSE.name }
            .sortedByDescending { it.transaction.dateTimestamp }
    }

    // Income transactions
    val incomeItems = remember(filteredTransactions) {
        filteredTransactions
            .filter { it.transaction.type == TransactionType.INCOME.name }
            .sortedByDescending { it.transaction.dateTimestamp }
    }

    // Group expenses by category
    val expenseCategories = remember(expenseItems) {
        val map = linkedMapOf<CategoryEntity, MutableList<TransactionWithCategory>>()
        for (item in expenseItems) {
            val cat = item.category ?: CategoryEntity(
                id = -1,
                name = "Diğer",
                type = "EXPENSE",
                iconName = "other",
                colorHex = 0xFF64748B,
                isDefault = true
            )
            map.getOrPut(cat) { mutableListOf() }.add(item)
        }
        map
    }

    // Group incomes by category
    val incomeCategories = remember(incomeItems) {
        val map = linkedMapOf<CategoryEntity, MutableList<TransactionWithCategory>>()
        for (item in incomeItems) {
            val cat = item.category ?: CategoryEntity(
                id = -1,
                name = "Diğer",
                type = "INCOME",
                iconName = "other",
                colorHex = 0xFF64748B,
                isDefault = true
            )
            map.getOrPut(cat) { mutableListOf() }.add(item)
        }
        map
    }

    val totalExpense = remember(expenseItems) { expenseItems.sumOf { it.transaction.amount } }
    val totalIncome = remember(incomeItems) { incomeItems.sumOf { it.transaction.amount } }

    var isExpensesExpanded by remember { mutableStateOf(false) }
    var isIncomesExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Month Selector Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.testTag("history_prev_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Önceki Ay"
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onMonthClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("history_month_selector_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Formatters.formatMonthYear(state.selectedCalendar),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.testTag("history_next_month_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Sonraki Ay"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input Field
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_search_input"),
            placeholder = { Text("Kayıt veya notlarda ara...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ara",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Temizle")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type Filter Chips: Tümü | Giderler | Gelirler
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.typeFilter == null,
                onClick = { onTypeFilterChange(null) },
                label = { Text("Tümü") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("filter_chip_all")
            )
            FilterChip(
                selected = state.typeFilter == TransactionType.EXPENSE,
                onClick = {
                    onTypeFilterChange(if (state.typeFilter == TransactionType.EXPENSE) null else TransactionType.EXPENSE)
                },
                label = { Text("Giderler") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                    selectedLabelColor = ExpenseRed
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("filter_chip_expense")
            )
            FilterChip(
                selected = state.typeFilter == TransactionType.INCOME,
                onClick = {
                    onTypeFilterChange(if (state.typeFilter == TransactionType.INCOME) null else TransactionType.INCOME)
                },
                label = { Text("Gelirler") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                    selectedLabelColor = IncomeGreen
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("filter_chip_income")
            )
        }

        // Horizontal Category Filter Chips
        if (state.allCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.categoryFilterId == null,
                    onClick = { onCategoryFilterChange(null) },
                    label = { Text("Tüm Kategoriler") },
                    shape = RoundedCornerShape(12.dp)
                )

                state.allCategories.forEach { category ->
                    val isSelected = state.categoryFilterId == category.id
                    val catColor = Color(category.colorHex)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onCategoryFilterChange(if (isSelected) null else category.id)
                        },
                        label = { Text(category.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryIcons.getIcon(category.iconName),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = catColor.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Transaction List Grouped by Type (Giderler / Gelirler) and Category
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Eşleşen kayıt bulunamadı",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Filtreleri veya arama kelimesini değiştirmeyi deneyebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("transactions_lazy_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ==================== GİDERLER BÖLÜMÜ ====================
                val showExpenses = state.typeFilter == null || state.typeFilter == TransactionType.EXPENSE
                if (showExpenses && expenseCategories.isNotEmpty()) {
                    item {
                        // Ana Giderler Başlığı (Dokunulduğunda kategoriler açılır/kapanır)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { isExpensesExpanded = !isExpensesExpanded }
                                .testTag("expenses_main_header"),
                            shape = RoundedCornerShape(16.dp),
                            color = ExpenseRedContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "GİDERLER",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ExpenseRedText
                                        )
                                        Text(
                                            text = "${expenseItems.size} kayıt",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ExpenseRedText.copy(alpha = 0.75f)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "-" + Formatters.formatCurrency(totalExpense),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = if (isExpensesExpanded) "Daralt" else "Genişlet",
                                        tint = ExpenseRed,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .rotate(if (isExpensesExpanded) 180f else 0f)
                                    )
                                }
                            }
                        }
                    }

                    // Her kategori için kart ve altındaki kayıtlar (Giderler ana başlığı açıksa gösterilir)
                    if (isExpensesExpanded) {
                        items(expenseCategories.keys.toList(), key = { "expense_cat_${it.id}" }) { category ->
                            val transactions = expenseCategories[category] ?: emptyList()
                            CategoryGroupCard(
                                category = category,
                                transactions = transactions,
                                isIncome = false,
                                onItemClick = onItemClick
                            )
                        }
                    }
                }

                // ==================== GELİRLER BÖLÜMÜ ====================
                val showIncomes = state.typeFilter == null || state.typeFilter == TransactionType.INCOME
                if (showIncomes && incomeCategories.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        // Ana Gelirler Başlığı (Dokunulduğunda kategoriler açılır/kapanır)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { isIncomesExpanded = !isIncomesExpanded }
                                .testTag("incomes_main_header"),
                            shape = RoundedCornerShape(16.dp),
                            color = IncomeGreenContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "GELİRLER",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = IncomeGreenText
                                        )
                                        Text(
                                            text = "${incomeItems.size} kayıt",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IncomeGreenText.copy(alpha = 0.75f)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+" + Formatters.formatCurrency(totalIncome),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = if (isIncomesExpanded) "Daralt" else "Genişlet",
                                        tint = IncomeGreen,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .rotate(if (isIncomesExpanded) 180f else 0f)
                                    )
                                }
                            }
                        }
                    }

                    // Her gelir kategorisi için kart ve kayıtlar (Gelirler ana başlığı açıksa gösterilir)
                    if (isIncomesExpanded) {
                        items(incomeCategories.keys.toList(), key = { "income_cat_${it.id}" }) { category ->
                            val transactions = incomeCategories[category] ?: emptyList()
                            CategoryGroupCard(
                                category = category,
                                transactions = transactions,
                                isIncome = true,
                                onItemClick = onItemClick
                            )
                        }
                    }
                }

                // Kayıtlar bölümünün en altında o ay girilen gelir ve kalan bakiye özeti
                item {
                    val remainingBalance = totalIncome - totalExpense
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_monthly_balance_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Aylık Durum Özeti",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreen)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Aylık Toplam Gelir:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = Formatters.formatCurrency(totalIncome),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRed)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Aylık Toplam Gider:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = Formatters.formatCurrency(totalExpense),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kalan Net Bakiye:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = Formatters.formatCurrency(remainingBalance),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (remainingBalance >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}

/**
 * Kategori Kartı: Başlık altında kategorinin ikonu, adı, toplam tutarı ve içerdiği tüm kayıtları gösterir.
 */
@Composable
fun CategoryGroupCard(
    category: CategoryEntity,
    transactions: List<TransactionWithCategory>,
    isIncome: Boolean,
    onItemClick: (TransactionWithCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val catColor = Color(category.colorHex)
    val catTotal = transactions.sumOf { it.transaction.amount }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_group_${category.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Kategori Başlık Satırı (Dokunulduğunda alt kayıtlar açılır/kapanır)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(catColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryIcons.getIcon(category.iconName),
                            contentDescription = category.name,
                            tint = catColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${transactions.size} kayıt",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (if (isIncome) "+" else "-") + Formatters.formatCurrency(catTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) IncomeGreen else ExpenseRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Daralt" else "Genişlet",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(if (isExpanded) 180f else 0f)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Kategoriye ait gider/gelir kayıtları (Başlıkları kategori ismiyle aynı hizada: start = 48.dp)
                    transactions.forEachIndexed { index, item ->
                        CategorizedTransactionItem(
                            item = item,
                            isIncome = isIncome,
                            onClick = { onItemClick(item) }
                        )
                        if (index < transactions.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(start = 48.dp)
                                    .padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kategori içindeki tekil kayıt satırı:
 * Başlık ve detaylar tam üstündeki kategori ismiyle aynı hizada (start = 48.dp) başlar.
 */
@Composable
fun CategorizedTransactionItem(
    item: TransactionWithCategory,
    isIncome: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val t = item.transaction
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("transaction_item_${t.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 48.dp, end = 12.dp)
        ) {
            Text(
                text = t.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = Formatters.formatShortDate(t.dateTimestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (t.notes.isNotBlank()) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = t.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Text(
            text = (if (isIncome) "+" else "-") + Formatters.formatCurrency(t.amount),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isIncome) IncomeGreen else ExpenseRed
        )
    }
}
