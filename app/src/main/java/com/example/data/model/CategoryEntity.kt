package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["profileId"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 0L, // 0L: Sistem varsayılan kategorisi, >0L: Kullanıcıya özel kategori
    val name: String,
    val type: String, // "EXPENSE", "INCOME", "BOTH"
    val iconName: String, // "receipt", "debt", "cart", "other", "salary", "bonus", "home", "car", "health"
    val colorHex: Long,
    val isDefault: Boolean = false
)

