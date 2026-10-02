package com.example.data.repository

import android.content.Context
import com.example.data.model.ProjectConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class AndroidGenerator(private val context: Context, private val fileManager: FileManager) {

    fun getGeneratedProjectDir(projectId: String): File {
        val dir = File(fileManager.getProjectDir(projectId), "generated_android_project")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun generateAndroidProject(config: ProjectConfig): File = withContext(Dispatchers.IO) {
        val projectDir = getGeneratedProjectDir(config.id)
        if (projectDir.exists()) projectDir.deleteRecursively()
        projectDir.mkdirs()

        // 1. Root settings.gradle.kts
        File(projectDir, "settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }
            rootProject.name = "${config.name.replace("\"", "\\\"")}"
            include(":app")
            """.trimIndent()
        )

        // 2. Root build.gradle.kts
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.android.application") version "8.7.3" apply false
                id("org.jetbrains.kotlin.android") version "2.0.21" apply false
            }
            """.trimIndent()
        )

        // 3. Root gradle.properties
        File(projectDir, "gradle.properties").writeText(
            """
            org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
            android.useAndroidX=true
            android.nonTransitiveRClass=true
            """.trimIndent()
        )

        // 4. GitHub Actions Workflow (.github/workflows/build-apk.yml)
        val workflowDir = File(projectDir, ".github/workflows")
        workflowDir.mkdirs()
        File(workflowDir, "build-apk.yml").writeText(generateGitHubWorkflow(config))

        // 5. App module directory
        val appDir = File(projectDir, "app")
        appDir.mkdirs()

        // App build.gradle.kts
        File(appDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.android.application")
                id("org.jetbrains.kotlin.android")
            }

            android {
                namespace = "${config.packageName}"
                compileSdk = 35

                defaultConfig {
                    applicationId = "${config.packageName}"
                    minSdk = 24
                    targetSdk = 35
                    versionCode = ${config.versionCode}
                    versionName = "${config.versionName}"

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                    debug {
                        applicationIdSuffix = ".debug"
                        isDebuggable = true
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                kotlinOptions {
                    jvmTarget = "17"
                }

                buildFeatures {
                    viewBinding = true
                }
            }

            dependencies {
                implementation("androidx.core:core-ktx:1.15.0")
                implementation("androidx.appcompat:appcompat:1.7.0")
                implementation("com.google.android.material:material:1.12.0")
                implementation("androidx.webkit:webkit:1.12.1")
                implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
                implementation("androidx.constraintlayout:constraintlayout:2.2.0")
            }
            """.trimIndent()
        )

        // 6. Manifest
        val mainDir = File(appDir, "src/main")
        mainDir.mkdirs()
        File(mainDir, "AndroidManifest.xml").writeText(generateManifest(config))

        // 7. Assets (Website files)
        val assetsDir = File(mainDir, "assets/website")
        assetsDir.mkdirs()
        val srcWebsiteDir = fileManager.getWebsiteDir(config.id)
        if (srcWebsiteDir.exists()) {
            srcWebsiteDir.copyRecursively(assetsDir, overwrite = true)
        }

        // 8. Res files
        val resDir = File(mainDir, "res")
        val valuesDir = File(resDir, "values")
        valuesDir.mkdirs()

        File(valuesDir, "strings.xml").writeText(
            """
            <resources>
                <string name="app_name">${config.name}</string>
            </resources>
            """.trimIndent()
        )

        File(valuesDir, "colors.xml").writeText(
            """
            <resources>
                <color name="primary_color">${config.titleBarColor}</color>
                <color name="splash_color">${config.splashColor}</color>
            </resources>
            """.trimIndent()
        )

        File(valuesDir, "themes.xml").writeText(
            """
            <resources>
                <style name="Theme.Web2App" parent="Theme.Material3.DayNight.NoActionBar">
                    <item name="colorPrimary">@color/primary_color</item>
                    <item name="android:statusBarColor">@color/primary_color</item>
                </style>
            </resources>
            """.trimIndent()
        )

        // Layout XML
        val layoutDir = File(resDir, "layout")
        layoutDir.mkdirs()
        File(layoutDir, "activity_main.xml").writeText(generateLayoutXml(config))

        // 9. Kotlin source code (MainActivity.kt)
        val packagePath = config.packageName.replace('.', '/')
        val javaDir = File(mainDir, "java/$packagePath")
        javaDir.mkdirs()
        File(javaDir, "MainActivity.kt").writeText(generateMainActivityKotlin(config))

        projectDir
    }

    private fun generateManifest(config: ProjectConfig): String {
        val permissions = mutableListOf(
            "<uses-permission android:name=\"android.permission.INTERNET\" />",
            "<uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />"
        )
        if (config.camera) {
            permissions.add("<uses-permission android:name=\"android.permission.CAMERA\" />")
            permissions.add("<uses-feature android:name=\"android.hardware.camera\" android:required=\"false\" />")
        }
        if (config.microphone) {
            permissions.add("<uses-permission android:name=\"android.permission.RECORD_AUDIO\" />")
            permissions.add("<uses-permission android:name=\"android.permission.MODIFY_AUDIO_SETTINGS\" />")
        }
        if (config.permissionNotifications) {
            permissions.add("<uses-permission android:name=\"android.permission.POST_NOTIFICATIONS\" />")
        }

        val orientationAttr = when (config.orientation) {
            "portrait" -> "android:screenOrientation=\"portrait\""
            "landscape" -> "android:screenOrientation=\"landscape\""
            "sensor" -> "android:screenOrientation=\"sensor\""
            else -> "android:screenOrientation=\"unspecified\""
        }

        return """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                ${permissions.joinToString("\n    ")}

                <application
                    android:allowBackup="true"
                    android:icon="@mipmap/ic_launcher"
                    android:label="@string/app_name"
                    android:roundIcon="@mipmap/ic_launcher_round"
                    android:supportsRtl="true"
                    android:usesCleartextTraffic="true"
                    android:theme="@style/Theme.Web2App">
                    <activity
                        android:name=".MainActivity"
                        android:exported="true"
                        $orientationAttr
                        android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
                        android:windowSoftInputMode="adjustResize"
                        android:theme="@style/Theme.Web2App">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent()
    }

    private fun generateLayoutXml(config: ProjectConfig): String {
        val showTitleBar = !config.hideTitleBar && !config.fullscreen
        val titleBarXml = if (showTitleBar) {
            """
            <com.google.android.material.appbar.MaterialToolbar
                android:id="@+id/toolbar"
                android:layout_width="match_parent"
                android:layout_height="?attr/actionBarSize"
                android:background="@color/primary_color"
                app:title="${config.name}"
                app:titleTextColor="#FFFFFF" />
            """.trimIndent()
        } else ""

        val swipeRefreshStart = if (config.swipeRefresh) {
            """<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
                android:id="@+id/swipeRefreshLayout"
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1">"""
        } else """<FrameLayout
                android:layout_width="match_parent"
                android:layout_height="0dp"
                android:layout_weight="1">"""

        val swipeRefreshEnd = if (config.swipeRefresh) {
            "</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>"
        } else "</FrameLayout>"

        val splashXml = if (config.splashScreen) {
            """
            <FrameLayout
                android:id="@+id/splashContainer"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@color/splash_color"
                android:elevation="10dp">
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="center"
                    android:text="${config.name}"
                    android:textColor="#FFFFFF"
                    android:textSize="24sp"
                    android:textStyle="bold" />
            </FrameLayout>
            """.trimIndent()
        } else ""

        return """
            <?xml version="1.0" encoding="utf-8"?>
            <FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="#000000">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:orientation="vertical">

                    $titleBarXml

                    <com.google.android.material.progressindicator.LinearProgressIndicator
                        android:id="@+id/progressBar"
                        android:layout_width="match_parent"
                        android:layout_height="3dp"
                        android:indeterminate="false"
                        android:max="100"
                        android:visibility="gone" />

                    $swipeRefreshStart
                        <WebView
                            android:id="@+id/webView"
                            android:layout_width="match_parent"
                            android:layout_height="match_parent" />
                    $swipeRefreshEnd
                </LinearLayout>

                $splashXml
            </FrameLayout>
        """.trimIndent()
    }

    private fun generateMainActivityKotlin(config: ProjectConfig): String {
        val isRemote = config.mode == "remote"
        val targetUrl = if (isRemote) {
            "\"${config.remoteUrl}\""
        } else {
            "\"file:///android_asset/website/${config.homepage}\""
        }

        val pcModeDesktopUA = if (config.pcMode) {
            """
            settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            """.trimIndent()
        } else ""

        val fullscreenCode = if (config.fullscreen) {
            """
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
            """.trimIndent()
        } else ""

        val mediaAutoplayCode = if (config.mediaAutoplay) {
            "settings.mediaPlaybackRequiresUserGesture = false"
        } else {
            "settings.mediaPlaybackRequiresUserGesture = true"
        }

        val swipeRefreshBinding = if (config.swipeRefresh) {
            """
            val swipeRefreshLayout = findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            swipeRefreshLayout?.setOnRefreshListener {
                webView.reload()
            }
            """.trimIndent()
        } else ""

        val swipeRefreshStop = if (config.swipeRefresh) {
            "findViewById<androidx.swiperefreshlayout.widget.SwipeRefreshLayout>(R.id.swipeRefreshLayout)?.isRefreshing = false"
        } else ""

        val splashHandler = if (config.splashScreen) {
            """
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val splash = findViewById<android.view.View>(R.id.splashContainer)
                splash?.animate()?.alpha(0f)?.setDuration(400)?.withEndAction {
                    splash.visibility = android.view.View.GONE
                }
            }, ${config.splashDurationMs}L)
            """.trimIndent()
        } else ""

        val longPressCode = if (!config.allowLongPress) {
            """
            webView.setOnLongClickListener { true }
            webView.isHapticFeedbackEnabled = false
            """.trimIndent()
        } else ""

        return """
            package ${config.packageName}

            import android.annotation.SuppressLint
            import android.app.Activity
            import android.content.Intent
            import android.graphics.Bitmap
            import android.net.Uri
            import android.os.Bundle
            import android.view.View
            import android.webkit.CookieManager
            import android.webkit.ValueCallback
            import android.webkit.WebChromeClient
            import android.webkit.WebResourceError
            import android.webkit.WebResourceRequest
            import android.webkit.WebSettings
            import android.webkit.WebView
            import android.webkit.WebViewClient
            import android.widget.ProgressBar
            import androidx.activity.OnBackPressedCallback
            import androidx.appcompat.app.AppCompatActivity

            class MainActivity : AppCompatActivity() {

                private lateinit var webView: WebView
                private var progressBar: ProgressBar? = null
                private var filePathCallback: ValueCallback<Array<Uri>>? = null

                companion object {
                    private const val FILE_CHOOSER_REQUEST_CODE = 1001
                }

                @SuppressLint("SetJavaScriptEnabled")
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    $fullscreenCode
                    setContentView(R.layout.activity_main)

                    webView = findViewById(R.id.webView)
                    progressBar = findViewById(R.id.progressBar)

                    setupWebView()
                    $swipeRefreshBinding
                    $splashHandler
                    $longPressCode

                    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (webView.canGoBack()) {
                                webView.goBack()
                            } else {
                                isEnabled = false
                                onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    })

                    webView.loadUrl($targetUrl)
                }

                @SuppressLint("SetJavaScriptEnabled")
                private fun setupWebView() {
                    val settings = webView.settings
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.loadsImagesAutomatically = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.setSupportZoom(${config.allowZoom})
                    settings.builtInZoomControls = ${config.allowZoom}
                    settings.displayZoomControls = false
                    $mediaAutoplayCode
                    $pcModeDesktopUA

                    // Cookie support
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(webView, true)

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            if (${config.loadingUI}) {
                                progressBar?.visibility = View.VISIBLE
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            progressBar?.visibility = View.GONE
                            $swipeRefreshStop
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            progressBar?.visibility = View.GONE
                            $swipeRefreshStop
                        }
                    }

                    webView.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            progressBar?.progress = newProgress
                            if (newProgress >= 100) {
                                progressBar?.visibility = View.GONE
                            }
                        }

                        override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                            request?.grant(request.resources)
                        }

                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            this@MainActivity.filePathCallback?.onReceiveValue(null)
                            this@MainActivity.filePathCallback = filePathCallback

                            val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "*/*"
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                            try {
                                startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                            } catch (e: Exception) {
                                this@MainActivity.filePathCallback = null
                                return false
                            }
                            return true
                        }
                    }

                    // Download Listener
                    webView.setDownloadListener { url, _, _, _, _ ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                }

                override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                    super.onActivityResult(requestCode, resultCode, data)
                    if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
                        val results: Array<Uri>? = if (resultCode == Activity.RESULT_OK && data != null) {
                            val dataUri = data.data
                            if (dataUri != null) arrayOf(dataUri) else null
                        } else null
                        filePathCallback?.onReceiveValue(results)
                        filePathCallback = null
                    }
                }
            }
        """.trimIndent()
    }

    fun generateGitHubWorkflow(config: ProjectConfig): String {
        return """
            name: Build Android APK & AAB

            on:
              push:
                branches: [ main, master ]
              workflow_dispatch:
                inputs:
                  build_type:
                    description: 'Build Type (debug / release / bundle)'
                    required: true
                    default: 'debug'

            jobs:
              build:
                runs-on: ubuntu-latest

                steps:
                - name: Checkout Code
                  uses: actions/checkout@v4

                - name: Set up JDK 17
                  uses: actions/setup-java@v4
                  with:
                    java-version: '17'
                    distribution: 'temurin'
                    cache: gradle

                - name: Grant Execute Permission for Gradlew
                  run: chmod +x gradlew || true

                - name: Build Debug APK
                  if: "github.event.inputs.build_type == 'debug' || github.event_name == 'push'"
                  run: |
                    gradle :app:assembleDebug --stacktrace || ./gradlew :app:assembleDebug --stacktrace

                - name: Build Release APK
                  if: "github.event.inputs.build_type == 'release'"
                  run: |
                    gradle :app:assembleRelease --stacktrace || ./gradlew :app:assembleRelease --stacktrace

                - name: Build Android App Bundle (AAB)
                  if: "github.event.inputs.build_type == 'bundle'"
                  run: |
                    gradle :app:bundleRelease --stacktrace || ./gradlew :app:bundleRelease --stacktrace

                - name: Upload Debug APK
                  uses: actions/upload-artifact@v4
                  with:
                    name: app-debug-apk
                    path: app/build/outputs/apk/debug/*.apk
                    if-no-files-found: warn

                - name: Upload Release Artifacts
                  uses: actions/upload-artifact@v4
                  with:
                    name: app-release-artifacts
                    path: |
                      app/build/outputs/apk/release/*.apk
                      app/build/outputs/bundle/release/*.aab
                    if-no-files-found: warn
        """.trimIndent()
    }

    suspend fun exportGeneratedAndroidProjectZip(config: ProjectConfig): File = withContext(Dispatchers.IO) {
        val projectDir = generateAndroidProject(config)
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val zipFile = File(exportDir, "${config.packageName}_android_source.zip")
        if (zipFile.exists()) zipFile.delete()

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            projectDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relPath = file.relativeTo(projectDir).path.replace('\\', '/')
                    val entry = ZipEntry(relPath)
                    zos.putNextEntry(entry)
                    FileInputStream(file).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
        zipFile
    }
}
