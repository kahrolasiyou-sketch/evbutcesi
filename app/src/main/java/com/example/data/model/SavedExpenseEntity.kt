package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_expenses",
    indices = [
        Index(value = ["profileId"])
    ]
)
data class SavedExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1L,
    val title: String,
    val categoryId: Long? = null,
    val defaultAmount: Double? = null,
    val type: String = "EXPENSE",
    val createdAt: Long = System.currentTimeMillis()
)

