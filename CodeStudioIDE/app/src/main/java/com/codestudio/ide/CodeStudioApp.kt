package com.codestudio.ide

import android.app.Application
import android.util.Log
import com.codestudio.ide.data.AppDatabase
import com.codestudio.ide.data.PreferencesManager
import com.codestudio.ide.utils.FileManager

class CodeStudioApp : Application() {

    lateinit var database: AppDatabase
        private set
    
    lateinit var preferencesManager: PreferencesManager
        private set
    
    lateinit var fileManager: FileManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        try {
            // Initialize database
            database = AppDatabase.getInstance(this)
            
            // Initialize preferences
            preferencesManager = PreferencesManager(this)
            
            // Initialize file manager
            fileManager = FileManager(this)
            
            // Create default workspace
            fileManager.createDefaultWorkspace()
        } catch (e: Exception) {
            Log.e("CodeStudioApp", "Error during initialization", e)
        }
    }

    companion object {
        lateinit var instance: CodeStudioApp
            private set
    }
}
