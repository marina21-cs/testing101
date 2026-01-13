package com.codestudio.ide.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codestudio.ide.R
import com.codestudio.ide.databinding.ActivityWebviewBinding
import com.codestudio.ide.utils.WebServer
import java.io.File

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebviewBinding
    private var webServer: WebServer? = null
    private var projectPath: String = ""
    private var serverPort: Int = 8080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Preview"

        projectPath = intent.getStringExtra("project_path") ?: ""

        setupWebView()
        startWebServer()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                cacheMode = WebSettings.LOAD_NO_CACHE
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = android.view.View.GONE
                    supportActionBar?.subtitle = url
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    if (newProgress < 100) {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                }

                override fun onConsoleMessage(
                    message: android.webkit.ConsoleMessage?
                ): Boolean {
                    message?.let {
                        android.util.Log.d(
                            "WebViewConsole",
                            "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}"
                        )
                    }
                    return true
                }
            }
        }

        // Setup refresh
        binding.swipeRefresh.setOnRefreshListener {
            binding.webView.reload()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun startWebServer() {
        if (projectPath.isBlank()) {
            Toast.makeText(this, "No project path specified", Toast.LENGTH_SHORT).show()
            return
        }

        val projectDir = File(projectPath)
        if (!projectDir.exists()) {
            Toast.makeText(this, "Project directory not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Find available port
        serverPort = findAvailablePort()

        try {
            val server = WebServer(this, serverPort)
            server.setRootDirectory(projectDir)
            server.start()
            webServer = server

            val url = "http://localhost:$serverPort/"
            binding.webView.loadUrl(url)
            binding.urlInput.setText(url)

            Toast.makeText(this, "Server started on port $serverPort", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start server: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun findAvailablePort(): Int {
        var port = 8080
        while (port < 9000) {
            try {
                java.net.ServerSocket(port).use {
                    return port
                }
            } catch (e: Exception) {
                port++
            }
        }
        return 8080
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_webview, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_refresh -> {
                binding.webView.reload()
                true
            }
            R.id.action_back -> {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                }
                true
            }
            R.id.action_forward -> {
                if (binding.webView.canGoForward()) {
                    binding.webView.goForward()
                }
                true
            }
            R.id.action_open_in_browser -> {
                val url = binding.webView.url
                if (!url.isNullOrBlank()) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                    intent.data = android.net.Uri.parse(url)
                    startActivity(intent)
                }
                true
            }
            R.id.action_dev_tools -> {
                showDevToolsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDevToolsDialog() {
        val options = arrayOf("Inspect Element (Log)", "View Source", "Clear Cache")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Developer Tools")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        binding.webView.evaluateJavascript(
                            "(function() { return document.documentElement.outerHTML; })();"
                        ) { result ->
                            android.util.Log.d("WebView", "HTML: $result")
                            Toast.makeText(this, "HTML logged to console", Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> {
                        binding.webView.evaluateJavascript(
                            "(function() { return document.documentElement.outerHTML; })();"
                        ) { result ->
                            // Show in a dialog or new activity
                            showSourceDialog(result)
                        }
                    }
                    2 -> {
                        binding.webView.clearCache(true)
                        Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    private fun showSourceDialog(source: String) {
        val cleanSource = source
            .trim()
            .removeSurrounding("\"")
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\t", "\t")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Page Source")
            .setMessage(cleanSource.take(10000)) // Limit length
            .setPositiveButton("OK", null)
            .setNeutralButton("Copy") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Source", cleanSource)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webServer?.stop()
    }
}
