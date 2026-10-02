package com.example.data.model

enum class WebFileType {
    HTML,
    CSS,
    JAVASCRIPT,
    JSON,
    IMAGE,
    FONT,
    TEXT,
    OTHER;

    companion object {
        fun fromExtension(ext: String): WebFileType {
            return when (ext.lowercase()) {
                "html", "htm" -> HTML
                "css", "scss", "sass" -> CSS
                "js", "mjs", "ts" -> JAVASCRIPT
                "json" -> JSON
                "png", "jpg", "jpeg", "svg", "webp", "gif", "ico" -> IMAGE
                "ttf", "woff", "woff2", "otf" -> FONT
                "txt", "md" -> TEXT
                else -> OTHER
            }
        }
    }
}

data class FileItem(
    val path: String, // Relative path from website root
    val name: String,
    val isDirectory: Boolean,
    val extension: String,
    val size: Long,
    val lastModified: Long,
    val fileType: WebFileType
)

data class EditorTab(
    val path: String,
    val name: String,
    val isModified: Boolean = false
)
