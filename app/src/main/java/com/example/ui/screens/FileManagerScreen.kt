package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.model.FileItem
import com.example.data.model.ProjectItem
import com.example.data.model.WebFileType
import com.example.data.repository.FileManager
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch
import java.io.File
import java.util.zip.ZipInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    project: ProjectItem,
    fileManager: FileManager,
    onBack: () -> Unit,
    onOpenFile: (relativePath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentDir by remember { mutableStateOf("") }
    var fileList by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    var itemToRename by remember { mutableStateOf<FileItem?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<FileItem?>(null) }

    fun refreshFiles() {
        coroutineScope.launch {
            fileList = fileManager.listFiles(project.id, currentDir)
        }
    }

    LaunchedEffect(currentDir, project.id) {
        refreshFiles()
    }

    // File Upload launcher
    val fileUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "uploaded_asset_${System.currentTimeMillis()}"
                    val targetRelPath = if (currentDir.isEmpty()) fileName else "$currentDir/$fileName"
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val targetFile = File(fileManager.getWebsiteDir(project.id), targetRelPath)
                        targetFile.parentFile?.mkdirs()
                        targetFile.writeBytes(bytes)
                        refreshFiles()
                        Toast.makeText(context, "Uploaded $fileName", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Website Files", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (currentDir.isEmpty()) "website/" else "website/$currentDir/",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentDir.isNotEmpty()) {
                                currentDir = currentDir.substringBeforeLast('/', "")
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("file_manager_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showNewFileDialog = true }, modifier = Modifier.testTag("new_file_button")) {
                        Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "New File")
                    }
                    IconButton(onClick = { showNewFolderDialog = true }, modifier = Modifier.testTag("new_folder_button")) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
                    }
                    IconButton(onClick = { fileUploadLauncher.launch("*/*") }, modifier = Modifier.testTag("upload_file_button")) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Upload Asset")
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
        ) {
            // Breadcrumbs / Back level bar
            if (currentDir.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { currentDir = currentDir.substringBeforeLast('/', "") },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ".. (Go Up to parent folder)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }

            if (fileList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("This folder is empty", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Tap '+' in the top bar to create HTML, CSS, or JS files.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fileList, key = { it.path }) { item ->
                        var itemMenuExpanded by remember { mutableStateOf(false) }

                        val iconColor = when (item.fileType) {
                            WebFileType.HTML -> Color(0xFFEF4444)
                            WebFileType.CSS -> Color(0xFF38BDF8)
                            WebFileType.JAVASCRIPT -> Color(0xFFFBBF24)
                            WebFileType.JSON -> Color(0xFF10B981)
                            WebFileType.IMAGE -> Color(0xFFA855F7)
                            else -> if (item.isDirectory) TealPrimary else Color.Gray
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (item.isDirectory) {
                                        currentDir = item.path
                                    } else {
                                        onOpenFile(item.path)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(iconColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (item.isDirectory) Icons.Default.Folder
                                        else if (item.fileType == WebFileType.IMAGE) Icons.Default.Image
                                        else Icons.Default.Description,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (item.isDirectory) "Directory" else "${item.size} bytes · ${item.extension.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box {
                                    IconButton(onClick = { itemMenuExpanded = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                                    }

                                    DropdownMenu(
                                        expanded = itemMenuExpanded,
                                        onDismissRequest = { itemMenuExpanded = false }
                                    ) {
                                        if (!item.isDirectory) {
                                            DropdownMenuItem(
                                                text = { Text("Edit in Code Editor") },
                                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                                onClick = {
                                                    itemMenuExpanded = false
                                                    onOpenFile(item.path)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Duplicate") },
                                                leadingIcon = { Icon(Icons.Outlined.FileCopy, contentDescription = null) },
                                                onClick = {
                                                    itemMenuExpanded = false
                                                    coroutineScope.launch {
                                                        fileManager.duplicateItem(project.id, item.path)
                                                        refreshFiles()
                                                    }
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("Rename") },
                                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                            onClick = {
                                                itemMenuExpanded = false
                                                itemToRename = item
                                                renameInput = item.name
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                            onClick = {
                                                itemMenuExpanded = false
                                                itemToDelete = item
                                            }
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

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New File") },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("File Name (e.g. script.js, style.css)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("new_file_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFileName.trim()
                        if (name.isNotEmpty()) {
                            val rel = if (currentDir.isEmpty()) name else "$currentDir/$name"
                            coroutineScope.launch {
                                val success = fileManager.createFile(project.id, rel, "")
                                if (success) {
                                    showNewFileDialog = false
                                    newFileName = ""
                                    refreshFiles()
                                    onOpenFile(rel)
                                } else {
                                    Toast.makeText(context, "File already exists or invalid", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_new_file_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // New Folder Dialog
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name (e.g. css, js, assets)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("new_folder_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFolderName.trim()
                        if (name.isNotEmpty()) {
                            val rel = if (currentDir.isEmpty()) name else "$currentDir/$name"
                            coroutineScope.launch {
                                fileManager.createFolder(project.id, rel)
                                showNewFolderDialog = false
                                newFolderName = ""
                                refreshFiles()
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_new_folder_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Dialog
    if (itemToRename != null) {
        AlertDialog(
            onDismissRequest = { itemToRename = null },
            title = { Text("Rename ${itemToRename!!.name}") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("file_rename_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = itemToRename ?: return@Button
                        if (renameInput.isNotBlank() && renameInput != item.name) {
                            coroutineScope.launch {
                                val ok = fileManager.renameItem(project.id, item.path, renameInput.trim())
                                if (ok) {
                                    itemToRename = null
                                    refreshFiles()
                                } else {
                                    Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete ${itemToDelete!!.name}?") },
            text = { Text("This will permanently remove '${itemToDelete!!.name}'.") },
            confirmButton = {
                Button(
                    onClick = {
                        val item = itemToDelete ?: return@Button
                        coroutineScope.launch {
                            fileManager.deleteItem(project.id, item.path)
                            itemToDelete = null
                            refreshFiles()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
