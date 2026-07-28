package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TabungKuDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // Main Savings
    @Query("SELECT * FROM main_savings WHERE id = 1")
    fun getMainSavings(): Flow<MainSavings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMainSavings(savings: MainSavings)

    // Savings Target
    @Query("SELECT * FROM savings_target")
    fun getAllTargets(): Flow<List<SavingsTarget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: SavingsTarget)

    @Update
    suspend fun updateTarget(target: SavingsTarget)

    @Query("DELETE FROM savings_target WHERE id = :id")
    suspend fun deleteTarget(id: Int)

    // Transaction History
    @Query("SELECT * FROM transaction_history ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionHistory)
}
