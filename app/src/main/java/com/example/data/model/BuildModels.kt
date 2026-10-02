package com.example.data.model

import java.io.File

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

enum class ComponentStatus {
    INSTALLED,
    CONFIGURED,
    MISSING,
    DOWNLOADING,
    ERROR
}

data class EnvironmentComponentInfo(
    val name: String,
    val status: ComponentStatus,
    val version: String,
    val path: String,
    val details: String,
    val isRequired: Boolean = true
)

data class BuildEnvironmentReport(
    val components: List<EnvironmentComponentInfo>,
    val availableStorageMb: Long,
    val availableMemoryMb: Long,
    val totalMemoryMb: Long,
    val isOnline: Boolean,
    val architecture: String,
    val canBuildLocally: Boolean
)

data class BuildOutputInfo(
    val apkFile: File,
    val apkName: String,
    val apkSizeFormatted: String,
    val apkSizeBytes: Long,
    val versionName: String,
    val buildDurationMs: Long,
    val isRelease: Boolean
)

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
    val workflowName: String = "build-and-release.yml"
)
