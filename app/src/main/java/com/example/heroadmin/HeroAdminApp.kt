package com.example.heroadmin

import android.app.Application
import android.content.Context
import com.example.heroadmin.LocalDatabaseSingleton

class HeroAdminApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val preferences = getSharedPreferences("LocalDatabasePrefs", Context.MODE_PRIVATE)
        LocalDatabaseSingleton.initialize(preferences)
    }
}
