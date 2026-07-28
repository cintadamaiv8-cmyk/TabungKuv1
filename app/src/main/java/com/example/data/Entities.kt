package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Pengguna",
    val photoUri: String? = null,
    val aiProvider: String = "Gemini",
    val aiApiKey: String = "",
    val aiModel: String = ""
)

@Entity(tableName = "main_savings")
data class MainSavings(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 0.0
)

@Entity(tableName = "savings_target")
data class SavingsTarget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0
)

@Entity(tableName = "transaction_history")
data class TransactionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "DEPOSIT", "WITHDRAW"
    val timestamp: Long = System.currentTimeMillis(),
    val targetId: Int? = null // null means main savings
)
