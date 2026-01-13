package com.codestudio.ide.model

data class OpenFile(
    val fileItem: FileItem,
    var content: String = "",
    var originalContent: String = "",
    var cursorPosition: Int = 0,
    var scrollPosition: Int = 0,
    var isModified: Boolean = false,
    var encoding: String = "UTF-8"
) {
    val hasUnsavedChanges: Boolean
        get() = content != originalContent
    
    fun markAsSaved() {
        originalContent = content
        isModified = false
    }
}
