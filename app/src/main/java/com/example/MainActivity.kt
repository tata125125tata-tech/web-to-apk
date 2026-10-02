package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.local.DatabaseProvider
import com.example.data.model.ProjectItem
import com.example.data.repository.AndroidGenerator
import com.example.data.repository.BuildEngine
import com.example.data.repository.FileManager
import com.example.data.repository.GeminiAssistant
import com.example.data.repository.GitHubService
import com.example.data.repository.ProjectRepository
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AndroidSettingsScreen
import com.example.ui.screens.BuildScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.GitScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LivePreviewScreen
import com.example.ui.theme.MyApplicationTheme

sealed interface IdeScreen {
    data object Home : IdeScreen
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
        val buildEngine = BuildEngine(this, androidGenerator, fileManager, projectRepository, gitHubService)
        val geminiAssistant = GeminiAssistant()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Web2ApkApp(
                        projectRepository = projectRepository,
                        fileManager = fileManager,
                        androidGenerator = androidGenerator,
                        gitHubService = gitHubService,
                        buildEngine = buildEngine,
                        geminiAssistant = geminiAssistant
                    )
                }
            }
        }
    }
}

@Composable
fun Web2ApkApp(
    projectRepository: ProjectRepository,
    fileManager: FileManager,
    androidGenerator: AndroidGenerator,
    gitHubService: GitHubService,
    buildEngine: BuildEngine,
    geminiAssistant: GeminiAssistant
) {
    val projects by projectRepository.allProjects.collectAsState(initial = emptyList())
    val screenStack = remember { mutableStateListOf<IdeScreen>(IdeScreen.Home) }
    val currentScreen = screenStack.lastOrNull() ?: IdeScreen.Home

    fun navigateTo(screen: IdeScreen) {
        screenStack.add(screen)
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.lastIndex)
        }
    }

    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    when (val screen = currentScreen) {
        is IdeScreen.Home -> {
            HomeScreen(
                projects = projects,
                projectRepository = projectRepository,
                fileManager = fileManager,
                onOpenProject = { id -> navigateTo(IdeScreen.Dashboard(id)) },
                onOpenGitHubForProject = { id -> navigateTo(IdeScreen.Git(id)) }
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
                HomeScreen(
                    projects = projects,
                    projectRepository = projectRepository,
                    fileManager = fileManager,
                    onOpenProject = { id -> navigateTo(IdeScreen.Dashboard(id)) },
                    onOpenGitHubForProject = { id -> navigateTo(IdeScreen.Git(id)) }
                )
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
                    buildEngine = buildEngine,
                    androidGenerator = androidGenerator,
                    onBack = { navigateBack() },
                    onOpenGitSettings = { navigateTo(IdeScreen.Git(project.id)) }
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
