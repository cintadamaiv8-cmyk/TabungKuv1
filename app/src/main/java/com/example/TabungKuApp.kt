package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.TabungKuDatabase
import com.example.data.TabungKuRepository

class TabungKuApp : Application() {
    lateinit var database: TabungKuDatabase
        private set
    
    lateinit var repository: TabungKuRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            TabungKuDatabase::class.java,
            "tabungku_db"
        )
        .fallbackToDestructiveMigration()
        .build()
        repository = TabungKuRepository(database.dao())
    }
}
