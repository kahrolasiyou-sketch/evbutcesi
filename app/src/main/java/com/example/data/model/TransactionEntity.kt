package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["profileId"]),
        Index(value = ["categoryId"]),
        Index(value = ["dateTimestamp"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1L,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE", "INCOME"
    val categoryId: Long,
    val dateTimestamp: Long,
    val notes: String = ""
)

