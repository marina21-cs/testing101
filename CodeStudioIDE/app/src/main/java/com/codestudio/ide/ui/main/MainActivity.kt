package com.codestudio.ide.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codestudio.ide.CodeStudioApp
import com.codestudio.ide.R
import com.codestudio.ide.databinding.ActivityMainBinding
import com.codestudio.ide.model.FileItem
import com.codestudio.ide.model.OpenFile
import com.codestudio.ide.ui.adapter.FileTreeAdapter
import com.codestudio.ide.ui.adapter.TabAdapter
import com.codestudio.ide.ui.dialogs.CreateFileDialog
import com.codestudio.ide.ui.dialogs.SearchDialog
import com.codestudio.ide.ui.settings.SettingsActivity
import com.codestudio.ide.ui.webview.WebViewActivity
import com.codestudio.ide.utils.TerminalManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var fileTreeAdapter: FileTreeAdapter
    private lateinit var tabAdapter: TabAdapter
    private lateinit var terminalManager: TerminalManager
    private lateinit var toggle: ActionBarDrawerToggle

    private var currentProjectPath: String = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            loadProject()
        } else {
            showPermissionRationale()
        }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { openExternalFile(it) }
    }

    private val openFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { openExternalFolder(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        terminalManager = TerminalManager()

        setupToolbar()
        setupDrawer()
        setupFileTree()
        setupEditor()
        setupTabs()
        setupTerminal()
        setupBottomNavigation()
        setupFab()
        observeViewModel()

        checkPermissions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = getString(R.string.app_name)
        }
    }

    private fun setupDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    private fun setupFileTree() {
        fileTreeAdapter = FileTreeAdapter(
            onFileClick = { fileItem ->
                if (!fileItem.isDirectory) {
                    openFile(fileItem)
                }
            },
            onFileLongClick = { fileItem ->
                showFileContextMenu(fileItem)
                true
            },
            onFolderToggle = { fileItem ->
                viewModel.toggleFolder(fileItem)
            }
        )

        binding.fileTreeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = fileTreeAdapter
        }
    }

    private fun setupEditor() {
        try {
            binding.codeEditor.apply {
                setTextSize(14f)
                isWordwrap = false
                isLineNumberEnabled = true
                setTabWidth(4)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up editor", e)
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.selectTab(it.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupTerminal() {
        try {
            val session = terminalManager.createSession(
                CodeStudioApp.instance.fileManager.getProjectsDirectory().absolutePath
            )

            binding.terminalInput.setOnEditorActionListener { _, _, _ ->
                val command = binding.terminalInput.text.toString()
                if (command.isNotBlank()) {
                    executeTerminalCommand(command)
                    binding.terminalInput.text?.clear()
                }
                true
            }

            binding.terminalOutput.text = "CodeStudio Terminal\n$ "
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up terminal", e)
            binding.terminalOutput.text = "Terminal initialization error\n$ "
        }
    }

    private fun executeTerminalCommand(command: String) {
        val session = terminalManager.getCurrentSession() ?: return

        terminalManager.executeCommand(session.id, command, object : TerminalManager.TerminalCallback {
            override fun onOutput(output: String) {
                runOnUiThread {
                    binding.terminalOutput.append(output)
                    binding.terminalScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    binding.terminalOutput.append("\u001B[31m$error\u001B[0m\n")
                    binding.terminalScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }

            override fun onCommandComplete(cmd: com.codestudio.ide.model.TerminalCommand) {
                runOnUiThread {
                    binding.terminalOutput.append("$ ")
                    binding.terminalScrollView.fullScroll(View.FOCUS_DOWN)
                }
            }

            override fun onDirectoryChanged(newDirectory: String) {
                // Update prompt or status
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_files -> {
                    showPanel(PanelType.FILES)
                    true
                }
                R.id.nav_search -> {
                    showSearchDialog()
                    true
                }
                R.id.nav_terminal -> {
                    showPanel(PanelType.TERMINAL)
                    true
                }
                R.id.nav_preview -> {
                    launchWebPreview()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFab() {
        binding.fabNewFile.setOnClickListener {
            showCreateFileDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.fileTree.collect { files ->
                fileTreeAdapter.submitList(files)
            }
        }

        lifecycleScope.launch {
            viewModel.openFiles.collect { files ->
                updateTabs(files)
            }
        }

        lifecycleScope.launch {
            viewModel.currentFile.collect { file ->
                file?.let { updateEditor(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.statusMessage.collect { message ->
                message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTabs(files: List<OpenFile>) {
        binding.tabLayout.removeAllTabs()
        files.forEach { file ->
            val tab = binding.tabLayout.newTab()
            val title = if (file.hasUnsavedChanges) "● ${file.fileItem.name}" else file.fileItem.name
            tab.text = title
            binding.tabLayout.addTab(tab)
        }

        // Select current tab
        viewModel.currentFile.value?.let { current ->
            val index = files.indexOfFirst { it.fileItem.path == current.fileItem.path }
            if (index >= 0 && index < binding.tabLayout.tabCount) {
                binding.tabLayout.getTabAt(index)?.select()
            }
        }
    }

    private fun updateEditor(openFile: OpenFile) {
        binding.codeEditor.setText(openFile.content)
        binding.codeEditor.setSelection(openFile.cursorPosition, openFile.cursorPosition)
        
        // Update title
        supportActionBar?.subtitle = openFile.fileItem.name
    }

    private fun openFile(fileItem: FileItem) {
        viewModel.openFile(fileItem)
    }

    private fun openExternalFile(uri: Uri) {
        viewModel.openExternalFile(uri)
    }

    private fun openExternalFolder(uri: Uri) {
        // Handle external folder
        Toast.makeText(this, "Folder access: $uri", Toast.LENGTH_SHORT).show()
    }

    private fun showFileContextMenu(fileItem: FileItem) {
        val items = if (fileItem.isDirectory) {
            arrayOf("New File", "New Folder", "Rename", "Delete", "Copy Path")
        } else {
            arrayOf("Open", "Rename", "Delete", "Copy Path", "Duplicate")
        }

        AlertDialog.Builder(this)
            .setTitle(fileItem.name)
            .setItems(items) { _, which ->
                when (items[which]) {
                    "Open" -> openFile(fileItem)
                    "New File" -> showCreateFileDialog(fileItem.path)
                    "New Folder" -> showCreateFolderDialog(fileItem.path)
                    "Rename" -> showRenameDialog(fileItem)
                    "Delete" -> showDeleteConfirmation(fileItem)
                    "Copy Path" -> copyToClipboard(fileItem.path)
                    "Duplicate" -> viewModel.duplicateFile(fileItem)
                }
            }
            .show()
    }

    private fun showCreateFileDialog(parentPath: String? = null) {
        CreateFileDialog(
            context = this,
            parentPath = parentPath ?: currentProjectPath,
            isDirectory = false
        ) { name, path ->
            viewModel.createFile(path, name)
        }.show()
    }

    private fun showCreateFolderDialog(parentPath: String? = null) {
        CreateFileDialog(
            context = this,
            parentPath = parentPath ?: currentProjectPath,
            isDirectory = true
        ) { name, path ->
            viewModel.createDirectory(path, name)
        }.show()
    }

    private fun showRenameDialog(fileItem: FileItem) {
        val builder = AlertDialog.Builder(this)
        val input = android.widget.EditText(this)
        input.setText(fileItem.name)
        
        builder.setTitle("Rename")
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotBlank()) {
                    viewModel.renameFile(fileItem, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(fileItem: FileItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete '${fileItem.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFile(fileItem)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSearchDialog() {
        SearchDialog(this, currentProjectPath) { result ->
            openFile(result.file)
            // TODO: Scroll to line
        }.show()
    }

    private fun launchWebPreview() {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("project_path", currentProjectPath)
        startActivity(intent)
    }

    private fun showPanel(panelType: PanelType) {
        binding.fileTreeContainer.visibility = 
            if (panelType == PanelType.FILES) View.VISIBLE else View.GONE
        binding.terminalContainer.visibility = 
            if (panelType == PanelType.TERMINAL) View.VISIBLE else View.GONE
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Path", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showStoragePermissionDialog()
            } else {
                loadProject()
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            
            if (permissions.all { 
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED 
            }) {
                loadProject()
            } else {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }

    private fun showStoragePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission")
            .setMessage("CodeStudio needs storage access to manage your projects. You can also use the internal workspace.")
            .setPositiveButton("Grant Access") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
            .setNegativeButton("Use Internal Storage") { _, _ ->
                loadProject()
            }
            .show()
    }

    private fun showPermissionRationale() {
        Snackbar.make(
            binding.root,
            "Storage permission is needed for file access",
            Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }.show()
        
        // Load internal storage anyway
        loadProject()
    }

    private fun loadProject() {
        val projectsDir = CodeStudioApp.instance.fileManager.getProjectsDirectory()
        currentProjectPath = projectsDir.absolutePath
        
        // Check for last opened project
        CodeStudioApp.instance.preferencesManager.lastOpenedProject?.let { lastProject ->
            if (File(lastProject).exists()) {
                currentProjectPath = lastProject
            }
        }
        
        viewModel.loadFileTree(currentProjectPath)
        supportActionBar?.subtitle = File(currentProjectPath).name
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_new_file -> showCreateFileDialog()
            R.id.nav_new_folder -> showCreateFolderDialog()
            R.id.nav_open_file -> openDocumentLauncher.launch(arrayOf("*/*"))
            R.id.nav_open_folder -> openFolderLauncher.launch(null)
            R.id.nav_save -> viewModel.saveCurrentFile()
            R.id.nav_save_all -> viewModel.saveAllFiles()
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_about -> showAboutDialog()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("CodeStudio IDE")
            .setMessage("Version 1.0.0\n\nA powerful code editor for Android inspired by VS Code and Positron.\n\nFeatures:\n• Multi-language syntax highlighting\n• File management\n• Integrated terminal\n• Web preview\n• Find & Replace\n• And more!")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                viewModel.saveCurrentFile()
                true
            }
            R.id.action_undo -> {
                binding.codeEditor.undo()
                true
            }
            R.id.action_redo -> {
                binding.codeEditor.redo()
                true
            }
            R.id.action_find -> {
                showFindReplaceDialog()
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFindReplaceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_find_replace, null)
        val findInput = dialogView.findViewById<android.widget.EditText>(R.id.findInput)
        val replaceInput = dialogView.findViewById<android.widget.EditText>(R.id.replaceInput)

        AlertDialog.Builder(this)
            .setTitle("Find & Replace")
            .setView(dialogView)
            .setPositiveButton("Find") { _, _ ->
                val query = findInput.text.toString()
                if (query.isNotBlank()) {
                    val content = binding.codeEditor.text.toString()
                    val index = content.indexOf(query)
                    if (index >= 0) {
                        binding.codeEditor.setSelection(index, index + query.length)
                    } else {
                        Toast.makeText(this, "Not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNeutralButton("Replace All") { _, _ ->
                val query = findInput.text.toString()
                val replacement = replaceInput.text.toString()
                if (query.isNotBlank()) {
                    val content = binding.codeEditor.text.toString()
                    val newContent = content.replace(query, replacement)
                    binding.codeEditor.setText(newContent)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            viewModel.hasUnsavedChanges() -> {
                showUnsavedChangesDialog()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun showUnsavedChangesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Unsaved Changes")
            .setMessage("You have unsaved changes. Do you want to save before exiting?")
            .setPositiveButton("Save") { _, _ ->
                viewModel.saveAllFiles()
                finish()
            }
            .setNegativeButton("Discard") { _, _ ->
                finish()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        // Auto-save if enabled
        if (CodeStudioApp.instance.preferencesManager.autoSaveEnabled) {
            viewModel.saveAllFiles()
        }
        
        // Save state
        CodeStudioApp.instance.preferencesManager.lastOpenedProject = currentProjectPath
    }

    enum class PanelType {
        FILES, TERMINAL
    }
}
