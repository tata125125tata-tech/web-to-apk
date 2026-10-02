package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.local.DatabaseProvider
import com.example.data.model.ProjectItem
import com.example.data.repository.AndroidGenerator
import com.example.data.repository.FileManager
import com.example.data.repository.GeminiAssistant
import com.example.data.repository.GitHubService
import com.example.data.repository.LocalBuildEngine
import com.example.data.repository.ProjectRepository
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AndroidSettingsScreen
import com.example.ui.screens.BuildEnvironmentScreen
import com.example.ui.screens.BuildScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.GitScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LivePreviewScreen
import com.example.ui.screens.ProjectsListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface IdeScreen {
    data object Home : IdeScreen
    data object ProjectsList : IdeScreen
    data object SettingsTab : IdeScreen
    data object BuildEnvironment : IdeScreen
    data class Dashboard(val projectId: String) : IdeScreen
    data class FileManager(val projectId: String) : IdeScreen
    data class Editor(val projectId: String, val filePath: String) : IdeScreen
    data class Preview(val projectId: String) : IdeScreen
    data class AndroidSettings(val projectId: String) : IdeScreen
    data class Build(val projectId: String) : IdeScreen
    data class Git(val projectId: String) : IdeScreen
    data class AiAssistant(val projectId: String) : IdeScreen
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = DatabaseProvider.getDatabase(this)
        val fileManager = FileManager(this)
        val projectRepository = ProjectRepository(database.projectDao(), fileManager)
        val androidGenerator = AndroidGenerator(this, fileManager)
        val gitHubService = GitHubService()
        val localBuildEngine = LocalBuildEngine(this, androidGenerator, fileManager, projectRepository)
        val geminiAssistant = GeminiAssistant()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Web2AppMainApp(
                        projectRepository = projectRepository,
                        fileManager = fileManager,
                        androidGenerator = androidGenerator,
                        gitHubService = gitHubService,
                        localBuildEngine = localBuildEngine,
                        geminiAssistant = geminiAssistant
                    )
                }
            }
        }
    }
}

