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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectItem
import com.example.data.repository.AndroidGenerator
import com.example.data.repository.GitHubRepoItem
import com.example.data.repository.GitHubService
import com.example.data.repository.ProjectRepository
import com.example.data.repository.WorkflowRunInfo
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitScreen(
    project: ProjectItem,
    gitHubService: GitHubService,
    androidGenerator: AndroidGenerator,
    projectRepository: ProjectRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var patToken by remember { mutableStateOf("") }
    val defaultOwner = if (project.config.gitHubRepo.contains('/')) project.config.gitHubRepo.substringBefore('/') else "tata125125tata-tech"
    val defaultRepo = if (project.config.gitHubRepo.contains('/')) project.config.gitHubRepo.substringAfter('/') else "web-to-apk"
    var ownerName by remember { mutableStateOf(defaultOwner) }
    var repoName by remember { mutableStateOf(defaultRepo) }
    var branchName by remember { mutableStateOf(project.config.gitHubBranch) }

    var isVerifying by remember { mutableStateOf(false) }
    var authenticatedUser by remember { mutableStateOf<String?>(null) }
    var userRepos by remember { mutableStateOf<List<GitHubRepoItem>>(emptyList()) }
    var workflowRuns by remember { mutableStateOf<List<WorkflowRunInfo>>(emptyList()) }
    var isTriggering by remember { mutableStateOf(false) }
    var showWorkflowYaml by remember { mutableStateOf(false) }

    fun refreshWorkflowRuns() {
        if (patToken.isNotBlank() && ownerName.isNotBlank() && repoName.isNotBlank()) {
            coroutineScope.launch {
                val res = gitHubService.getRecentWorkflowRuns(patToken, ownerName, repoName)
                if (res.isSuccess) {
                    workflowRuns = res.getOrNull() ?: emptyList()
                }
            }
        }
    }

    LaunchedEffect(patToken, ownerName, repoName) {
        refreshWorkflowRuns()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GitHub & Remote CI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("git_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshWorkflowRuns() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Runs")
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
            // Authentication Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("GitHub Authentication", fontWeight = FontWeight.Bold, color = TealPrimary)
                    Text(
                        "Create a Personal Access Token (classic or fine-grained) with 'repo' and 'workflow' scopes to enable remote builds.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = patToken,
                        onValueChange = { patToken = it },
                        label = { Text("Personal Access Token (ghp_...)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("github_token_input")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isVerifying = true
                                    val res = gitHubService.verifyToken(patToken.trim())
                                    isVerifying = false
                                    if (res.isSuccess) {
                                        authenticatedUser = res.getOrNull()
                                        ownerName = authenticatedUser ?: ""
                                        Toast.makeText(context, "Connected as @$authenticatedUser", Toast.LENGTH_SHORT).show()
                                        // Fetch repos
                                        val reposRes = gitHubService.listUserRepos(patToken.trim())
                                        if (reposRes.isSuccess) {
                                            userRepos = reposRes.getOrNull() ?: emptyList()
                                        }
                                    } else {
                                        Toast.makeText(context, "Auth failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = !isVerifying && patToken.isNotBlank(),
                            modifier = Modifier.weight(1f).testTag("verify_github_button")
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Connect Token")
                            }
                        }

                        if (authenticatedUser != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = "@$authenticatedUser",
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Repository Link Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Target Repository", fontWeight = FontWeight.Bold, color = TealPrimary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Owner") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = repoName,
                            onValueChange = { repoName = it },
                            label = { Text("Repo Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = { Text("Default Branch") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val fullRepo = "$ownerName/$repoName"
                                    projectRepository.updateProjectConfig(
                                        project.config.copy(
                                            gitHubRepo = fullRepo,
                                            gitHubBranch = branchName.trim()
                                        )
                                    )
                                    Toast.makeText(context, "Linked repo $fullRepo", Toast.LENGTH_SHORT).show()
                                    refreshWorkflowRuns()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("link_repo_button")
                        ) {
                            Text("Save Link")
                        }

                        OutlinedButton(
                            onClick = { showWorkflowYaml = !showWorkflowYaml },
                            modifier = Modifier.weight(1f).testTag("view_workflow_yaml_button")
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Workflow")
                        }
                    }

                    // Trigger Build Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isTriggering = true
                                val res = gitHubService.triggerWorkflowDispatch(
                                    token = patToken.trim(),
                                    owner = ownerName.trim(),
                                    repo = repoName.trim(),
                                    workflowFileName = "build-apk.yml",
                                    ref = branchName.trim(),
                                    buildType = "debug"
                                )
                                isTriggering = false
                                if (res.isSuccess) {
                                    Toast.makeText(context, "Dispatched build to GitHub Actions!", Toast.LENGTH_SHORT).show()
                                    refreshWorkflowRuns()
                                } else {
                                    Toast.makeText(context, "Dispatch failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isTriggering && patToken.isNotBlank() && ownerName.isNotBlank() && repoName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().testTag("trigger_workflow_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trigger GitHub Actions Build", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Workflow YAML Preview
            if (showWorkflowYaml) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = ".github/workflows/build-apk.yml",
                            color = TealPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = androidGenerator.generateGitHubWorkflow(project.config),
                            color = Color(0xFFC9D1D9),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Recent Workflow Runs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent GitHub Actions Runs", fontWeight = FontWeight.Bold, color = TealPrimary)
                        Text(
                            text = "${workflowRuns.size} runs",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (workflowRuns.isEmpty()) {
                        Text(
                            text = "No recent workflow runs found. Tap 'Trigger GitHub Actions Build' to dispatch a run.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        workflowRuns.forEach { run ->
                            val (icon, color) = when (run.conclusion) {
                                "success" -> Icons.Default.CheckCircle to EmeraldSuccess
                                "failure" -> Icons.Default.Error to RoseError
                                else -> Icons.Default.HourglassTop to TealPrimary
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Run #${run.id} · ${run.status.uppercase()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Conclusion: ${run.conclusion ?: "Running..."} · ${run.createdAt.take(10)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
