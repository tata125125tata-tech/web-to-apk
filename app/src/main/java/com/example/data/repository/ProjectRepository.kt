package com.example.data.repository

import com.example.data.local.ProjectDao
import com.example.data.local.ProjectEntity
import com.example.data.model.ProjectConfig
import com.example.data.model.ProjectItem
import com.example.data.model.ProjectTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val fileManager: FileManager
) {
    val allProjects: Flow<List<ProjectItem>> = projectDao.getAllProjects().map { list ->
        list.map { it.toProjectItem() }
    }

    fun getProjectFlow(id: String): Flow<ProjectItem?> = projectDao.getProjectFlowById(id).map {
        it?.toProjectItem()
    }

    suspend fun getProjectById(id: String): ProjectItem? {
        return projectDao.getProjectById(id)?.toProjectItem()
    }

    suspend fun createProject(
        name: String,
        packageName: String,
        template: ProjectTemplate,
        mode: String = if (template.isRemoteUrl) "remote" else "local",
        remoteUrl: String = "https://example.com"
    ): ProjectConfig {
        val config = ProjectConfig(
            id = UUID.randomUUID().toString(),
            name = name,
            packageName = packageName,
            mode = mode,
            remoteUrl = remoteUrl,
            homepage = "index.html"
        )

        // Initialize file structure
        fileManager.initializeProjectWithTemplate(config, template)

        // Insert into Room
        val entity = ProjectEntity.fromConfig(config)
        projectDao.insertProject(entity)

        return config
    }

    suspend fun updateProjectConfig(config: ProjectConfig) {
        fileManager.saveProjectConfig(config)
        val entity = ProjectEntity.fromConfig(config)
        projectDao.updateProject(entity)
    }

    suspend fun duplicateProject(sourceId: String, newName: String): ProjectConfig? {
        val sourceConfig = fileManager.loadProjectConfig(sourceId) ?: return null
        val newId = UUID.randomUUID().toString()
        val newPackage = "${sourceConfig.packageName}.copy"

        val newConfig = sourceConfig.copy(
            id = newId,
            name = newName,
            packageName = newPackage,
            lastBuildStatus = "NONE",
            lastBuildLog = "",
            lastBuildTime = 0L
        )

        // Copy files
        val srcWebsiteDir = fileManager.getWebsiteDir(sourceId)
        val destWebsiteDir = fileManager.getWebsiteDir(newId)
        if (srcWebsiteDir.exists()) {
            srcWebsiteDir.copyRecursively(destWebsiteDir, overwrite = true)
        }

        fileManager.saveProjectConfig(newConfig)
        projectDao.insertProject(ProjectEntity.fromConfig(newConfig))
        return newConfig
    }

    suspend fun deleteProject(projectId: String) {
        projectDao.deleteProjectById(projectId)
        fileManager.getProjectDir(projectId).deleteRecursively()
    }

    suspend fun updateBuildStatus(projectId: String, status: String, log: String) {
        projectDao.updateBuildStatus(projectId, status, System.currentTimeMillis(), log)
    }
}
