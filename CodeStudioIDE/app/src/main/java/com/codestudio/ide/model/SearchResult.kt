package com.codestudio.ide.model

data class SearchResult(
    val file: FileItem,
    val lineNumber: Int,
    val lineContent: String,
    val matchStart: Int,
    val matchEnd: Int
)

data class SearchOptions(
    val query: String,
    val caseSensitive: Boolean = false,
    val wholeWord: Boolean = false,
    val useRegex: Boolean = false,
    val includePattern: String = "*",
    val excludePattern: String = ""
)
