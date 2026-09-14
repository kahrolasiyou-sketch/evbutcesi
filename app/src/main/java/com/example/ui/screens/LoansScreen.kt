package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LoanEntity
import com.example.data.model.LoanInstallmentEntity
import com.example.data.model.LoanWithInstallments
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LoanAccentGreen
import com.example.ui.theme.LoanAccentRed
import com.example.ui.util.Formatters

@Composable
fun LoansScreen(
    loans: List<LoanWithInstallments>,
    totalRemainingDebt: Double,
    monthlyLoansTotal: Double,
    activeLoansCount: Int,
    onAddNewLoanClick: () -> Unit,
    onToggleInstallmentPaid: (LoanInstallmentEntity) -> Unit,
    onDeleteLoanClick: (LoanEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Açık / kapalı kartların state'i (loanId -> isExpanded)
    val expandedStates = remember { mutableStateMapOf<Long, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Üst Başlık & "+ Yeni Kredi Ekle" Butonu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Krediler",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (activeLoansCount > 0) "$activeLoansCount aktif kredi takibi" else "Kredi ve taksit planlaması",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onAddNewLoanClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier.testTag("add_new_loan_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Yeni Kredi Ekle",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Özet Finansal Durum Kartı (Kalan Borç & Aylık Toplam Taksit)
        if (loans.isNotEmpty()) {
            LoansOverviewHeaderCard(
                totalRemainingDebt = totalRemainingDebt,
                monthlyLoansTotal = monthlyLoansTotal,
                activeLoansCount = activeLoansCount,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        }

        // Kredi Kartları Listesi (LazyColumn)
        if (loans.isEmpty()) {
            EmptyLoansView(
                onAddNewLoanClick = onAddNewLoanClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("loans_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = loans,
                    key = { it.loan.id }
                ) { item ->
                    val isExpanded = expandedStates[item.loan.id] ?: false

                    LoanExpandableCard(
                        loanWithInstallments = item,
                        isExpanded = isExpanded,
                        onExpandToggle = {
                            expandedStates[item.loan.id] = !isExpanded
                        },
                        onToggleInstallmentPaid = onToggleInstallmentPaid,
                        onDeleteLoanClick = { onDeleteLoanClick(item.loan) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

/**
 * Üst Özet Bilgi Kartı: Toplam Kalan Borç ve Bu Ayki Taksit Yükü
 */
@Composable
private fun LoansOverviewHeaderCard(
    totalRemainingDebt: Double,
    monthlyLoansTotal: Double,
    activeLoansCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol: Kalan Toplam Borç
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(LoanAccentRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Toplam Kalan Borç",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Formatters.formatCurrency(totalRemainingDebt),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LoanAccentRed
                )
            }

            // Sağ: Aylık Sabit Taksitler
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BrandPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Aylık Taksit Toplamı",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Formatters.formatCurrency(monthlyLoansTotal),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Akordeon / Açılır-Kapanır Kredi Kartı (Expandable Card)
 */
@Composable
private fun LoanExpandableCard(
    loanWithInstallments: LoanWithInstallments,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onToggleInstallmentPaid: (LoanInstallmentEntity) -> Unit,
    onDeleteLoanClick: () -> Unit
) {
    val loan = loanWithInstallments.loan
    val sortedInstallments = loanWithInstallments.sortedInstallments
    val paidCount = loanWithInstallments.actualPaidCount
    val totalCount = loan.totalInstallments
    val progress = loanWithInstallments.progressPercent
    val isCompleted = loanWithInstallments.isCompleted

    // Ok ikonu dönme animasyonu
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .animateContentSize()
            .testTag("loan_card_${loan.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // KAPALI KART GÖRÜNÜMÜ (ÖZET) - Karta tıklandığında açılır
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandToggle() }
                    .padding(18.dp)
            ) {
                // Üst Kısım: Banka Adı, İkon, Silme ve Aç/Kapa Oku
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isCompleted) LoanAccentGreen.copy(alpha = 0.15f)
                                    else BrandPrimary.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = if (isCompleted) LoanAccentGreen else BrandPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = loan.bankName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Son Ödeme: Her ayın ${loan.dueDayOfMonth}. günü",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDeleteLoanClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("delete_loan_${loan.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Krediyi Sil",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onExpandToggle,
                            modifier = Modifier
                                .size(36.dp)
                                .rotate(arrowRotation)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Kapat" else "Aç",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tutar Bilgileri: Kalan Borç & Aylık Taksit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kalan Borç",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatCurrency(loanWithInstallments.remainingDebt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) LoanAccentGreen else LoanAccentRed
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Aylık Taksit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatCurrency(loan.monthlyInstallment),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Görsel İlerleme Çubuğu (ProgressBar)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isCompleted) LoanAccentGreen else BrandPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCompleted) "Tüm taksitler ödendi! 🎉" else "$paidCount / $totalCount Taksit Ödendi",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) LoanAccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val percentInt = (progress * 100).toInt()
                    Text(
                        text = "%$percentInt",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) LoanAccentGreen else BrandPrimary
                    )
                }
            }

            // AÇIK KART GÖRÜNÜMÜ: Taksit Detay Listesi (Akordeon)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Taksit Ödeme Çizelgesi",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ödenenleri işaretleyin",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    sortedInstallments.forEach { installment ->
                        LoanInstallmentItemRow(
                            installment = installment,
                            onTogglePaid = { onToggleInstallmentPaid(installment) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Taksit Detay Satırı: Ay Adı, Checkbox, Üstü Çizili (Strikethrough) Efekt, Ödeme Zamanı Damgası
 */
@Composable
private fun LoanInstallmentItemRow(
    installment: LoanInstallmentEntity,
    onTogglePaid: () -> Unit
) {
    val monthName = Formatters.formatMonthYear(installment.year, installment.month)
    val isPaid = installment.isPaid

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPaid) MaterialTheme.colorScheme.surface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onTogglePaid() }
            .testTag("installment_item_${installment.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = isPaid,
                    onCheckedChange = { onTogglePaid() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = LoanAccentGreen,
                        uncheckedColor = MaterialTheme.colorScheme.outline,
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.testTag("installment_checkbox_${installment.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${installment.installmentNumber}. Taksit: $monthName",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isPaid) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (isPaid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }

                    // Ödeme Yapıldıysa Tarih Damgası, Yapılmadıysa Son Ödeme Günü
                    if (isPaid && installment.paidAtTimestamp != null) {
                        Text(
                            text = "✓ ${Formatters.formatDateTime(installment.paidAtTimestamp)} tarihinde ödendi",
                            style = MaterialTheme.typography.labelSmall,
                            color = LoanAccentGreen,
                            fontSize = 11.sp
                        )
                    } else {
                        Text(
                            text = "Son Ödeme: ${installment.dueDay} $monthName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Text(
                text = Formatters.formatCurrency(installment.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPaid) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isPaid) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}

/**
 * Boş Liste Görünümü (Henüz kredi eklenmemişse)
 */
@Composable
private fun EmptyLoansView(
    onAddNewLoanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(BrandPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = "Henüz Eklenmiş Kredi Yok",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Banka kredilerinizi, taksitli borçlarınızı ve ödeme planınızı takip etmek için yeni bir kredi ekleyin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddNewLoanClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier.testTag("empty_add_loan_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "İlk Kredini Ekle",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
