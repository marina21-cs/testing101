package com.codestudio.ide.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.codestudio.ide.CodeStudioApp
import com.codestudio.ide.R
import com.codestudio.ide.databinding.ActivitySettingsBinding
import com.codestudio.ide.model.EditorSettings

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val preferencesManager by lazy { CodeStudioApp.instance.preferencesManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val settings = preferencesManager.editorSettings

        // Editor settings
        binding.fontSizeSlider.value = settings.fontSize.toFloat()
        binding.fontSizeValue.text = "${settings.fontSize}sp"
        binding.tabSizeSlider.value = settings.tabSize.toFloat()
        binding.tabSizeValue.text = "${settings.tabSize}"
        binding.useSpacesSwitch.isChecked = settings.useSpaces
        binding.lineNumbersSwitch.isChecked = settings.showLineNumbers
        binding.wordWrapSwitch.isChecked = settings.wordWrap
        binding.highlightLineSwitch.isChecked = settings.highlightCurrentLine
        binding.autoCloseBracketsSwitch.isChecked = settings.autoCloseBrackets
        binding.autoCloseQuotesSwitch.isChecked = settings.autoCloseQuotes
        binding.autoIndentSwitch.isChecked = settings.autoIndent

        // App settings
        binding.darkThemeSwitch.isChecked = preferencesManager.isDarkTheme
        binding.showHiddenFilesSwitch.isChecked = preferencesManager.showHiddenFiles
        binding.autoSaveSwitch.isChecked = preferencesManager.autoSaveEnabled
        binding.autoSaveIntervalSlider.value = preferencesManager.autoSaveInterval.toFloat()
        binding.autoSaveIntervalValue.text = "${preferencesManager.autoSaveInterval}s"
        binding.webServerPortInput.setText(preferencesManager.webServerPort.toString())

        // Terminal settings
        binding.terminalFontSizeSlider.value = preferencesManager.terminalFontSize.toFloat()
        binding.terminalFontSizeValue.text = "${preferencesManager.terminalFontSize}sp"
    }

    private fun setupListeners() {
        // Font size
        binding.fontSizeSlider.addOnChangeListener { _, value, _ ->
            binding.fontSizeValue.text = "${value.toInt()}sp"
        }

        // Tab size
        binding.tabSizeSlider.addOnChangeListener { _, value, _ ->
            binding.tabSizeValue.text = "${value.toInt()}"
        }

        // Auto save interval
        binding.autoSaveIntervalSlider.addOnChangeListener { _, value, _ ->
            binding.autoSaveIntervalValue.text = "${value.toInt()}s"
        }

        // Terminal font size
        binding.terminalFontSizeSlider.addOnChangeListener { _, value, _ ->
            binding.terminalFontSizeValue.text = "${value.toInt()}sp"
        }

        // Dark theme
        binding.darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.isDarkTheme = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Save button
        binding.saveButton.setOnClickListener {
            saveSettings()
        }

        // Reset button
        binding.resetButton.setOnClickListener {
            resetToDefaults()
        }
    }

    private fun saveSettings() {
        val settings = EditorSettings(
            fontSize = binding.fontSizeSlider.value.toInt(),
            tabSize = binding.tabSizeSlider.value.toInt(),
            useSpaces = binding.useSpacesSwitch.isChecked,
            showLineNumbers = binding.lineNumbersSwitch.isChecked,
            wordWrap = binding.wordWrapSwitch.isChecked,
            highlightCurrentLine = binding.highlightLineSwitch.isChecked,
            autoCloseBrackets = binding.autoCloseBracketsSwitch.isChecked,
            autoCloseQuotes = binding.autoCloseQuotesSwitch.isChecked,
            autoIndent = binding.autoIndentSwitch.isChecked,
            theme = if (preferencesManager.isDarkTheme) "dark" else "light"
        )

        preferencesManager.editorSettings = settings
        preferencesManager.showHiddenFiles = binding.showHiddenFilesSwitch.isChecked
        preferencesManager.autoSaveEnabled = binding.autoSaveSwitch.isChecked
        preferencesManager.autoSaveInterval = binding.autoSaveIntervalSlider.value.toInt()
        preferencesManager.terminalFontSize = binding.terminalFontSizeSlider.value.toInt()

        try {
            preferencesManager.webServerPort = binding.webServerPortInput.text.toString().toInt()
        } catch (e: NumberFormatException) {
            preferencesManager.webServerPort = 8080
        }

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetToDefaults() {
        val defaultSettings = EditorSettings()
        preferencesManager.editorSettings = defaultSettings
        preferencesManager.isDarkTheme = true
        preferencesManager.showHiddenFiles = false
        preferencesManager.autoSaveEnabled = false
        preferencesManager.autoSaveInterval = 30
        preferencesManager.terminalFontSize = 12
        preferencesManager.webServerPort = 8080

        loadSettings()
        Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
