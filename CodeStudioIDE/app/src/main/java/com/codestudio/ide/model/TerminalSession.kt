package com.codestudio.ide.model

data class TerminalSession(
    val id: String,
    val name: String = "Terminal",
    var currentDirectory: String,
    val history: MutableList<String> = mutableListOf(),
    val output: StringBuilder = StringBuilder(),
    var isRunning: Boolean = false
)

data class TerminalCommand(
    val command: String,
    val output: String,
    val exitCode: Int,
    val timestamp: Long = System.currentTimeMillis()
)
