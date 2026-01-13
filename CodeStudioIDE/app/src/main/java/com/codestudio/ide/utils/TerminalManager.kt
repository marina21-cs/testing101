package com.codestudio.ide.utils

import android.os.Handler
import android.os.Looper
import com.codestudio.ide.model.TerminalCommand
import com.codestudio.ide.model.TerminalSession
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.UUID
import java.util.concurrent.Executors

class TerminalManager {
    
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private val sessions = mutableMapOf<String, TerminalSession>()
    private var currentSessionId: String? = null

    interface TerminalCallback {
        fun onOutput(output: String)
        fun onError(error: String)
        fun onCommandComplete(command: TerminalCommand)
        fun onDirectoryChanged(newDirectory: String)
    }

    fun createSession(initialDirectory: String): TerminalSession {
        val session = TerminalSession(
            id = UUID.randomUUID().toString(),
            currentDirectory = initialDirectory
        )
        sessions[session.id] = session
        if (currentSessionId == null) {
            currentSessionId = session.id
        }
        return session
    }

    fun getSession(id: String): TerminalSession? = sessions[id]

    fun getCurrentSession(): TerminalSession? = currentSessionId?.let { sessions[it] }

    fun setCurrentSession(id: String) {
        if (sessions.containsKey(id)) {
            currentSessionId = id
        }
    }

    fun closeSession(id: String) {
        sessions.remove(id)
        if (currentSessionId == id) {
            currentSessionId = sessions.keys.firstOrNull()
        }
    }

    fun executeCommand(
        sessionId: String,
        command: String,
        callback: TerminalCallback
    ) {
        val session = sessions[sessionId] ?: return
        
        session.history.add(command)
        session.isRunning = true

        executor.execute {
            try {
                val result = executeShellCommand(command, session.currentDirectory, callback)
                
                // Handle cd command
                if (command.trim().startsWith("cd ")) {
                    val newDir = command.trim().removePrefix("cd ").trim()
                    val targetDir = if (newDir.startsWith("/")) {
                        File(newDir)
                    } else if (newDir == "~") {
                        File(System.getProperty("user.home") ?: session.currentDirectory)
                    } else if (newDir == "..") {
                        File(session.currentDirectory).parentFile ?: File(session.currentDirectory)
                    } else {
                        File(session.currentDirectory, newDir)
                    }
                    
                    if (targetDir.exists() && targetDir.isDirectory) {
                        session.currentDirectory = targetDir.absolutePath
                        mainHandler.post {
                            callback.onDirectoryChanged(session.currentDirectory)
                        }
                    } else {
                        mainHandler.post {
                            callback.onError("cd: no such file or directory: $newDir")
                        }
                    }
                }

                val terminalCommand = TerminalCommand(
                    command = command,
                    output = result.first,
                    exitCode = result.second
                )
                
                session.output.append("$ $command\n")
                session.output.append(result.first)
                if (!result.first.endsWith("\n")) {
                    session.output.append("\n")
                }
                
                session.isRunning = false
                
                mainHandler.post {
                    callback.onCommandComplete(terminalCommand)
                }
                
            } catch (e: Exception) {
                session.isRunning = false
                mainHandler.post {
                    callback.onError("Error: ${e.message}")
                }
            }
        }
    }

    private fun executeShellCommand(
        command: String,
        workingDirectory: String,
        callback: TerminalCallback
    ): Pair<String, Int> {
        val output = StringBuilder()
        var exitCode = 0

        try {
            // Handle built-in commands
            when {
                command.trim() == "clear" || command.trim() == "cls" -> {
                    return Pair("\u001B[2J\u001B[H", 0)
                }
                command.trim() == "pwd" -> {
                    return Pair(workingDirectory, 0)
                }
                command.trim().startsWith("echo ") -> {
                    val text = command.trim().removePrefix("echo ").trim()
                    // Handle quotes
                    val result = text.removeSurrounding("\"").removeSurrounding("'")
                    return Pair(result, 0)
                }
                command.trim().startsWith("cd ") -> {
                    // Handled separately in executeCommand
                    return Pair("", 0)
                }
                command.trim() == "help" -> {
                    return Pair(getHelpText(), 0)
                }
            }

            val processBuilder = ProcessBuilder()
            processBuilder.directory(File(workingDirectory))
            processBuilder.redirectErrorStream(true)
            
            // Use sh -c for complex commands
            processBuilder.command("sh", "-c", command)
            
            val process = processBuilder.start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
                val currentLine = line
                mainHandler.post {
                    callback.onOutput(currentLine + "\n")
                }
            }
            
            exitCode = process.waitFor()
            
        } catch (e: Exception) {
            output.append("Error: ${e.message}\n")
            exitCode = 1
        }

        return Pair(output.toString(), exitCode)
    }

    private fun getHelpText(): String {
        return """
            |CodeStudio Terminal - Available Commands:
            |
            |  File Operations:
            |    ls, dir       - List directory contents
            |    cd <path>     - Change directory
            |    pwd           - Print working directory
            |    cat <file>    - Display file contents
            |    touch <file>  - Create empty file
            |    mkdir <dir>   - Create directory
            |    rm <file>     - Remove file
            |    cp <src> <dst> - Copy file
            |    mv <src> <dst> - Move/rename file
            |
            |  Text Processing:
            |    echo <text>   - Print text
            |    grep <pattern> <file> - Search in files
            |    head <file>   - Show first lines
            |    tail <file>   - Show last lines
            |
            |  System:
            |    clear, cls    - Clear screen
            |    help          - Show this help
            |    exit          - Close terminal
            |
            |  Package Managers (if installed):
            |    npm, yarn, pip, cargo, luarocks
            |
        """.trimMargin()
    }

    fun getCommandHistory(sessionId: String): List<String> {
        return sessions[sessionId]?.history ?: emptyList()
    }

    fun clearSession(sessionId: String) {
        sessions[sessionId]?.output?.clear()
    }

    fun getAllSessions(): List<TerminalSession> = sessions.values.toList()
}
