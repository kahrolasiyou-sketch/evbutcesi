package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.LoanEntity
import com.example.ui.theme.ExpenseRed

@Composable
fun DeleteLoanConfirmDialog(
    isVisible: Boolean,
    loan: LoanEntity?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible || loan == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = ExpenseRed
            )
        },
        title = {
            Text(
                text = "Krediyi Sil",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "\"${loan.bankName}\" kredisi ve tüm taksit kayıtları kalıcı olarak silinecektir. Bu işlemi onaylıyor musunuz?"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExpenseRed,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("confirm_delete_loan_button")
            ) {
                Text("Sil", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_loan_button")
            ) {
                Text("İptal")
            }
        }
    )
}
