package com.codestudio.ide.utils

import com.codestudio.ide.model.SearchOptions
import com.codestudio.ide.model.SearchResult
import com.codestudio.ide.model.FileItem
import java.io.File
import java.util.regex.Pattern

class SearchManager {

    fun searchInFiles(
        directory: String,
        options: SearchOptions
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val root = File(directory)
        
        if (!root.exists() || !root.isDirectory) {
            return results
        }

        val pattern = createSearchPattern(options)
        val includePatterns = parseGlobPatterns(options.includePattern)
        val excludePatterns = parseGlobPatterns(options.excludePattern)

        searchRecursive(root, pattern, includePatterns, excludePatterns, results)
        
        return results
    }

    private fun searchRecursive(
        directory: File,
        pattern: Pattern,
        includePatterns: List<Regex>,
        excludePatterns: List<Regex>,
        results: MutableList<SearchResult>
    ) {
        directory.listFiles()?.forEach { file ->
            // Skip hidden files and common non-text directories
            if (file.name.startsWith(".") || 
                file.name in listOf("node_modules", "build", "dist", ".git", "__pycache__")) {
                return@forEach
            }

            if (file.isDirectory) {
                searchRecursive(file, pattern, includePatterns, excludePatterns, results)
            } else {
                // Check if file matches include/exclude patterns
                val fileName = file.name
                val matchesInclude = includePatterns.isEmpty() || 
                    includePatterns.any { it.matches(fileName) }
                val matchesExclude = excludePatterns.any { it.matches(fileName) }

                if (matchesInclude && !matchesExclude && isTextFile(file)) {
                    searchInFile(file, pattern, results)
                }
            }
        }
    }

    private fun searchInFile(
        file: File,
        pattern: Pattern,
        results: MutableList<SearchResult>
    ) {
        try {
            // Skip large files (> 5MB)
            if (file.length() > 5 * 1024 * 1024) {
                return
            }

            val lines = file.readLines()
            val fileItem = FileItem.fromFile(file)

            lines.forEachIndexed { index, line ->
                val matcher = pattern.matcher(line)
                while (matcher.find()) {
                    results.add(
                        SearchResult(
                            file = fileItem,
                            lineNumber = index + 1,
                            lineContent = line,
                            matchStart = matcher.start(),
                            matchEnd = matcher.end()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Skip files that can't be read
        }
    }

    private fun createSearchPattern(options: SearchOptions): Pattern {
        var patternString = if (options.useRegex) {
            options.query
        } else {
            Pattern.quote(options.query)
        }

        if (options.wholeWord) {
            patternString = "\\b$patternString\\b"
        }

        val flags = if (options.caseSensitive) 0 else Pattern.CASE_INSENSITIVE
        return Pattern.compile(patternString, flags)
    }

    private fun parseGlobPatterns(pattern: String): List<Regex> {
        if (pattern.isBlank() || pattern == "*") {
            return emptyList()
        }

        return pattern.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { globToRegex(it) }
    }

    private fun globToRegex(glob: String): Regex {
        val regex = StringBuilder()
        for (char in glob) {
            when (char) {
                '*' -> regex.append(".*")
                '?' -> regex.append(".")
                '.' -> regex.append("\\.")
                else -> regex.append(char)
            }
        }
        return Regex("^${regex}$", RegexOption.IGNORE_CASE)
    }

    private fun isTextFile(file: File): Boolean {
        val textExtensions = setOf(
            // Web
            "html", "htm", "css", "scss", "sass", "less",
            "js", "jsx", "ts", "tsx", "mjs", "cjs",
            "json", "xml", "svg", "vue", "svelte",
            
            // Programming
            "py", "pyw", "r", "rmd", "lua", "rs",
            "java", "kt", "kts", "scala", "groovy",
            "c", "h", "cpp", "cc", "cxx", "hpp",
            "cs", "go", "rb", "php", "swift", "dart",
            
            // Shell & Config
            "sh", "bash", "zsh", "fish", "ps1",
            "bat", "cmd", "yaml", "yml", "toml",
            "ini", "cfg", "conf", "env", "properties",
            
            // Markup & Data
            "md", "markdown", "txt", "log", "csv",
            "sql", "graphql", "gql",
            
            // Build & Project
            "gradle", "maven", "makefile", "cmake",
            "dockerfile", "gitignore", "gitattributes",
            "editorconfig", "eslintrc", "prettierrc",
            
            // Other
            "tex", "latex", "bib", "rst"
        )

        return file.extension.lowercase() in textExtensions ||
            file.nameWithoutExtension.lowercase() in listOf(
                "makefile", "dockerfile", "gemfile", "rakefile",
                "procfile", "vagrantfile", "jenkinsfile"
            )
    }

    fun findAndReplace(
        filePath: String,
        searchText: String,
        replaceText: String,
        options: SearchOptions
    ): Int {
        val file = File(filePath)
        if (!file.exists() || !file.isFile) {
            return 0
        }

        try {
            val content = file.readText()
            val pattern = createSearchPattern(options.copy(query = searchText))
            val matcher = pattern.matcher(content)
            
            var count = 0
            val result = StringBuilder()
            var lastEnd = 0

            while (matcher.find()) {
                result.append(content.substring(lastEnd, matcher.start()))
                result.append(replaceText)
                lastEnd = matcher.end()
                count++
            }
            result.append(content.substring(lastEnd))

            if (count > 0) {
                file.writeText(result.toString())
            }

            return count
        } catch (e: Exception) {
            return 0
        }
    }

    fun findAndReplaceAll(
        directory: String,
        searchText: String,
        replaceText: String,
        options: SearchOptions
    ): Map<String, Int> {
        val results = mutableMapOf<String, Int>()
        
        searchInFiles(directory, options.copy(query = searchText)).groupBy { it.file.path }
            .forEach { (path, _) ->
                val count = findAndReplace(path, searchText, replaceText, options)
                if (count > 0) {
                    results[path] = count
                }
            }

        return results
    }
}
