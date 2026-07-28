package com.example.data

import kotlinx.coroutines.flow.Flow

class TabungKuRepository(private val dao: TabungKuDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val mainSavings: Flow<MainSavings?> = dao.getMainSavings()
    val allTargets: Flow<List<SavingsTarget>> = dao.getAllTargets()
    val allTransactions: Flow<List<TransactionHistory>> = dao.getAllTransactions()

    suspend fun saveUserProfile(profile: UserProfile) {
        dao.saveUserProfile(profile)
    }

    suspend fun addMainSavings(amount: Double) {
        val currentSavings = dao.getMainSavings()
        // We handle this in viewmodel or we can just fetch and update
    }
    
    suspend fun saveMainSavings(balance: Double) {
        dao.saveMainSavings(MainSavings(balance = balance))
    }

    suspend fun insertTarget(name: String, amount: Double) {
        dao.insertTarget(SavingsTarget(name = name, targetAmount = amount))
    }
    
    suspend fun updateTarget(target: SavingsTarget) {
        dao.updateTarget(target)
    }

    suspend fun deleteTarget(id: Int) {
        dao.deleteTarget(id)
    }

    suspend fun addTransaction(amount: Double, type: String, targetId: Int? = null) {
        dao.insertTransaction(TransactionHistory(amount = amount, type = type, targetId = targetId))
    }
}
