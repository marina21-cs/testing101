package com.codestudio.ide.model

data class Project(
    val id: Long = 0,
    val name: String,
    val path: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val type: ProjectType = ProjectType.GENERIC
)

enum class ProjectType {
    GENERIC,
    WEB,
    PYTHON,
    RUST,
    LUA,
    R_LANG,
    ANDROID,
    NODEJS
}
