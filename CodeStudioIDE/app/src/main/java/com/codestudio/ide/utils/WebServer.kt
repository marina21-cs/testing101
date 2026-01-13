package com.codestudio.ide.utils

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class WebServer(
    private val context: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    private var rootDirectory: File? = null

    fun setRootDirectory(directory: File) {
        rootDirectory = directory
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        Log.d("WebServer", "Request: $uri")

        val root = rootDirectory ?: return newFixedLengthResponse(
            Response.Status.INTERNAL_ERROR,
            MIME_PLAINTEXT,
            "No root directory set"
        )

        // Handle root path
        val filePath = if (uri == "/" || uri.isEmpty()) {
            "index.html"
        } else {
            uri.removePrefix("/")
        }

        val file = File(root, filePath)

        return if (file.exists() && file.isFile) {
            try {
                val mimeType = getMimeType(file.extension)
                val fis = FileInputStream(file)
                newChunkedResponse(Response.Status.OK, mimeType, fis)
            } catch (e: Exception) {
                newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    MIME_PLAINTEXT,
                    "Error reading file: ${e.message}"
                )
            }
        } else if (file.exists() && file.isDirectory) {
            // Try to serve index.html from directory
            val indexFile = File(file, "index.html")
            if (indexFile.exists()) {
                try {
                    val fis = FileInputStream(indexFile)
                    newChunkedResponse(Response.Status.OK, "text/html", fis)
                } catch (e: Exception) {
                    newFixedLengthResponse(
                        Response.Status.INTERNAL_ERROR,
                        MIME_PLAINTEXT,
                        "Error reading file: ${e.message}"
                    )
                }
            } else {
                // Return directory listing
                newFixedLengthResponse(
                    Response.Status.OK,
                    "text/html",
                    generateDirectoryListing(file, uri)
                )
            }
        } else {
            newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                MIME_PLAINTEXT,
                "File not found: $filePath"
            )
        }
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js", "mjs" -> "application/javascript"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "svg" -> "image/svg+xml"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "ico" -> "image/x-icon"
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "ttf" -> "font/ttf"
            "otf" -> "font/otf"
            "eot" -> "application/vnd.ms-fontobject"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "md" -> "text/markdown"
            "csv" -> "text/csv"
            "mp3" -> "audio/mpeg"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            else -> "application/octet-stream"
        }
    }

    private fun generateDirectoryListing(directory: File, currentPath: String): String {
        val files = directory.listFiles()?.sortedWith(
            compareBy({ !it.isDirectory }, { it.name.lowercase() })
        ) ?: emptyList()

        val html = StringBuilder()
        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Index of $currentPath</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: #1e1e1e;
                        color: #d4d4d4;
                        padding: 20px;
                        margin: 0;
                    }
                    h1 { color: #569cd6; margin-bottom: 20px; }
                    ul { list-style: none; padding: 0; }
                    li { padding: 8px 0; border-bottom: 1px solid #333; }
                    a { color: #9cdcfe; text-decoration: none; }
                    a:hover { color: #569cd6; }
                    .folder { color: #dcdcaa; }
                    .folder::before { content: '📁 '; }
                    .file::before { content: '📄 '; }
                </style>
            </head>
            <body>
                <h1>Index of $currentPath</h1>
                <ul>
        """.trimIndent())

        if (currentPath != "/") {
            html.append("""<li><a href="../">📂 ..</a></li>""")
        }

        files.forEach { file ->
            val name = file.name
            val path = if (currentPath.endsWith("/")) {
                "$currentPath$name"
            } else {
                "$currentPath/$name"
            }
            val cssClass = if (file.isDirectory) "folder" else "file"
            val suffix = if (file.isDirectory) "/" else ""
            html.append("""<li><a class="$cssClass" href="$path$suffix">$name</a></li>""")
        }

        html.append("""
                </ul>
            </body>
            </html>
        """.trimIndent())

        return html.toString()
    }
}