@Composable
fun Web2AppMainApp(
    projectRepository: ProjectRepository,
    fileManager: FileManager,
    androidGenerator: AndroidGenerator,
    gitHubService: GitHubService,
    localBuildEngine: LocalBuildEngine,
    geminiAssistant: GeminiAssistant
) {
    val projects by projectRepository.allProjects.collectAsState(initial = emptyList())
    val screenStack = remember { mutableStateListOf<IdeScreen>(IdeScreen.Home) }
    val currentScreen = screenStack.lastOrNull() ?: IdeScreen.Home
    var activeProjectId by remember { mutableStateOf<String?>(null) }

    fun navigateTo(screen: IdeScreen) {
        screenStack.add(screen)
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
        }
    }

    fun switchTab(screen: IdeScreen) {
        screenStack.clear()
        screenStack.add(screen)
    }

    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    val isTopLevelScreen = currentScreen is IdeScreen.Home || currentScreen is IdeScreen.ProjectsList || currentScreen is IdeScreen.SettingsTab

    Scaffold(
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentScreen is IdeScreen.Home,
                        onClick = { switchTab(IdeScreen.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentScreen is IdeScreen.ProjectsList,
                        onClick = { switchTab(IdeScreen.ProjectsList) },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Projects") },
                        label = { Text("Projects") },
                        modifier = Modifier.testTag("nav_projects")
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            val targetId = activeProjectId ?: projects.firstOrNull()?.id
                            if (targetId != null) {
                                activeProjectId = targetId
                                navigateTo(IdeScreen.Editor(targetId, "index.html"))
                            } else {
                                switchTab(IdeScreen.ProjectsList)
                            }
                        },
                        icon = { Icon(Icons.Default.Code, contentDescription = "Editor") },
                        label = { Text("Editor") },
                        modifier = Modifier.testTag("nav_editor")
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            val targetId = activeProjectId ?: projects.firstOrNull()?.id
                            if (targetId != null) {
                                activeProjectId = targetId
                                navigateTo(IdeScreen.Build(targetId))
                            } else {
                                navigateTo(IdeScreen.BuildEnvironment)
                            }
                        },
                        icon = { Icon(Icons.Default.Build, contentDescription = "Build") },
                        label = { Text("Build") },
                        modifier = Modifier.testTag("nav_build")
                    )
                    NavigationBarItem(
                        selected = currentScreen is IdeScreen.SettingsTab,
                        onClick = { switchTab(IdeScreen.SettingsTab) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTopLevelScreen) innerPadding else androidx.compose.foundation.layout.PaddingValues())
        ) {
            when (val screen = currentScreen) {
                is IdeScreen.Home -> {
                    HomeScreen(
                        projects = projects,
                        projectRepository = projectRepository,
                        fileManager = fileManager,
                        onOpenProject = { id ->
                            activeProjectId = id
                            navigateTo(IdeScreen.Dashboard(id))
                        },
                        onOpenGitHubForProject = { id ->
                            activeProjectId = id
                            navigateTo(IdeScreen.Git(id))
                        },
                        onOpenEnvironmentDiagnostics = {
                            navigateTo(IdeScreen.BuildEnvironment)
                        }
                    )
                }

                is IdeScreen.ProjectsList -> {
                    ProjectsListScreen(
                        projects = projects,
                        projectRepository = projectRepository,
                        fileManager = fileManager,
                        onOpenProject = { id ->
                            activeProjectId = id
                            navigateTo(IdeScreen.Dashboard(id))
                        },
                        onOpenGitHubForProject = { id ->
                            activeProjectId = id
                            navigateTo(IdeScreen.Git(id))
                        }
                    )
                }

                is IdeScreen.SettingsTab -> {
                    SettingsScreen(
                        localBuildEngine = localBuildEngine,
                        onOpenEnvironmentDiagnostics = { navigateTo(IdeScreen.BuildEnvironment) }
                    )
                }

                is IdeScreen.BuildEnvironment -> {
                    BuildEnvironmentScreen(
                        localBuildEngine = localBuildEngine,
                        onBack = { navigateBack() }
                    )
                }

                is IdeScreen.Dashboard -> {
                    val project = projects.find { it.id == screen.projectId }
                    if (project != null) {
                        DashboardScreen(
                            project = project,
                            onBack = { navigateBack() },
                            onNavigateWebsiteFiles = { navigateTo(IdeScreen.FileManager(project.id)) },
                            onNavigatePreview = { navigateTo(IdeScreen.Preview(project.id)) },
                            onNavigateAndroidSettings = { navigateTo(IdeScreen.AndroidSettings(project.id)) },
                            onNavigateBuild = { navigateTo(IdeScreen.Build(project.id)) },
                            onNavigateGit = { navigateTo(IdeScreen.Git(project.id)) },
                            onNavigateAiAssistant = { navigateTo(IdeScreen.AiAssistant(project.id)) }
                        )
                    } else {
                        switchTab(IdeScreen.Home)
                    }
                }

                is IdeScreen.FileManager -> {
                    val project = projects.find { it.id == screen.projectId }
                    if (project != null) {
                        FileManagerScreen(
                            project = project,
                            fileManager = fileManager,
                            onBack = { navigateBack() },
                            onOpenFile = { path -> navigateTo(IdeScreen.Editor(project.id, path)) }
                        )
                    }
                }

                is IdeScreen.Editor -> {
                    val project = projects.find { it.id == screen.projectId }
                    if (project != null) {
                        EditorScreen(
                            project = project,
                            initialFile = screen.filePath,
                            fileManager = fileManager,
                            onBack = { navigateBack() },
                            onPreview = { navigateTo(IdeScreen.Preview(project.id)) }
                        )
                    }
                }

                is IdeScreen.Preview -> {
                    val project = projects.find { it.id == screen.projectId }
                    if (project != null) {
                        LivePreviewScreen(
                            project = project,
                            fileManager = fileManager,
                            onBack = { navigateBack() }
                        )
                    }
                }

                is IdeScreen.AndroidSettings -> {
                    val project = projects.find { it.id == screen.projectId }
                    if (project != null) {
                        AndroidSettingsScreen(
                            project = project,
                            projectRepository = projectRepository,
                            onBack = { navigateBack() }
                        )
                    }
                }

                is IdeScreen.Build -> {
                    val project = projects.find { it.id == screen.projectId }
                    if (project != null) {
                        BuildScreen(
                            project = project,
                            localBuildEngine = localBuildEngine,
                            androidGenerator = androidGenerator,
                            onBack = { navigateBack() },
                            onOpenEnvironmentDiagnostics = { navigateTo(IdeScreen.BuildEnvironment) }
                        )
                    }
                }

                is IdeScreen.Git -> {
                    val project = projects.find { it.id == screen.projectId }
                    if (project != null) {
                        GitScreen(
                            project = project,
                            gitHubService = gitHubService,
                            androidGenerator = androidGenerator,
                            projectRepository = projectRepository,
                            onBack = { navigateBack() }
                        )
                    }
                }

                is IdeScreen.AiAssistant -> {
                    val project = projects.find { it.id == screen.projectId }
                    if (project != null) {
                        AiAssistantScreen(
                            project = project,
                            geminiAssistant = geminiAssistant,
                            fileManager = fileManager,
                            onBack = { navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
