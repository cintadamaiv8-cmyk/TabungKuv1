package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserProfile::class, MainSavings::class, SavingsTarget::class, TransactionHistory::class],
    version = 2,
    exportSchema = false
)
abstract class TabungKuDatabase : RoomDatabase() {
    abstract fun dao(): TabungKuDao
}
