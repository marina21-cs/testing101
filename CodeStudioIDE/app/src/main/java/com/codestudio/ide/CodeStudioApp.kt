package com.codestudio.ide

import android.app.Application
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
        
        // Initialize database
        database = AppDatabase.getInstance(this)
        
        // Initialize preferences
        preferencesManager = PreferencesManager(this)
        
        // Initialize file manager
        fileManager = FileManager(this)
        
        // Create default workspace
        fileManager.createDefaultWorkspace()
    }

    companion object {
        lateinit var instance: CodeStudioApp
            private set
    }
}
