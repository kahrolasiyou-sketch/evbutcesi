package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val password: String = "",
    val pin: String = "",
    val avatarName: String = "person",
    val colorHex: Long = 0xFF28C76F,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false,
    val isGuest: Boolean = false,
    val isBiometricEnabled: Boolean = false
) {
    val isProtected: Boolean
        get() = password.isNotBlank() || pin.isNotBlank()

    val hasPassword: Boolean
        get() = password.isNotBlank() || pin.isNotBlank()

    val displayName: String
        get() = if (isGuest) "Misafir" else name
}

