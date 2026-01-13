package com.codestudio.ide.utils

import android.content.Context
import android.widget.Toast

object UndoRedoManager {
    private const val MAX_HISTORY = 100
    
    data class EditState(
        val content: String,
        val cursorPosition: Int
    )
    
    class FileHistory {
        private val undoStack = ArrayDeque<EditState>()
        private val redoStack = ArrayDeque<EditState>()
        private var lastSavedState: EditState? = null
        
        fun pushState(state: EditState) {
            undoStack.addLast(state)
            if (undoStack.size > MAX_HISTORY) {
                undoStack.removeFirst()
            }
            redoStack.clear()
        }
        
        fun undo(): EditState? {
            if (undoStack.size <= 1) return null
            val current = undoStack.removeLast()
            redoStack.addLast(current)
            return undoStack.lastOrNull()
        }
        
        fun redo(): EditState? {
            if (redoStack.isEmpty()) return null
            val state = redoStack.removeLast()
            undoStack.addLast(state)
            return state
        }
        
        fun canUndo(): Boolean = undoStack.size > 1
        fun canRedo(): Boolean = redoStack.isNotEmpty()
        
        fun markAsSaved() {
            lastSavedState = undoStack.lastOrNull()
        }
        
        fun hasUnsavedChanges(): Boolean {
            return undoStack.lastOrNull() != lastSavedState
        }
        
        fun clear() {
            undoStack.clear()
            redoStack.clear()
            lastSavedState = null
        }
    }
    
    private val fileHistories = mutableMapOf<String, FileHistory>()
    
    fun getHistory(filePath: String): FileHistory {
        return fileHistories.getOrPut(filePath) { FileHistory() }
    }
    
    fun clearHistory(filePath: String) {
        fileHistories.remove(filePath)
    }
    
    fun clearAllHistories() {
        fileHistories.clear()
    }
}

object ClipboardManager {
    private var clipboardContent: String = ""
    
    fun copy(text: String, context: Context) {
        clipboardContent = text
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("CodeStudio", text)
        clipboard.setPrimaryClip(clip)
    }
    
    fun paste(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        return clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: clipboardContent
    }
    
    fun cut(text: String, context: Context) {
        copy(text, context)
    }
}

object TextUtils {
    fun getLineNumber(text: String, position: Int): Int {
        var line = 1
        for (i in 0 until position.coerceAtMost(text.length)) {
            if (text[i] == '\n') line++
        }
        return line
    }
    
    fun getColumnNumber(text: String, position: Int): Int {
        val lastNewLine = text.lastIndexOf('\n', position - 1)
        return position - lastNewLine
    }
    
    fun getLineStartPosition(text: String, lineNumber: Int): Int {
        var line = 1
        for (i in text.indices) {
            if (line == lineNumber) return i
            if (text[i] == '\n') line++
        }
        return text.length
    }
    
    fun getLineEndPosition(text: String, lineNumber: Int): Int {
        var line = 1
        for (i in text.indices) {
            if (line == lineNumber && text[i] == '\n') return i
            if (text[i] == '\n') line++
        }
        return text.length
    }
    
    fun getLineContent(text: String, lineNumber: Int): String {
        val lines = text.split("\n")
        return if (lineNumber in 1..lines.size) {
            lines[lineNumber - 1]
        } else {
            ""
        }
    }
    
    fun getTotalLines(text: String): Int {
        return text.count { it == '\n' } + 1
    }
    
    fun getIndentation(line: String): String {
        val match = Regex("^\\s*").find(line)
        return match?.value ?: ""
    }
    
    fun insertText(text: String, position: Int, insertion: String): String {
        return text.substring(0, position) + insertion + text.substring(position)
    }
    
    fun deleteText(text: String, start: Int, end: Int): String {
        return text.substring(0, start) + text.substring(end)
    }
    
    fun replaceText(text: String, start: Int, end: Int, replacement: String): String {
        return text.substring(0, start) + replacement + text.substring(end)
    }
}
