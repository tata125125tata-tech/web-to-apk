package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectConfig
import com.example.data.model.ProjectItem
import com.example.data.repository.ProjectRepository
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidSettingsScreen(
    project: ProjectItem,
    projectRepository: ProjectRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var appName by remember { mutableStateOf(project.config.name) }
    var packageName by remember { mutableStateOf(project.config.packageName) }
    var versionName by remember { mutableStateOf(project.config.versionName) }
    var versionCode by remember { mutableIntStateOf(project.config.versionCode) }
    var homepage by remember { mutableStateOf(project.config.homepage) }
    var orientation by remember { mutableStateOf(project.config.orientation) }
    var titleBarColor by remember { mutableStateOf(project.config.titleBarColor) }
    var splashColor by remember { mutableStateOf(project.config.splashColor) }
    var splashDuration by remember { mutableIntStateOf(project.config.splashDurationMs) }

    // Toggles
    var fullscreen by remember { mutableStateOf(project.config.fullscreen) }
    var hideTitleBar by remember { mutableStateOf(project.config.hideTitleBar) }
    var allowZoom by remember { mutableStateOf(project.config.allowZoom) }
    var allowLongPress by remember { mutableStateOf(project.config.allowLongPress) }
    var loadingUI by remember { mutableStateOf(project.config.loadingUI) }
    var swipeRefresh by remember { mutableStateOf(project.config.swipeRefresh) }
    var pcMode by remember { mutableStateOf(project.config.pcMode) }
    var mediaAutoplay by remember { mutableStateOf(project.config.mediaAutoplay) }
    var camera by remember { mutableStateOf(project.config.camera) }
    var microphone by remember { mutableStateOf(project.config.microphone) }
    var notifications by remember { mutableStateOf(project.config.permissionNotifications) }
    var splashScreen by remember { mutableStateOf(project.config.splashScreen) }

    val packageRegex = remember { Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$") }
    val isPackageValid = remember(packageName) { packageRegex.matches(packageName.trim().lowercase()) }

    fun saveSettings() {
        if (!isPackageValid) {
            Toast.makeText(context, "Invalid package name format", Toast.LENGTH_SHORT).show()
            return
        }
        val updated = project.config.copy(
            name = appName.trim(),
            packageName = packageName.trim(),
            versionName = versionName.trim(),
            versionCode = versionCode,
            homepage = homepage.trim(),
            orientation = orientation,
            titleBarColor = titleBarColor.trim(),
            splashColor = splashColor.trim(),
            splashDurationMs = splashDuration,
            fullscreen = fullscreen,
            hideTitleBar = hideTitleBar,
            allowZoom = allowZoom,
            allowLongPress = allowLongPress,
            loadingUI = loadingUI,
            swipeRefresh = swipeRefresh,
            pcMode = pcMode,
            mediaAutoplay = mediaAutoplay,
            camera = camera,
            microphone = microphone,
            permissionNotifications = notifications,
            splashScreen = splashScreen
        )

        coroutineScope.launch {
            projectRepository.updateProjectConfig(updated)
            Toast.makeText(context, "Settings saved to project.json", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android App Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { saveSettings() }, modifier = Modifier.testTag("save_settings_button")) {
                        Icon(Icons.Default.Save, contentDescription = "Save Settings", tint = EmeraldSuccess)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("General App Metadata", fontWeight = FontWeight.Bold, color = TealPrimary)

                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("Application Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = packageName,
                        onValueChange = { packageName = it.trim().lowercase() },
                        label = { Text("Package Name (Unique Application ID)") },
                        isError = !isPackageValid,
                        supportingText = {
                            if (!isPackageValid) Text("Format: com.company.appname", color = RoseError)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = versionName,
                            onValueChange = { versionName = it },
                            label = { Text("Version Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = versionCode.toString(),
                            onValueChange = { versionCode = it.toIntOrNull() ?: versionCode },
                            label = { Text("Version Code") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = homepage,
                        onValueChange = { homepage = it },
                        label = { Text("Homepage Entry (e.g. index.html)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Screen Orientation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Screen Rotation Method", fontWeight = FontWeight.Bold, color = TealPrimary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("auto" to "Auto Rotate", "portrait" to "Portrait", "landscape" to "Landscape", "sensor" to "Sensor").forEach { (valKey, label) ->
                            FilterChip(
                                selected = orientation == valKey,
                                onClick = { orientation = valKey },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Theming & Colors Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Colors & Appearance", fontWeight = FontWeight.Bold, color = TealPrimary)

                    OutlinedTextField(
                        value = titleBarColor,
                        onValueChange = { titleBarColor = it },
                        label = { Text("Title Bar Background Hex (e.g. #0F172A)") },
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(runCatching { Color(android.graphics.Color.parseColor(titleBarColor)) }.getOrDefault(TealPrimary))
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = splashColor,
                        onValueChange = { splashColor = it },
                        label = { Text("Splash Screen Background Hex (e.g. #0F172A)") },
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(runCatching { Color(android.graphics.Color.parseColor(splashColor)) }.getOrDefault(Color(0xFF0F172A)))
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // WebView Features Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("WebView Engine Options", fontWeight = FontWeight.Bold, color = TealPrimary)

                    SettingToggleRow("Fullscreen Mode", "Hides status and navigation bars", fullscreen) { fullscreen = it }
                    SettingToggleRow("Hide Title Bar", "Maximizes web content height", hideTitleBar) { hideTitleBar = it }
                    SettingToggleRow("Allow Zoom", "Pinch-to-zoom support", allowZoom) { allowZoom = it }
                    SettingToggleRow("Allow Long Press", "Context selection & tap-and-hold", allowLongPress) { allowLongPress = it }
                    SettingToggleRow("Show Loading UI", "Display top progress bar during page load", loadingUI) { loadingUI = it }
                    SettingToggleRow("Swipe to Refresh", "Pull down to reload website", swipeRefresh) { swipeRefresh = it }
                    SettingToggleRow("PC Mode (Desktop UA)", "Request desktop website version", pcMode) { pcMode = it }
                    SettingToggleRow("Allow Media Autoplay", "Autoplay video and audio tags", mediaAutoplay) { mediaAutoplay = it }
                    SettingToggleRow("Splash Screen", "Display animated launch screen", splashScreen) { splashScreen = it }
                }
            }

            // Permissions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Android Permissions", fontWeight = FontWeight.Bold, color = TealPrimary)
                    Text("Internet permission is included by default.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    SettingToggleRow("Camera Permission", "WebRTC & file capture (<input type=file>)", camera) { camera = it }
                    SettingToggleRow("Microphone Permission", "Audio recording & WebRTC voice chat", microphone) { microphone = it }
                    SettingToggleRow("Push Notifications", "Allow Web Push & local notifications", notifications) { notifications = it }
                }
            }

            // Save Button
            Button(
                onClick = { saveSettings() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_settings_bottom_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
