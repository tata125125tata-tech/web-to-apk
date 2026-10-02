package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildMode
import com.example.data.model.BuildType
import com.example.data.model.GitHubSettings
import com.example.data.model.ProjectItem
import com.example.data.model.SigningSettings
import com.example.data.repository.AndroidGenerator
import com.example.data.repository.BuildEngine
import com.example.ui.components.TerminalView
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildScreen(
    project: ProjectItem,
    buildEngine: BuildEngine,
    androidGenerator: AndroidGenerator,
    onBack: () -> Unit,
    onOpenGitSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val logs by buildEngine.logs.collectAsState()
    val isBuilding by buildEngine.isBuilding.collectAsState()
    val currentStatus by buildEngine.currentStatus.collectAsState()

    var selectedMode by remember { mutableStateOf(BuildMode.REMOTE) }
    var selectedBuildType by remember { mutableStateOf(BuildType.DEBUG_APK) }

    // Signing settings
    var showSigningDialog by remember { mutableStateOf(false) }
    var useReleaseSigning by remember { mutableStateOf(false) }
    var keystorePath by remember { mutableStateOf("") }
    var keystorePassword by remember { mutableStateOf("") }
    var keyAlias by remember { mutableStateOf("") }
    var keyPassword by remember { mutableStateOf("") }

    val hasLocalCompiler = remember { buildEngine.isLocalCompilerAvailable() }

    fun triggerBuild(type: BuildType) {
        selectedBuildType = type
        coroutineScope.launch {
            val signing = SigningSettings(
                useReleaseSigning = useReleaseSigning,
                keystorePath = keystorePath,
                keystorePassword = keystorePassword,
                keyAlias = keyAlias,
                keyPassword = keyPassword
            )
            val gitHub = GitHubSettings(
                personalAccessToken = project.config.gitHubRepo, // repo & token configured
                owner = project.config.gitHubRepo.substringBefore('/'),
                repoName = project.config.gitHubRepo.substringAfter('/'),
                branch = project.config.gitHubBranch
            )
            buildEngine.executeBuild(
                config = project.config,
                buildType = type,
                signing = signing,
                gitHub = gitHub,
                targetMode = selectedMode
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build & Compilation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("build_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenGitSettings) {
                        Icon(Icons.Default.Share, contentDescription = "Git CI Setup")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Selector: LOCAL vs REMOTE
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
                        Text("Build Engine Mode", fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedMode == BuildMode.REMOTE) TealPrimary.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = selectedMode.name,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedMode == BuildMode.REMOTE) TealPrimary else EmeraldSuccess,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = selectedMode == BuildMode.REMOTE,
                            onClick = { selectedMode = BuildMode.REMOTE },
                            label = { Text("Remote (GitHub CI)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedMode == BuildMode.LOCAL,
                            onClick = { selectedMode = BuildMode.LOCAL },
                            label = { Text("Local Generator / ZIP") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text(
                        text = if (selectedMode == BuildMode.REMOTE)
                            "Builds real APK & AAB in the cloud with GitHub Actions. Free, fast, and does not require on-device SDK."
                        else if (hasLocalCompiler)
                            "Local Android Gradle compiler detected on host."
                        else
                            "Standard Android devices lack local Gradle SDK daemons. Web2APK generates the complete compilable Android Studio project and exports as ZIP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Build Actions Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { triggerBuild(BuildType.DEBUG_APK) },
                    enabled = !isBuilding,
                    modifier = Modifier.weight(1f).testTag("build_debug_apk_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Debug APK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { triggerBuild(BuildType.RELEASE_APK) },
                    enabled = !isBuilding,
                    modifier = Modifier.weight(1f).testTag("build_release_apk_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Release APK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { triggerBuild(BuildType.BUNDLE_AAB) },
                    enabled = !isBuilding,
                    modifier = Modifier.weight(1f).testTag("build_aab_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Build AAB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Export Project ZIP & Signing buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val zip = androidGenerator.exportGeneratedAndroidProjectZip(project.config)
                                Toast.makeText(context, "Exported Gradle Project: ${zip.name}", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("export_gradle_zip_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Gradle ZIP", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { showSigningDialog = !showSigningDialog },
                    modifier = Modifier.weight(1f).testTag("signing_config_toggle"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (useReleaseSigning) "Release Key" else "Debug Key", fontSize = 12.sp)
                }
            }

            // Signing Dialog / Expandable
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
                            Text("APK Signing Configuration", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            Text("Using default Android debug.keystore profile.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Terminal View
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                TerminalView(
                    logs = logs,
                    isBuilding = isBuilding,
                    onClearLogs = { buildEngine.clearLogs() },
                    onRetryBuild = { triggerBuild(selectedBuildType) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
