package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.UserProfileHelper
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.BudgetUiState
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun BudgetTopBar(
    viewModel: BudgetViewModel,
    state: BudgetUiState,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    val screenTitle = when (state.currentDestination) {
        AppDestination.DASHBOARD -> "Ev Bütçesi"
        AppDestination.HISTORY -> "İşlem Kayıtları"
        AppDestination.LOANS -> "Krediler & Borçlar"
        AppDestination.SETTINGS -> "Ayarlar"
    }

    val active = state.activeUser
    val avatarIcon = if (active != null) UserProfileHelper.getAvatarIcon(active.avatarName) else Icons.Default.Person
    val avatarColor = if (active != null) Color(active.colorHex) else IncomeGreen

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("budget_top_bar"),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol Taraf: Sayfa Başlığı
            Column {
                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Sağ Taraf: Aktif Profil Rozeti (Avatar + İsim + Menü)
            Box {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { isMenuExpanded = true }
                        .testTag("top_bar_profile_chip"),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(avatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = avatarIcon,
                                contentDescription = "Profil Avatarı",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = active?.name ?: "Misafir",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (active?.isGuest == true) {
                                Text(
                                    text = "Misafir Modu",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (active?.isProtected == true) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Korumalı",
                                tint = IncomeGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Profil Menüsü
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Profil Değiştir") },
                        leadingIcon = {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = IncomeGreen)
                        },
                        onClick = {
                            isMenuExpanded = false
                            viewModel.openSwitchProfileDialog()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Yeni Profil Oluştur") },
                        leadingIcon = {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        onClick = {
                            isMenuExpanded = false
                            viewModel.openCreateProfileDialog()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Oturumu Kapat", color = ExpenseRed) },
                        leadingIcon = {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = ExpenseRed)
                        },
                        onClick = {
                            isMenuExpanded = false
                            viewModel.logoutToWelcome()
                        }
                    )
                }
            }
        }
    }
}
