package com.codestudio.ide.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val path: String,
    val type: String = "GENERIC",
    val createdAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_files")
data class RecentFileEntity(
    @PrimaryKey
    val path: String,
    val name: String,
    val projectId: Long? = null,
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val cursorPosition: Int = 0,
    val scrollPosition: Int = 0
)

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val prefix: String,
    val body: String,
    val language: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
