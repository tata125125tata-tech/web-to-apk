package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BuildLogEntry
import com.example.data.model.LogLevel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TerminalView(
    logs: List<BuildLogEntry>,
    isBuilding: Boolean,
    onClearLogs: () -> Unit,
    onRetryBuild: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Terminal Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(if (isBuilding) AmberWarning else EmeraldSuccess, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isBuilding) "BUILDING..." else "TERMINAL READY",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${logs.size} log entries",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row {
                IconButton(
                    onClick = {
                        val text = logs.joinToString("\n") { entry ->
                            "[${timeFormat.format(Date(entry.timestamp))}] ${entry.message}"
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Build Log", text))
                        Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_log_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Log",
                        tint = Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier.testTag("clear_log_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Log",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Terminal Log List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF04060A), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Build logs will appear here...\nTap 'Build Debug APK' or 'Build with GitHub Actions' to begin.",
                        color = Color(0xFF475569),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(logs) { entry ->
                        val color = when (entry.level) {
                            LogLevel.INFO -> Color(0xFFE2E8F0)
                            LogLevel.SUCCESS -> EmeraldSuccess
                            LogLevel.WARNING -> AmberWarning
                            LogLevel.ERROR -> RoseError
                            LogLevel.COMMAND -> TealPrimary
                        }

                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                            Text(
                                text = timeFormat.format(Date(entry.timestamp)),
                                color = Color(0xFF475569),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = entry.message,
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Terminal Actions bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = onRetryBuild,
                enabled = !isBuilding,
                modifier = Modifier.testTag("retry_build_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.padding(end = 4.dp))
                Text("Retry Build")
            }
        }
    }
}
