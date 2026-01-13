package com.codestudio.ide.ui.main

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codestudio.ide.CodeStudioApp
import com.codestudio.ide.model.FileItem
import com.codestudio.ide.model.OpenFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val fileManager = CodeStudioApp.instance.fileManager
    private val preferencesManager = CodeStudioApp.instance.preferencesManager

    private val _fileTree = MutableStateFlow<List<FileItem>>(emptyList())
    val fileTree: StateFlow<List<FileItem>> = _fileTree

    private val _openFiles = MutableStateFlow<List<OpenFile>>(emptyList())
    val openFiles: StateFlow<List<OpenFile>> = _openFiles

    private val _currentFile = MutableStateFlow<OpenFile?>(null)
    val currentFile: StateFlow<OpenFile?> = _currentFile

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val expandedFolders = mutableSetOf<String>()

    fun loadFileTree(rootPath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val showHidden = preferencesManager.showHiddenFiles
            val files = fileManager.getFileTree(rootPath, showHidden)
            val flatList = flattenFileTree(files)
            _fileTree.value = flatList
        }
    }

    private fun flattenFileTree(files: List<FileItem>, depth: Int = 0): List<FileItem> {
        val result = mutableListOf<FileItem>()
        files.forEach { file ->
            val item = file.copy(depth = depth, isExpanded = expandedFolders.contains(file.path))
            result.add(item)
            if (file.isDirectory && expandedFolders.contains(file.path)) {
                result.addAll(flattenFileTree(file.children, depth + 1))
            }
        }
        return result
    }

    fun toggleFolder(fileItem: FileItem) {
        if (expandedFolders.contains(fileItem.path)) {
            expandedFolders.remove(fileItem.path)
        } else {
            expandedFolders.add(fileItem.path)
        }
        
        // Reload the current tree
        val root = _fileTree.value.firstOrNull()?.let { 
            File(it.path).parent 
        } ?: return
        
        loadFileTree(root)
    }

    fun openFile(fileItem: FileItem) {
        if (fileItem.isDirectory) return

        viewModelScope.launch(Dispatchers.IO) {
            // Check if file is already open
            val existingFile = _openFiles.value.find { it.fileItem.path == fileItem.path }
            if (existingFile != null) {
                _currentFile.value = existingFile
                return@launch
            }

            // Read file content
            val result = fileManager.readFile(fileItem.path)
            result.onSuccess { content ->
                val openFile = OpenFile(
                    fileItem = fileItem,
                    content = content,
                    originalContent = content
                )
                
                val updatedList = _openFiles.value.toMutableList()
                updatedList.add(openFile)
                _openFiles.value = updatedList
                _currentFile.value = openFile
            }.onFailure { error ->
                _statusMessage.value = "Error opening file: ${error.message}"
            }
        }
    }

    fun openExternalFile(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = fileManager.readFileFromUri(uri)
            result.onSuccess { content ->
                val fileName = uri.lastPathSegment ?: "Untitled"
                val fileItem = FileItem(
                    name = fileName,
                    path = uri.toString(),
                    isDirectory = false,
                    extension = fileName.substringAfterLast('.', "")
                )
                
                val openFile = OpenFile(
                    fileItem = fileItem,
                    content = content,
                    originalContent = content
                )
                
                val updatedList = _openFiles.value.toMutableList()
                updatedList.add(openFile)
                _openFiles.value = updatedList
                _currentFile.value = openFile
            }.onFailure { error ->
                _statusMessage.value = "Error opening file: ${error.message}"
            }
        }
    }

    fun selectTab(position: Int) {
        if (position in _openFiles.value.indices) {
            _currentFile.value = _openFiles.value[position]
        }
    }

    fun closeTab(position: Int) {
        if (position !in _openFiles.value.indices) return

        val fileToClose = _openFiles.value[position]
        
        // Check for unsaved changes
        if (fileToClose.hasUnsavedChanges) {
            // This should trigger a dialog in the UI
            _statusMessage.value = "File has unsaved changes"
            return
        }

        val updatedList = _openFiles.value.toMutableList()
        updatedList.removeAt(position)
        _openFiles.value = updatedList

        // Update current file
        _currentFile.value = when {
            updatedList.isEmpty() -> null
            position < updatedList.size -> updatedList[position]
            else -> updatedList.lastOrNull()
        }
    }

    fun onEditorContentChanged(newContent: String) {
        val current = _currentFile.value ?: return
        
        val updatedFile = current.copy(
            content = newContent,
            isModified = newContent != current.originalContent
        )
        
        // Update in open files list
        val updatedList = _openFiles.value.map { 
            if (it.fileItem.path == current.fileItem.path) updatedFile else it 
        }
        _openFiles.value = updatedList
        _currentFile.value = updatedFile
    }

    fun saveCurrentFile() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _currentFile.value ?: run {
                _statusMessage.value = "No file to save"
                return@launch
            }

            val result = fileManager.writeFile(current.fileItem.path, current.content)
            result.onSuccess {
                val savedFile = current.copy(
                    originalContent = current.content,
                    isModified = false
                )
                
                // Update in open files list
                val updatedList = _openFiles.value.map { 
                    if (it.fileItem.path == current.fileItem.path) savedFile else it 
                }
                _openFiles.value = updatedList
                _currentFile.value = savedFile
                
                _statusMessage.value = "File saved: ${current.fileItem.name}"
            }.onFailure { error ->
                _statusMessage.value = "Error saving file: ${error.message}"
            }
        }
    }

    fun saveAllFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            var savedCount = 0
            var errorCount = 0

            _openFiles.value.filter { it.hasUnsavedChanges }.forEach { file ->
                val result = fileManager.writeFile(file.fileItem.path, file.content)
                if (result.isSuccess) {
                    savedCount++
                } else {
                    errorCount++
                }
            }

            // Update all files as saved
            val updatedList = _openFiles.value.map { it.copy(originalContent = it.content, isModified = false) }
            _openFiles.value = updatedList
            _currentFile.value?.let { current ->
                _currentFile.value = updatedList.find { it.fileItem.path == current.fileItem.path }
            }

            withContext(Dispatchers.Main) {
                _statusMessage.value = when {
                    errorCount > 0 -> "Saved $savedCount files, $errorCount errors"
                    savedCount > 0 -> "Saved $savedCount files"
                    else -> "No files to save"
                }
            }
        }
    }

    fun createFile(parentPath: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val filePath = "$parentPath/$name"
            val result = fileManager.createFile(filePath)
            
            result.onSuccess { file ->
                // Reload file tree
                loadFileTree(File(parentPath).parent ?: parentPath)
                
                // Open the new file
                openFile(FileItem.fromFile(file))
                
                _statusMessage.value = "Created: $name"
            }.onFailure { error ->
                _statusMessage.value = "Error creating file: ${error.message}"
            }
        }
    }

    fun createDirectory(parentPath: String, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dirPath = "$parentPath/$name"
            val result = fileManager.createDirectory(dirPath)
            
            result.onSuccess {
                loadFileTree(File(parentPath).parent ?: parentPath)
                _statusMessage.value = "Created folder: $name"
            }.onFailure { error ->
                _statusMessage.value = "Error creating folder: ${error.message}"
            }
        }
    }

    fun renameFile(fileItem: FileItem, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val parentPath = File(fileItem.path).parent ?: return@launch
            val newPath = "$parentPath/$newName"
            
            val result = fileManager.renameFile(fileItem.path, newPath)
            
            result.onSuccess {
                loadFileTree(parentPath)
                _statusMessage.value = "Renamed to: $newName"
            }.onFailure { error ->
                _statusMessage.value = "Error renaming: ${error.message}"
            }
        }
    }

    fun deleteFile(fileItem: FileItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val parentPath = File(fileItem.path).parent ?: return@launch
            
            // Close file if open
            val openIndex = _openFiles.value.indexOfFirst { it.fileItem.path == fileItem.path }
            if (openIndex >= 0) {
                val updatedList = _openFiles.value.toMutableList()
                updatedList.removeAt(openIndex)
                _openFiles.value = updatedList
                
                if (_currentFile.value?.fileItem?.path == fileItem.path) {
                    _currentFile.value = updatedList.firstOrNull()
                }
            }
            
            val result = fileManager.deleteFile(fileItem.path)
            
            result.onSuccess {
                loadFileTree(parentPath)
                _statusMessage.value = "Deleted: ${fileItem.name}"
            }.onFailure { error ->
                _statusMessage.value = "Error deleting: ${error.message}"
            }
        }
    }

    fun duplicateFile(fileItem: FileItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val parentPath = File(fileItem.path).parent ?: return@launch
            val baseName = fileItem.name.substringBeforeLast('.')
            val extension = if (fileItem.name.contains('.')) ".${fileItem.extension}" else ""
            
            var counter = 1
            var newPath: String
            do {
                newPath = "$parentPath/${baseName}_copy$counter$extension"
                counter++
            } while (File(newPath).exists())
            
            val result = fileManager.copyFile(fileItem.path, newPath)
            
            result.onSuccess {
                loadFileTree(parentPath)
                _statusMessage.value = "Duplicated: ${File(newPath).name}"
            }.onFailure { error ->
                _statusMessage.value = "Error duplicating: ${error.message}"
            }
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return _openFiles.value.any { it.hasUnsavedChanges }
    }
}
