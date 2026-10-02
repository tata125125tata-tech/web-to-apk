package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildOutputInfo
import com.example.data.model.BuildType
import com.example.data.model.ProjectItem
import com.example.data.model.SigningSettings
import com.example.data.repository.AndroidGenerator
import com.example.data.repository.ApkInstaller
import com.example.data.repository.LocalBuildEngine
import com.example.ui.components.TerminalView
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildScreen(
    project: ProjectItem,
    localBuildEngine: LocalBuildEngine,
    androidGenerator: AndroidGenerator,
    onBack: () -> Unit,
    onOpenEnvironmentDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val logs by localBuildEngine.logs.collectAsState()
    val isBuilding by localBuildEngine.isBuilding.collectAsState()
    val currentStatus by localBuildEngine.currentStatus.collectAsState()
    val lastOutput by localBuildEngine.lastOutput.collectAsState()

    var selectedBuildType by remember { mutableStateOf(BuildType.DEBUG_APK) }

    // Signing settings
    var showSigningDialog by remember { mutableStateOf(false) }
    var useReleaseSigning by remember { mutableStateOf(false) }
    var keystorePath by remember { mutableStateOf("") }
    var keystorePassword by remember { mutableStateOf("") }
    var keyAlias by remember { mutableStateOf("") }
    var keyPassword by remember { mutableStateOf("") }

    val isOnline = remember { localBuildEngine.isOnline() }
    val freeStorage = remember { localBuildEngine.getAvailableStorageMb() }

    fun triggerLocalBuild(type: BuildType) {
        selectedBuildType = type
        coroutineScope.launch {
            val signing = SigningSettings(
                useReleaseSigning = useReleaseSigning,
                keystorePath = keystorePath.trim(),
                keystorePassword = keystorePassword.trim(),
                keyAlias = keyAlias.trim(),
                keyPassword = keyPassword.trim()
            )
            val output = localBuildEngine.executeLocalBuild(
                config = project.config,
                buildType = type,
                signing = signing
            )
            if (output != null) {
                Toast.makeText(context, "Local Build Succeeded! Ready to install.", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Local Build Engine", fontWeight = FontWeight.Bold)
                        Text(
                            text = "On-Device Android Compilation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("build_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenEnvironmentDiagnostics, modifier = Modifier.testTag("open_env_button")) {
                        Icon(Icons.Default.Build, contentDescription = "Build Environment Diagnostics", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Environment & Status Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Primary: LOCAL ON-DEVICE BUILD", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Online / Offline Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isOnline) EmeraldSuccess.copy(alpha = 0.2f) else AmberWarning.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = if (isOnline) EmeraldSuccess else AmberWarning,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOnline) "ONLINE" else "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnline) EmeraldSuccess else AmberWarning
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Storage: ${freeStorage} MB free · App-managed outputs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Diagnostics →",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.clickable { onOpenEnvironmentDiagnostics() }
                        )
                    }
                }
            }

            // Primary Build Buttons Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { triggerLocalBuild(BuildType.DEBUG_APK) },
                    enabled = !isBuilding,
                    modifier = Modifier.weight(1.2f).height(48.dp).testTag("build_debug_apk_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isBuilding && selectedBuildType == BuildType.DEBUG_APK) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Build Debug APK", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = { triggerLocalBuild(BuildType.RELEASE_APK) },
                    enabled = !isBuilding,
                    modifier = Modifier.weight(1f).height(48.dp).testTag("build_release_apk_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Release APK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { triggerLocalBuild(BuildType.BUNDLE_AAB) },
                    enabled = !isBuilding,
                    modifier = Modifier.weight(0.9f).height(48.dp).testTag("build_aab_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("AAB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Signing & Export Quick Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { showSigningDialog = !showSigningDialog },
                    modifier = Modifier.weight(1f).testTag("signing_config_toggle"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (useReleaseSigning) "Release Key" else "Debug Key", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            val zip = androidGenerator.exportGeneratedAndroidProjectZip(project.config)
                            ApkInstaller.shareProjectZip(context, zip)
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("export_project_zip_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Gradle ZIP", fontSize = 12.sp)
                }
            }

            // Signing Dialog / Expandable Card
            if (showSigningDialog) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("APK Signing Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Switch(checked = useReleaseSigning, onCheckedChange = { useReleaseSigning = it })
                        }
                        if (useReleaseSigning) {
                            OutlinedTextField(
                                value = keystorePath,
                                onValueChange = { keystorePath = it },
                                label = { Text("Keystore Path (e.g. /sdcard/release.jks)") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = keyAlias,
                                    onValueChange = { keyAlias = it },
                                    label = { Text("Key Alias") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = keystorePassword,
                                    onValueChange = { keystorePassword = it },
                                    label = { Text("Keystore Pass") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Text("Using standard Android debug keystore profile.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Successful Build Output Card (Section 11)
            AnimatedVisibility(visible = lastOutput != null) {
                lastOutput?.let { output ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("build_output_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Build Successful!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldSuccess)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("• File: ${output.apkName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("• Size: ${output.apkSizeFormatted} (${output.apkSizeBytes} bytes)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("• Version: ${output.versionName} | Build Time: ${output.buildDurationMs / 1000.0}s", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { ApkInstaller.installApk(context, output.apkFile) },
                                    modifier = Modifier.weight(1.2f).height(44.dp).testTag("install_apk_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Install APK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { ApkInstaller.shareApk(context, output.apkFile) },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("share_apk_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Real Terminal Log Stream (Sections 10 & 13)
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                TerminalView(
                    logs = logs,
                    isBuilding = isBuilding,
                    onClearLogs = { localBuildEngine.clearLogs() },
                    onRetryBuild = { triggerLocalBuild(selectedBuildType) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
