package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MainSavings
import com.example.data.SavingsTarget
import com.example.data.TabungKuRepository
import com.example.data.UserProfile
import com.example.data.TransactionHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.BuildConfig
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.Part
import com.example.network.RetrofitClient
import com.example.network.ThinkingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TabungKuViewModel(private val repository: TabungKuRepository) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val mainSavings: StateFlow<MainSavings?> = repository.mainSavings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        
    val allTargets: StateFlow<List<SavingsTarget>> = repository.allTargets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionHistory>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize if empty
        viewModelScope.launch {
            repository.saveUserProfile(UserProfile(name = "Pengguna"))
            repository.saveMainSavings(0.0)
        }
    }

    fun updateProfile(name: String, photoUri: String?) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.saveUserProfile(current.copy(name = name, photoUri = photoUri))
        }
    }

    fun updateAiSettings(provider: String, apiKey: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.saveUserProfile(current.copy(aiProvider = provider, aiApiKey = apiKey, aiModel = ""))
        }
    }

    suspend fun verifyAiKey(provider: String, apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Pair(false, "API Key tidak boleh kosong")
        }
        try {
            if (provider == "Groq") {
                val request = com.example.network.GroqRequest(
                    model = "llama-3.1-8b-instant",
                    messages = listOf(com.example.network.GroqMessage(role = "user", content = "Test"))
                )
                com.example.network.GroqRetrofitClient.service.generateContent("Bearer $apiKey", request)
            } else {
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Test"))))
                )
                RetrofitClient.service.generateContent(apiKey, request)
            }
            Pair(true, "🟢 Siap Digunakan")
        } catch (e: Exception) {
            Pair(false, "API Key tidak valid atau error jaringan")
        }
    }

    fun depositMain(amount: Double) {
        if (amount > 0) {
            viewModelScope.launch {
                val current = mainSavings.value?.balance ?: 0.0
                repository.saveMainSavings(current + amount)
                repository.addTransaction(amount, "DEPOSIT")
            }
        }
    }

    fun withdrawMain(amount: Double) {
        if (amount > 0) {
            viewModelScope.launch {
                val current = mainSavings.value?.balance ?: 0.0
                if (current >= amount) {
                    repository.saveMainSavings(current - amount)
                    repository.addTransaction(amount, "WITHDRAW")
                }
            }
        }
    }

    fun addTarget(name: String, amount: Double) {
        if (name.isNotBlank() && amount > 0) {
            viewModelScope.launch {
                repository.insertTarget(name, amount)
            }
        }
    }

    fun depositToTarget(target: SavingsTarget, amount: Double) {
        if (amount > 0) {
            viewModelScope.launch {
                val updatedTarget = target.copy(currentAmount = target.currentAmount + amount)
                repository.updateTarget(updatedTarget)
                repository.addTransaction(amount, "DEPOSIT", target.id)
            }
        }
    }

    suspend fun generateFinancialSummary(): String = withContext(Dispatchers.IO) {
        try {
            val balance = mainSavings.value?.balance ?: 0.0
            val targets = allTargets.value.joinToString(", ") { "${it.name}: ${it.currentAmount}/${it.targetAmount}" }
            
            val prompt = """
                Bertindak sebagai penasihat keuangan pintar untuk aplikasi TabungKu.
                Data pengguna saat ini:
                - Saldo Utama: Rp $balance
                - Target Tabungan: $targets
                
                Berikan 2-3 kalimat ringkasan motivasi dan saran singkat untuk pengelolaan uang yang lebih baik. Gunakan bahasa Indonesia yang santai, modern, dan profesional.
            """.trimIndent()
            
            val profile = userProfile.value ?: return@withContext "Data profil tidak ditemukan."
            val apiKey = profile.aiApiKey.trim()
            val provider = profile.aiProvider
            val customModel = profile.aiModel.trim()
            
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "API Key belum diatur. Silakan atur di Pengaturan."
            }
            
            if (provider == "Groq") {
                val modelToUse = if (customModel.isNotEmpty()) customModel else "llama-3.1-8b-instant"
                val request = com.example.network.GroqRequest(
                    model = modelToUse,
                    messages = listOf(
                        com.example.network.GroqMessage(role = "user", content = prompt)
                    )
                )
                val response = com.example.network.GroqRetrofitClient.service.generateContent("Bearer $apiKey", request)
                response.choices.firstOrNull()?.message?.content ?: "Tidak ada saran saat ini."
            } else {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    ),
                    generationConfig = GenerationConfig(
                        thinkingConfig = ThinkingConfig("HIGH")
                    )
                )
                val response = RetrofitClient.service.generateContent(apiKey, request)
                response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Tidak ada saran saat ini."
            }
        } catch (e: Exception) {
            "Gagal menghubungi server AI: ${e.message}"
        }
    }
}

class TabungKuViewModelFactory(private val repository: TabungKuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TabungKuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TabungKuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
