package com.codestudio.ide.model

data class EditorSettings(
    var fontSize: Int = 14,
    var fontFamily: String = "JetBrains Mono",
    var tabSize: Int = 4,
    var useSpaces: Boolean = true,
    var showLineNumbers: Boolean = true,
    var wordWrap: Boolean = false,
    var autoSave: Boolean = false,
    var autoSaveInterval: Int = 30,
    var highlightCurrentLine: Boolean = true,
    var showWhitespace: Boolean = false,
    var autoCloseBrackets: Boolean = true,
    var autoCloseQuotes: Boolean = true,
    var autoIndent: Boolean = true,
    var theme: String = "dark"
)
