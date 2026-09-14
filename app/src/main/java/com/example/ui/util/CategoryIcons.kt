package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

data class IconOption(
    val key: String,
    val name: String,
    val icon: ImageVector
)

object CategoryIcons {
    val options = listOf(
        IconOption("receipt", "Fatura", Icons.Default.Receipt),
        IconOption("debt", "Borç / Kart", Icons.Default.CreditCard),
        IconOption("cart", "Market", Icons.Default.ShoppingCart),
        IconOption("salary", "Maaş", Icons.Default.Payments),
        IconOption("bonus", "Ek Gelir", Icons.Default.TrendingUp),
        IconOption("savings", "Birikim", Icons.Default.Savings),
        IconOption("bank", "Banka", Icons.Default.AccountBalance),
        IconOption("wallet", "Cüzdan", Icons.Default.AccountBalanceWallet),
        IconOption("home", "Kira / Ev", Icons.Default.Home),
        IconOption("electric", "Elektrik / Su", Icons.Default.Bolt),
        IconOption("car", "Ulaşım / Araç", Icons.Default.DirectionsCar),
        IconOption("food", "Yemek / Kafe", Icons.Default.Restaurant),
        IconOption("health", "Sağlık", Icons.Default.Healing),
        IconOption("school", "Eğitim", Icons.Default.School),
        IconOption("gift", "Hediye", Icons.Default.CardGiftcard),
        IconOption("other", "Diğer", Icons.Default.Category)
    )

    fun getIcon(key: String): ImageVector {
        return options.find { it.key == key }?.icon ?: Icons.Default.MoreHoriz
    }
}

val PRESET_COLORS = listOf(
    0xFFEF4444, // Kırmızı
    0xFFF97316, // Turuncu
    0xFFF59E0B, // Kehribar
    0xFF10B981, // Zümrüt Yeşil
    0xFF06B6D4, // Camgöbeği
    0xFF3B82F6, // Mavi
    0xFF6366F1, // İndigo
    0xFF8B5CF6, // Menekşe
    0xFFEC4899, // Pembe
    0xFF64748B  // Arduvaz Gri
)
