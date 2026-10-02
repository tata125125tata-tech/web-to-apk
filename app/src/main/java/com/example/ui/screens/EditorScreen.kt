package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EditorTab
import com.example.data.model.ProjectItem
import com.example.data.repository.FileManager
import com.example.ui.components.CodeKeyboardBar
import com.example.ui.theme.EditorBackgroundDark
import com.example.ui.theme.EditorGutterDark
import com.example.ui.theme.EditorLineNumberDark
import com.example.ui.theme.EditorTextDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: ProjectItem,
    initialFile: String,
    fileManager: FileManager,
    onBack: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val openTabs = remember {
        mutableStateListOf(
            EditorTab(
                path = initialFile,
                name = initialFile.substringAfterLast('/'),
                isModified = false
            )
        )
    }
    var activeTabPath by remember { mutableStateOf(initialFile) }
    var codeContent by remember { mutableStateOf(TextFieldValue("")) }
    var isDirty by remember { mutableStateOf(false) }

    // Undo / Redo history
    val undoStack = remember { mutableListOf<String>() }
    val redoStack = remember { mutableListOf<String>() }

    // Search and Replace
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }

    // Load file content when active tab changes
    LaunchedEffect(activeTabPath) {
        val content = fileManager.readFile(project.id, activeTabPath)
        codeContent = TextFieldValue(content, selection = TextRange(0))
        undoStack.clear()
        redoStack.clear()
        undoStack.add(content)
        isDirty = false
    }

    fun saveCurrentFile() {
        coroutineScope.launch {
            val ok = fileManager.writeFile(project.id, activeTabPath, codeContent.text)
            if (ok) {
                isDirty = false
                val idx = openTabs.indexOfFirst { it.path == activeTabPath }
                if (idx >= 0) {
                    openTabs[idx] = openTabs[idx].copy(isModified = false)
                }
                Toast.makeText(context, "Saved ${activeTabPath.substringAfterLast('/')}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val lines = remember(codeContent.text) {
        codeContent.text.lines()
    }
    val lineCount = lines.size.coerceAtLeast(1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeTabPath.substringAfterLast('/'),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDirty) TealPrimary else MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (undoStack.size > 1) {
                                val current = undoStack.removeAt(undoStack.lastIndex)
                                redoStack.add(current)
                                val previous = undoStack.last()
                                codeContent = TextFieldValue(previous)
                                isDirty = true
                            }
                        },
                        enabled = undoStack.size > 1,
                        modifier = Modifier.testTag("editor_undo_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }

                    IconButton(
                        onClick = {
                            if (redoStack.isNotEmpty()) {
                                val next = redoStack.removeAt(redoStack.lastIndex)
                                undoStack.add(next)
                                codeContent = TextFieldValue(next)
                                isDirty = true
                            }
                        },
                        enabled = redoStack.isNotEmpty(),
                        modifier = Modifier.testTag("editor_redo_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }

                    IconButton(
                        onClick = { showSearchBar = !showSearchBar },
                        modifier = Modifier.testTag("editor_search_toggle")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search & Replace")
                    }

                    IconButton(
                        onClick = { saveCurrentFile() },
                        modifier = Modifier.testTag("editor_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (isDirty) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onPreview,
                        modifier = Modifier.testTag("editor_preview_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = TealPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            CodeKeyboardBar(
                onInsertSymbol = { symbol ->
                    val text = codeContent.text
                    val start = codeContent.selection.start
                    val end = codeContent.selection.end
                    val newText = text.replaceRange(start, end, symbol)
                    val newCursor = start + symbol.length
                    codeContent = TextFieldValue(newText, selection = TextRange(newCursor))
                    undoStack.add(newText)
                    redoStack.clear()
                    isDirty = true
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EditorBackgroundDark)
        ) {
            // File Tabs Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                color = EditorGutterDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    openTabs.forEach { tab ->
                        val isActive = tab.path == activeTabPath
                        Surface(
                            onClick = { activeTabPath = tab.path },
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                            color = if (isActive) EditorBackgroundDark else Color.Transparent,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${tab.name}${if (tab.isModified || (isActive && isDirty)) " •" else ""}",
                                    color = if (isActive) TealPrimary else EditorLineNumberDark,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Search & Replace Bar
            if (showSearchBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Find text...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            IconButton(onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val idx = codeContent.text.indexOf(searchQuery, codeContent.selection.end, ignoreCase = true)
                                    val targetIdx = if (idx >= 0) idx else codeContent.text.indexOf(searchQuery, 0, ignoreCase = true)
                                    if (targetIdx >= 0) {
                                        codeContent = codeContent.copy(selection = TextRange(targetIdx, targetIdx + searchQuery.length))
                                    } else {
                                        Toast.makeText(context, "Text not found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "Next")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = replaceQuery,
                                onValueChange = { replaceQuery = it },
                                placeholder = { Text("Replace with...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            IconButton(onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val newText = codeContent.text.replace(searchQuery, replaceQuery)
                                    codeContent = TextFieldValue(newText)
                                    undoStack.add(newText)
                                    isDirty = true
                                    Toast.makeText(context, "Replaced occurrences", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.FindReplace, contentDescription = "Replace All")
                            }
                        }
                    }
                }
            }

            // Code Editor Body with Line Numbers
            val editorScrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(editorScrollState)
            ) {
                // Line Numbers Gutter
                Column(
                    modifier = Modifier
                        .background(EditorGutterDark)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .width(42.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = "$i",
                            color = EditorLineNumberDark,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Text Input Area
                BasicTextField(
                    value = codeContent,
                    onValueChange = { newValue ->
                        if (newValue.text != codeContent.text) {
                            undoStack.add(newValue.text)
                            redoStack.clear()
                            isDirty = true
                            val idx = openTabs.indexOfFirst { it.path == activeTabPath }
                            if (idx >= 0) {
                                openTabs[idx] = openTabs[idx].copy(isModified = true)
                            }
                        }
                        codeContent = newValue
                    },
                    textStyle = TextStyle(
                        color = EditorTextDark,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(TealPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .testTag("code_editor_field")
                )
            }
        }
    }
}
