package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object UserProfileHelper {

    val availableAvatars = listOf(
        "person" to "Kullanıcı",
        "face" to "Güleryüz",
        "savings" to "Kumbara",
        "account_balance" to "Banka",
        "wallet" to "Cüzdan",
        "home" to "Ev",
        "work" to "İş",
        "star" to "Yıldız"
    )

    val availableColors = listOf(
        0xFF28C76F, // Yeşil (#28C76F)
        0xFF007BFF, // Mavi
        0xFF7367F0, // Mor
        0xFFFF9F43, // Turuncu
        0xFFEA5455, // Kırmızı (#FF4D4D tonu)
        0xFF00CFE8, // Turkuaz
        0xFFE83E8C, // Pembe
        0xFF6C757D  // Nötr Gri
    )

    fun getAvatarIcon(avatarName: String): ImageVector {
        return when (avatarName) {
            "face" -> Icons.Default.Face
            "savings" -> Icons.Default.Savings
            "account_balance" -> Icons.Default.AccountBalance
            "wallet" -> Icons.Default.Wallet
            "home" -> Icons.Default.Home
            "work" -> Icons.Default.Work
            "star" -> Icons.Default.Star
            else -> Icons.Default.Person
        }
    }
}
