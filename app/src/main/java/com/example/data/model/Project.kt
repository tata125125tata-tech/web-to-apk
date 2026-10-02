package com.example.data.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class ProjectConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String,
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val homepage: String = "index.html",
    val mode: String = "local", // "local" or "remote"
    val remoteUrl: String = "https://example.com",
    val orientation: String = "auto", // "auto", "portrait", "landscape", "sensor"
    val titleBarColor: String = "#0F172A",
    val fullscreen: Boolean = false,
    val hideTitleBar: Boolean = false,
    val allowZoom: Boolean = true,
    val allowLongPress: Boolean = true,
    val loadingUI: Boolean = true,
    val swipeRefresh: Boolean = true,
    val camera: Boolean = false,
    val microphone: Boolean = false,
    val mediaAutoplay: Boolean = false,
    val pcMode: Boolean = false,
    val permissionNotifications: Boolean = false,
    val splashScreen: Boolean = true,
    val splashDurationMs: Int = 1500,
    val splashColor: String = "#0F172A",
    val gitHubRepo: String = "",
    val gitHubBranch: String = "main",
    val lastBuildStatus: String = "NONE", // "NONE", "SUCCESS", "FAILED", "RUNNING"
    val lastBuildTime: Long = 0L,
    val lastBuildLog: String = ""
)

data class ProjectItem(
    val id: String,
    val name: String,
    val packageName: String,
    val mode: String,
    val remoteUrl: String,
    val homepage: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastBuildStatus: String,
    val config: ProjectConfig
)
