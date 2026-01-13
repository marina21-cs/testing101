package com.codestudio.ide.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.codestudio.ide.R

class CreateFileDialog(
    context: Context,
    private val parentPath: String,
    private val isDirectory: Boolean,
    private val onConfirm: (name: String, path: String) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_create_file)

        val nameInput = findViewById<EditText>(R.id.fileNameInput)
        val templateSpinner = findViewById<Spinner>(R.id.templateSpinner)
        val createButton = findViewById<Button>(R.id.createButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        // Set title
        setTitle(if (isDirectory) "New Folder" else "New File")

        // Setup templates
        if (!isDirectory) {
            val templates = listOf(
                "Empty File",
                "HTML File (.html)",
                "CSS File (.css)",
                "JavaScript (.js)",
                "TypeScript (.ts)",
                "Python (.py)",
                "Rust (.rs)",
                "Lua (.lua)",
                "R Script (.R)",
                "Markdown (.md)",
                "JSON (.json)",
                "YAML (.yaml)"
            )
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, templates)
            templateSpinner.adapter = adapter

            templateSpinner.setSelection(0)
        } else {
            templateSpinner.visibility = android.view.View.GONE
        }

        createButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isBlank()) {
                nameInput.error = "Name is required"
                return@setOnClickListener
            }

            // Add extension based on template
            val finalName = if (!isDirectory && !name.contains('.')) {
                when (templateSpinner.selectedItemPosition) {
                    1 -> "$name.html"
                    2 -> "$name.css"
                    3 -> "$name.js"
                    4 -> "$name.ts"
                    5 -> "$name.py"
                    6 -> "$name.rs"
                    7 -> "$name.lua"
                    8 -> "$name.R"
                    9 -> "$name.md"
                    10 -> "$name.json"
                    11 -> "$name.yaml"
                    else -> name
                }
            } else {
                name
            }

            onConfirm(finalName, parentPath)
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }
}
