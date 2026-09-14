package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.UserProfileHelper
import com.example.ui.viewmodel.BudgetUiState
import com.example.ui.viewmodel.BudgetViewModel

@Composable
fun CreateProfileDialog(
    viewModel: BudgetViewModel,
    state: BudgetUiState
) {
    if (!state.isCreateProfileDialogVisible) return

    AlertDialog(
        onDismissRequest = { viewModel.closeCreateProfileDialog() },
        title = {
            Text(
                text = if (state.activeUser?.isGuest == true) "Misafir Hesabını Kaydet" else "Yeni Profil Oluştur",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Profil Adı
                OutlinedTextField(
                    value = state.createProfileName,
                    onValueChange = { viewModel.updateCreateProfileName(it) },
                    label = { Text("Kullanıcı / Profil Adı") },
                    placeholder = { Text("Örn: Ahmet, Aile Bütçesi") },
                    singleLine = true,
                    isError = state.createProfileError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_profile_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (state.createProfileError != null) {
                    Text(
                        text = state.createProfileError,
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 4 Haneli Sayısal PIN
                OutlinedTextField(
                    value = state.createProfilePin,
                    onValueChange = { viewModel.updateCreateProfilePin(it) },
                    label = { Text("4 Haneli Güvenlik PIN'i (İsteğe bağlı)") },
                    placeholder = { Text("Örn: 1234") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = {
                        Text("Boş bırakılırsa profil korumasız olur. Doldurulursa tam 4 rakam olmalıdır.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_profile_pin_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Avatar İkonu Seçimi
                Text(
                    text = "Profil İkonu",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserProfileHelper.availableAvatars.forEach { (avatarKey, label) ->
                        val isSelected = state.createProfileAvatar == avatarKey
                        val icon = UserProfileHelper.getAvatarIcon(avatarKey)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(state.createProfileColor).copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(state.createProfileColor) else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.updateCreateProfileAvatar(avatarKey) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color(state.createProfileColor) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Profil Rengi Seçimi
                Text(
                    text = "Profil Rengi",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserProfileHelper.availableColors.forEach { colorLong ->
                        val isSelected = state.createProfileColor == colorLong
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorLong))
                                .clickable { viewModel.updateCreateProfileColor(colorLong) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçildi",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Misafir Verilerini Aktarma Seçeneği
                if (state.activeUser?.isGuest == true) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.updateCreateProfileMigrateGuestData(!state.createProfileMigrateGuestData)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.createProfileMigrateGuestData,
                            onCheckedChange = { viewModel.updateCreateProfileMigrateGuestData(it) },
                            colors = CheckboxDefaults.colors(checkedColor = IncomeGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Misafir modundaki mevcut verilerimi (gelir, gider, krediler) bu yeni profile aktar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.confirmCreateProfile() },
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_create_profile_btn")
            ) {
                Text("Profili Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeCreateProfileDialog() }) {
                Text("İptal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SwitchProfileDialog(
    viewModel: BudgetViewModel,
    state: BudgetUiState
) {
    if (!state.isSwitchProfileDialogVisible) return

    val target = state.targetProfileToSwitch

    AlertDialog(
        onDismissRequest = { viewModel.closeSwitchProfileDialog() },
        title = {
            Text(
                text = if (target != null && target.isProtected) "${target.name} Girişi" else "Profil Seçin / Değiştirin",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (target == null) {
                    Text(
                        text = "Geçiş yapmak istediğiniz profili seçin:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    state.users.forEach { user ->
                        val isCurrent = user.id == state.activeUser?.id
                        val avatarIcon = UserProfileHelper.getAvatarIcon(user.avatarName)
                        val profileColor = Color(user.colorHex)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!isCurrent) {
                                        viewModel.selectTargetProfileToSwitch(user)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = if (isCurrent) BorderStroke(1.5.dp, IncomeGreen) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(profileColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = avatarIcon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = user.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when {
                                                user.isGuest -> "Misafir Modu"
                                                user.isProtected -> "4 Haneli PIN Korumalı"
                                                else -> "PIN Bulunmuyor"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (user.isProtected) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Text(
                                        text = "Aktif",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = IncomeGreen
                                    )
                                } else if (user.isProtected) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Korumalı",
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Hedef profil için 4 haneli PIN girişi
                    Text(
                        text = "'${target.name}' profili 4 haneli PIN ile korunmaktadır. Devam etmek için lütfen PIN girin:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = state.switchProfilePinInput,
                        onValueChange = { viewModel.updateSwitchProfilePin(it) },
                        label = { Text("4 Haneli PIN") },
                        placeholder = { Text("••••") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        isError = state.switchProfileError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("switch_profile_pin_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (state.switchProfileError != null) {
                        Text(
                            text = state.switchProfileError,
                            color = ExpenseRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (target != null) {
                Button(
                    onClick = { viewModel.confirmSwitchProfile() },
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_switch_profile_btn")
                ) {
                    Text("Giriş Yap")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeSwitchProfileDialog() }) {
                Text(if (target != null) "Geri" else "Kapat")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ChangePasswordDialog(
    viewModel: BudgetViewModel,
    state: BudgetUiState
) {
    if (!state.isChangePasswordDialogVisible) return

    val active = state.activeUser ?: return

    AlertDialog(
        onDismissRequest = { viewModel.closeChangePasswordDialog() },
        title = {
            Text(
                text = "4 Haneli PIN Belirle / Değiştir",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (active.isProtected) {
                    OutlinedTextField(
                        value = state.currentPinInput,
                        onValueChange = { viewModel.updateCurrentPinInput(it) },
                        label = { Text("Mevcut 4 Haneli PIN") },
                        placeholder = { Text("••••") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = state.newPinInput,
                    onValueChange = { viewModel.updateNewPinInput(it) },
                    label = { Text("Yeni 4 Haneli PIN") },
                    placeholder = { Text("Örn: 5678") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = { Text("PIN'i kaldırmak istiyorsanız boş bırakabilirsiniz.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (state.newPinInput.isNotEmpty()) {
                    OutlinedTextField(
                        value = state.confirmNewPinInput,
                        onValueChange = { viewModel.updateConfirmNewPinInput(it) },
                        label = { Text("Yeni PIN'i Onaylayın") },
                        placeholder = { Text("••••") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (state.changePasswordError != null) {
                    Text(
                        text = state.changePasswordError,
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.confirmChangePassword() },
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeChangePasswordDialog() }) {
                Text("İptal")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteProfileDialog(
    viewModel: BudgetViewModel,
    state: BudgetUiState
) {
    if (!state.isDeleteProfileDialogVisible) return

    val target = state.profileToDelete ?: return

    AlertDialog(
        onDismissRequest = { viewModel.closeDeleteProfileDialog() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ExpenseRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Profili ve Verilerini Sil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "'${target.name}' profilini silmek üzeresiniz.",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "DİKKAT: Bu işlem geri alınamaz! Bu profile ait tüm gelir, gider, krediler ve taksit verileri veritabanından kalıcı olarak silinecektir.",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (target.isProtected) {
                    OutlinedTextField(
                        value = state.deleteProfilePinInput,
                        onValueChange = { viewModel.updateDeleteProfilePinInput(it) },
                        label = { Text("Silme Onayı İçin 4 Haneli PIN") },
                        placeholder = { Text("••••") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = state.deleteProfileError != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_profile_pin_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (state.deleteProfileError != null) {
                        Text(
                            text = state.deleteProfileError,
                            color = ExpenseRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.confirmDeleteProfile() },
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_delete_profile_btn")
            ) {
                Text("Profili ve Tüm Verileri Sil")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeDeleteProfileDialog() }) {
                Text("Vazgeç")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Karşılama / Giriş Ekranındaki "Diğer Profilleri Görüntüle / Profil Değiştir" Diyaloğu
 */
@Composable
fun ProfileListDialog(
    viewModel: BudgetViewModel,
    state: BudgetUiState
) {
    if (!state.isProfileListDialogVisible) return

    AlertDialog(
        onDismissRequest = { viewModel.closeProfileListDialog() },
        title = {
            Text(
                text = "Kayıtlı Profiller",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Giriş yapmak istediğiniz profili seçin:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                state.users.forEach { user ->
                    val isSelected = user.id == state.loginSelectedUser?.id
                    val avatarIcon = UserProfileHelper.getAvatarIcon(user.avatarName)
                    val profileColor = Color(user.colorHex)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.selectLoginUser(user)
                            }
                            .testTag("profile_list_item_${user.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (isSelected) BorderStroke(1.5.dp, IncomeGreen) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(profileColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = avatarIcon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when {
                                            user.isGuest -> "Misafir Modu"
                                            user.isProtected -> "4 Haneli PIN Korumalı"
                                            else -> "PIN Bulunmuyor"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (user.isProtected) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçili",
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (user.isProtected) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Korumalı",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.closeProfileListDialog()
                    viewModel.openCreateProfileDialog()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Yeni Profil")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeProfileListDialog() }) {
                Text("Kapat")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ExportDataDialog(
    viewModel: BudgetViewModel,
    state: BudgetUiState
) {
    if (!state.isExportDialogVisible) return

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { viewModel.closeExportDialog() },
        title = {
            Text(
                text = "Finansal Verileri Dışa Aktar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Aktif profilinizin (${state.activeUser?.name ?: "Bilinmiyor"}) tüm gelir, gider ve kredi geçmişini yedekleyin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.exportFormat == "JSON",
                        onClick = { viewModel.setExportFormat("JSON") },
                        label = { Text("JSON Formatı") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                            selectedLabelColor = IncomeGreen
                        )
                    )
                    FilterChip(
                        selected = state.exportFormat == "CSV",
                        onClick = { viewModel.setExportFormat("CSV") },
                        label = { Text("CSV (Excel) Formatı") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                            selectedLabelColor = IncomeGreen
                        )
                    )
                }

                // Önizleme kutusu
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = state.exportDataText.take(1500) + if (state.exportDataText.length > 1500) "\n..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, state.exportDataText)
                        type = if (state.exportFormat == "CSV") "text/csv" else "application/json"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Finansal Verileri Paylaş / Kaydet")
                    context.startActivity(shareIntent)
                    viewModel.closeExportDialog()
                },
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Paylaş / Dışa Aktar")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeExportDialog() }) {
                Text("Kapat")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
