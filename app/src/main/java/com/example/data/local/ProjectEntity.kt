package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ProjectConfig
import com.example.data.model.ProjectItem

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val homepage: String,
    val mode: String, // "local" or "remote"
    val remoteUrl: String,
    val orientation: String,
    val titleBarColor: String,
    val fullscreen: Boolean,
    val hideTitleBar: Boolean,
    val allowZoom: Boolean,
    val allowLongPress: Boolean,
    val loadingUI: Boolean,
    val swipeRefresh: Boolean,
    val camera: Boolean,
    val microphone: Boolean,
    val mediaAutoplay: Boolean,
    val pcMode: Boolean,
    val permissionNotifications: Boolean,
    val splashScreen: Boolean,
    val splashDurationMs: Int,
    val splashColor: String,
    val gitHubRepo: String,
    val gitHubBranch: String,
    val lastBuildStatus: String,
    val lastBuildTime: Long,
    val lastBuildLog: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toProjectItem(): ProjectItem {
        val config = toConfig()
        return ProjectItem(
            id = id,
            name = name,
            packageName = packageName,
            mode = mode,
            remoteUrl = remoteUrl,
            homepage = homepage,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastBuildStatus = lastBuildStatus,
            config = config
        )
    }

    fun toConfig(): ProjectConfig {
        return ProjectConfig(
            id = id,
            name = name,
            packageName = packageName,
            versionName = versionName,
            versionCode = versionCode,
            homepage = homepage,
            mode = mode,
            remoteUrl = remoteUrl,
            orientation = orientation,
            titleBarColor = titleBarColor,
            fullscreen = fullscreen,
            hideTitleBar = hideTitleBar,
            allowZoom = allowZoom,
            allowLongPress = allowLongPress,
            loadingUI = loadingUI,
            swipeRefresh = swipeRefresh,
            camera = camera,
            microphone = microphone,
            mediaAutoplay = mediaAutoplay,
            pcMode = pcMode,
            permissionNotifications = permissionNotifications,
            splashScreen = splashScreen,
            splashDurationMs = splashDurationMs,
            splashColor = splashColor,
            gitHubRepo = gitHubRepo,
            gitHubBranch = gitHubBranch,
            lastBuildStatus = lastBuildStatus,
            lastBuildTime = lastBuildTime,
            lastBuildLog = lastBuildLog
        )
    }

    companion object {
        fun fromConfig(config: ProjectConfig, createdAt: Long = System.currentTimeMillis()): ProjectEntity {
            return ProjectEntity(
                id = config.id,
                name = config.name,
                packageName = config.packageName,
                versionName = config.versionName,
                versionCode = config.versionCode,
                homepage = config.homepage,
                mode = config.mode,
                remoteUrl = config.remoteUrl,
                orientation = config.orientation,
                titleBarColor = config.titleBarColor,
                fullscreen = config.fullscreen,
                hideTitleBar = config.hideTitleBar,
                allowZoom = config.allowZoom,
                allowLongPress = config.allowLongPress,
                loadingUI = config.loadingUI,
                swipeRefresh = config.swipeRefresh,
                camera = config.camera,
                microphone = config.microphone,
                mediaAutoplay = config.mediaAutoplay,
                pcMode = config.pcMode,
                permissionNotifications = config.permissionNotifications,
                splashScreen = config.splashScreen,
                splashDurationMs = config.splashDurationMs,
                splashColor = config.splashColor,
                gitHubRepo = config.gitHubRepo,
                gitHubBranch = config.gitHubBranch,
                lastBuildStatus = config.lastBuildStatus,
                lastBuildTime = config.lastBuildTime,
                lastBuildLog = config.lastBuildLog,
                createdAt = createdAt,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
