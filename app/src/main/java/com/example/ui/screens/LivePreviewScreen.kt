package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ProjectItem
import com.example.data.repository.FileManager
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TealPrimary
import java.io.File

data class ConsoleLogItem(
    val level: ConsoleMessage.MessageLevel,
    val message: String,
    val lineNumber: Int,
    val sourceId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LivePreviewScreen(
    project: ProjectItem,
    fileManager: FileManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDesktopMode by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showConsole by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    val consoleLogs = remember { mutableStateListOf<ConsoleLogItem>() }

    // Check entry point
    val isLocal = project.mode == "local"
    val entryFile = remember(project) {
        if (isLocal) {
            File(fileManager.getWebsiteDir(project.id), project.homepage)
        } else null
    }
    val isEntryMissing = isLocal && (entryFile == null || !entryFile.exists())

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Live Preview", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isLocal) "local: ${project.homepage}" else project.remoteUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("preview_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier.testTag("preview_reload_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload")
                        }

                        IconButton(
                            onClick = {
                                isDesktopMode = !isDesktopMode
                                webViewInstance?.let { wv ->
                                    val settings = wv.settings
                                    if (isDesktopMode) {
                                        settings.userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                                    } else {
                                        settings.userAgentString = null
                                    }
                                    wv.reload()
                                }
                            },
                            modifier = Modifier.testTag("preview_desktop_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDesktopMode) Icons.Default.Computer else Icons.Default.PhoneAndroid,
                                contentDescription = "Toggle Desktop/Mobile viewport",
                                tint = if (isDesktopMode) TealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { showConsole = !showConsole },
                            modifier = Modifier.testTag("preview_console_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Toggle JavaScript Console",
                                tint = if (consoleLogs.any { it.level == ConsoleMessage.MessageLevel.ERROR }) RoseError else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { isFullscreen = !isFullscreen },
                            modifier = Modifier.testTag("preview_fullscreen_toggle")
                        ) {
                            Icon(
                                imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(if (isFullscreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
        ) {
            if (isLoading && progress < 100) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = TealPrimary
                )
            }

            if (isEntryMissing) {
                // Missing index.html Banner (Requirement 7: If index.html is missing, clearly display an error)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = RoseError,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Missing Homepage Entry: ${project.homepage}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RoseError
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "The preview engine could not locate '${project.homepage}' in your project's website folder.\n\nPlease go to Website Files and create '${project.homepage}', or change your Homepage in Android App Settings.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(onClick = onBack) {
                                Text("Return to Workspace")
                            }
                        }
                    }
                }
            } else {
                // Real WebView
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                @SuppressLint("SetJavaScriptEnabled")
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    allowFileAccess = true
                                    allowContentAccess = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    cacheMode = WebSettings.LOAD_NO_CACHE
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                    }

                                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                        isLoading = false
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        progress = newProgress
                                        if (newProgress >= 100) isLoading = false
                                    }

                                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                        if (consoleMessage != null) {
                                            consoleLogs.add(
                                                ConsoleLogItem(
                                                    level = consoleMessage.messageLevel(),
                                                    message = consoleMessage.message(),
                                                    lineNumber = consoleMessage.lineNumber(),
                                                    sourceId = consoleMessage.sourceId()
                                                )
                                            )
                                        }
                                        return true
                                    }
                                }

                                webViewInstance = this

                                val url = if (isLocal) {
                                    "file://${entryFile!!.absolutePath}"
                                } else {
                                    project.remoteUrl
                                }
                                loadUrl(url)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Exit Fullscreen floating badge
                    if (isFullscreen) {
                        Surface(
                            onClick = { isFullscreen = false },
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xCC000000),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FullscreenExit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exit Fullscreen", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // In-app JavaScript Console Drawer
                AnimatedVisibility(visible = showConsole) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        color = Color(0xFF060911),
                        tonalElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "JavaScript Console (${consoleLogs.size})",
                                    color = TealPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Clear",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.clickable { consoleLogs.clear() }
                                )
                            }

                            if (consoleLogs.isEmpty()) {
                                Text(
                                    text = "No console logs or errors recorded.",
                                    color = Color.DarkGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(consoleLogs) { log ->
                                        val color = when (log.level) {
                                            ConsoleMessage.MessageLevel.ERROR -> RoseError
                                            ConsoleMessage.MessageLevel.WARNING -> AmberWarning
                                            else -> Color(0xFFE2E8F0)
                                        }
                                        Text(
                                            text = "[${log.level}] ${log.message} (${log.sourceId.substringAfterLast('/')}:${log.lineNumber})",
                                            color = color,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
