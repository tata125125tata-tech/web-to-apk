package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileItem
import com.example.data.model.ProjectItem
import com.example.data.repository.AiResponse
import com.example.data.repository.FileManager
import com.example.data.repository.GeminiAssistant
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.VioletAi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    project: ProjectItem,
    geminiAssistant: GeminiAssistant,
    fileManager: FileManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    var fileMenuExpanded by remember { mutableStateOf(false) }

    var selectedTask by remember { mutableStateOf("fix") }
    var userPrompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var aiResult by remember { mutableStateOf<AiResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(project.id) {
        val list = fileManager.listFiles(project.id, "")
        val editable = list.filter { !it.isDirectory }
        files = editable
        selectedFile = editable.find { it.name == "index.html" } ?: editable.firstOrNull()
    }

    val quickPrompts = listOf(
        "Make this layout responsive for mobile phones",
        "Add a dark/light mode toggle in JavaScript",
        "Fix syntax and layout bugs in this file",
        "Add touch gesture support and button animations",
        "Create an interactive card component"
    )

    fun executeAiRequest() {
        if (selectedFile == null) {
            Toast.makeText(context, "Please select a target file first", Toast.LENGTH_SHORT).show()
            return
        }
        val prompt = userPrompt.ifBlank {
            when (selectedTask) {
                "explain" -> "Explain the structure, functions, and layout of this file."
                "fix" -> "Check for bugs, syntax errors, and improvements in this file."
                "generate" -> "Generate modern mobile component additions for this file."
                else -> "Analyze build and runtime errors."
            }
        }

        coroutineScope.launch {
            isGenerating = true
            errorMessage = null
            aiResult = null

            val content = fileManager.readFile(project.id, selectedFile!!.path)
            val res = geminiAssistant.askAssistant(
                taskType = selectedTask,
                currentFileName = selectedFile!!.path,
                currentFileContent = content,
                userPrompt = prompt
            )

            isGenerating = false
            if (res.isSuccess) {
                aiResult = res.getOrNull()
            } else {
                errorMessage = res.exceptionOrNull()?.message ?: "Unknown error"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = VioletAi)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Coding Assistant", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("ai_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Target File Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Target File", fontWeight = FontWeight.Bold, color = VioletAi)

                    ExposedDropdownMenuBox(
                        expanded = fileMenuExpanded,
                        onExpandedChange = { fileMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedFile?.name ?: "No file selected",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select File") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fileMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_ai_file"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = fileMenuExpanded,
                            onDismissRequest = { fileMenuExpanded = false }
                        ) {
                            files.forEach { file ->
                                DropdownMenuItem(
                                    text = { Text(file.path) },
                                    onClick = {
                                        selectedFile = file
                                        fileMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Task Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("AI Task", fontWeight = FontWeight.Bold, color = VioletAi)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "fix" to "Fix Bugs",
                            "generate" to "Generate",
                            "explain" to "Explain",
                            "analyze_error" to "Errors"
                        ).forEach { (taskVal, label) ->
                            FilterChip(
                                selected = selectedTask == taskVal,
                                onClick = { selectedTask = taskVal },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    // Prompt Input
                    OutlinedTextField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        placeholder = { Text("Describe what you'd like the AI to do...") },
                        modifier = Modifier.fillMaxWidth().testTag("ai_prompt_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    // Quick Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPrompts.forEach { qp ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable { userPrompt = qp }
                            ) {
                                Text(
                                    text = qp,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Ask AI Button
                    Button(
                        onClick = { executeAiRequest() },
                        enabled = !isGenerating && selectedFile != null,
                        modifier = Modifier.fillMaxWidth().testTag("submit_ai_request"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini is thinking...")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask AI Assistant")
                        }
                    }
                }
            }

            // Error display
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp
                    )
                }
            }

            // AI Result Preview
            if (aiResult != null) {
                val res = aiResult!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Analysis & Proposal", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = res.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )

                        if (res.suggestedCode != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Proposed Changes for: ${res.targetFileName ?: selectedFile?.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealPrimary)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF090D16),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = res.suggestedCode.take(2000),
                                    color = Color(0xFFC9D1D9),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            // Apply to File button (Requirement 19: show proposed changes before applying)
                            Button(
                                onClick = {
                                    val target = res.targetFileName ?: selectedFile?.path
                                    if (target != null) {
                                        coroutineScope.launch {
                                            fileManager.writeFile(project.id, target, res.suggestedCode)
                                            Toast.makeText(context, "Applied changes to $target!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("apply_ai_code_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Apply Proposed Changes to File", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
