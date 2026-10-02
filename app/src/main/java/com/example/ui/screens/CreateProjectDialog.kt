package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectTemplate
import com.example.data.model.ProjectTemplates
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TealPrimary

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, packageName: String, template: ProjectTemplate, mode: String, remoteUrl: String) -> Unit
) {
    var projectName by remember { mutableStateOf("My Web App") }
    var packageName by remember { mutableStateOf("com.example.mywebapp") }
    var selectedTemplate by remember { mutableStateOf(ProjectTemplates.templates[0]) }
    var remoteUrl by remember { mutableStateOf("https://example.com") }

    val packageRegex = remember { Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$") }
    val isPackageValid = remember(packageName) { packageRegex.matches(packageName.trim().lowercase()) }
    val isNameValid = remember(projectName) { projectName.trim().isNotEmpty() }
    val isUrlValid = remember(remoteUrl, selectedTemplate) {
        !selectedTemplate.isRemoteUrl || (remoteUrl.startsWith("http://") || remoteUrl.startsWith("https://"))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New Project",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Project Name
                OutlinedTextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                        // Auto suggest package name if not manually modified
                        val slug = it.lowercase().replace(Regex("[^a-z0-9]"), "")
                        if (slug.isNotEmpty()) {
                            packageName = "com.example.$slug"
                        }
                    },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Package Name
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it.trim().lowercase() },
                    label = { Text("Package Name (Android ID)") },
                    supportingText = {
                        if (!isPackageValid && packageName.isNotEmpty()) {
                            Text(
                                text = "Invalid package name. Must be e.g. com.example.myapp",
                                color = RoseError
                            )
                        } else {
                            Text(text = "Unique identifier on Android device", color = EmeraldSuccess)
                        }
                    },
                    isError = !isPackageValid && packageName.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("package_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Website Source Template:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                // Template Cards
                ProjectTemplates.templates.forEach { tmpl ->
                    val isSelected = selectedTemplate.id == tmpl.id
                    val icon: ImageVector = when (tmpl.iconName) {
                        "dashboard" -> Icons.Default.Dashboard
                        "sports_esports" -> Icons.Default.SportsEsports
                        "language" -> Icons.Default.Language
                        else -> Icons.Default.Code
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTemplate = tmpl },
                        color = if (isSelected) TealPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tmpl.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tmpl.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // If Remote URL template selected, show URL input
                if (selectedTemplate.isRemoteUrl) {
                    OutlinedTextField(
                        value = remoteUrl,
                        onValueChange = { remoteUrl = it.trim() },
                        label = { Text("Remote Website URL") },
                        placeholder = { Text("https://example.com") },
                        isError = !isUrlValid,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("remote_url_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mode = if (selectedTemplate.isRemoteUrl) "remote" else "local"
                    onCreate(projectName.trim(), packageName.trim(), selectedTemplate, mode, remoteUrl.trim())
                },
                enabled = isNameValid && isPackageValid && isUrlValid,
                modifier = Modifier.testTag("submit_create_project")
            ) {
                Text("Create Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
