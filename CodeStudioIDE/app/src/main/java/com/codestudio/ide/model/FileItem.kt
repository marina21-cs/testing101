package com.codestudio.ide.model

import com.codestudio.ide.R
import java.io.File
import java.io.Serializable

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = System.currentTimeMillis(),
    val extension: String = "",
    var isExpanded: Boolean = false,
    var depth: Int = 0,
    var children: MutableList<FileItem> = mutableListOf()
) : Serializable {

    companion object {
        fun fromFile(file: File, depth: Int = 0): FileItem {
            return FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0,
                lastModified = file.lastModified(),
                extension = file.extension.lowercase(),
                depth = depth
            )
        }
    }

    fun getLanguage(): String {
        return when (extension) {
            // Web Development
            "html", "htm" -> "html"
            "css", "scss", "sass", "less" -> "css"
            "js", "mjs" -> "javascript"
            "ts", "tsx" -> "typescript"
            "jsx" -> "jsx"
            "vue" -> "vue"
            "svelte" -> "svelte"
            "json" -> "json"
            "xml", "svg" -> "xml"
            
            // Programming Languages
            "py", "pyw" -> "python"
            "r", "rmd" -> "r"
            "lua" -> "lua"
            "rs" -> "rust"
            "kt", "kts" -> "kotlin"
            "java" -> "java"
            "c", "h" -> "c"
            "cpp", "cc", "cxx", "hpp" -> "cpp"
            "cs" -> "csharp"
            "go" -> "go"
            "rb" -> "ruby"
            "php" -> "php"
            "swift" -> "swift"
            "dart" -> "dart"
            
            // Shell & Config
            "sh", "bash", "zsh" -> "shell"
            "ps1" -> "powershell"
            "bat", "cmd" -> "batch"
            "yaml", "yml" -> "yaml"
            "toml" -> "toml"
            "ini", "cfg", "conf" -> "ini"
            "env" -> "dotenv"
            
            // Data & Markup
            "md", "markdown" -> "markdown"
            "sql" -> "sql"
            "graphql", "gql" -> "graphql"
            
            // Other
            "dockerfile" -> "dockerfile"
            "gradle" -> "groovy"
            "tex", "latex" -> "latex"
            
            else -> "plaintext"
        }
    }

    fun getIcon(): Int {
        return when {
            isDirectory -> R.drawable.ic_folder
            else -> when (extension) {
                "html", "htm" -> R.drawable.ic_file_html
                "css", "scss", "sass" -> R.drawable.ic_file_css
                "js", "mjs", "jsx" -> R.drawable.ic_file_js
                "ts", "tsx" -> R.drawable.ic_file_ts
                "py", "pyw" -> R.drawable.ic_file_python
                "r", "rmd" -> R.drawable.ic_file_r
                "lua" -> R.drawable.ic_file_lua
                "rs" -> R.drawable.ic_file_rust
                "kt", "kts" -> R.drawable.ic_file_kotlin
                "java" -> R.drawable.ic_file_java
                "json" -> R.drawable.ic_file_json
                "xml", "svg" -> R.drawable.ic_file_xml
                "md", "markdown" -> R.drawable.ic_file_markdown
                "yaml", "yml" -> R.drawable.ic_file_yaml
                "sh", "bash" -> R.drawable.ic_file_shell
                "sql" -> R.drawable.ic_file_sql
                "c", "h" -> R.drawable.ic_file_c
                "cpp", "cc", "hpp" -> R.drawable.ic_file_cpp
                "go" -> R.drawable.ic_file_go
                "php" -> R.drawable.ic_file_php
                "rb" -> R.drawable.ic_file_ruby
                "swift" -> R.drawable.ic_file_swift
                "dart" -> R.drawable.ic_file_dart
                else -> R.drawable.ic_file_text
            }
        }
    }
}
