package com.example.data.model

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    COMMAND
}

data class BuildLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

enum class BuildType {
    DEBUG_APK,
    RELEASE_APK,
    BUNDLE_AAB
}

enum class BuildMode {
    LOCAL,
    REMOTE
}

data class SigningSettings(
    val useReleaseSigning: Boolean = false,
    val keystorePath: String = "",
    val keystorePassword: String = "",
    val keyAlias: String = "",
    val keyPassword: String = ""
)

data class GitHubSettings(
    val personalAccessToken: String = "",
    val owner: String = "",
    val repoName: String = "",
    val branch: String = "main",
    val workflowName: String = "build-apk.yml"
)
