package com.example.data.repository

import android.content.Context
import com.example.data.model.FileItem
import com.example.data.model.ProjectConfig
import com.example.data.model.ProjectTemplate
import com.example.data.model.WebFileType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileManager(private val context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val configAdapter = moshi.adapter(ProjectConfig::class.java).indent("  ")

    fun getProjectDir(projectId: String): File {
        val dir = File(context.filesDir, "projects/$projectId")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getWebsiteDir(projectId: String): File {
        val dir = File(getProjectDir(projectId), "website")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun initializeProjectWithTemplate(
        config: ProjectConfig,
        template: ProjectTemplate
    ) = withContext(Dispatchers.IO) {
        val projectDir = getProjectDir(config.id)
        val websiteDir = getWebsiteDir(config.id)

        // Save project.json
        val configFile = File(projectDir, "project.json")
        configFile.writeText(configAdapter.toJson(config))

        // Populate default template files
        template.defaultFiles.forEach { (relPath, content) ->
            val targetFile = File(websiteDir, relPath)
            targetFile.parentFile?.mkdirs()
            targetFile.writeText(content)
        }
    }

    suspend fun saveProjectConfig(config: ProjectConfig) = withContext(Dispatchers.IO) {
        val projectDir = getProjectDir(config.id)
        val configFile = File(projectDir, "project.json")
        configFile.writeText(configAdapter.toJson(config))
    }

    suspend fun loadProjectConfig(projectId: String): ProjectConfig? = withContext(Dispatchers.IO) {
        val configFile = File(getProjectDir(projectId), "project.json")
        if (configFile.exists()) {
            try {
                return@withContext configAdapter.fromJson(configFile.readText())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        null
    }

    suspend fun listFiles(projectId: String, relativeDir: String = ""): List<FileItem> = withContext(Dispatchers.IO) {
        val websiteDir = getWebsiteDir(projectId)
        val currentDir = if (relativeDir.isEmpty()) websiteDir else File(websiteDir, relativeDir)
        if (!currentDir.exists() || !currentDir.isDirectory) return@withContext emptyList()

        val files = currentDir.listFiles() ?: return@withContext emptyList()
        val items = files.map { file ->
            val relPath = file.relativeTo(websiteDir).path.replace('\\', '/')
            FileItem(
                path = relPath,
                name = file.name,
                isDirectory = file.isDirectory,
                extension = file.extension,
                size = if (file.isDirectory) 0L else file.length(),
                lastModified = file.lastModified(),
                fileType = if (file.isDirectory) WebFileType.OTHER else WebFileType.fromExtension(file.extension)
            )
        }

        // Sort directories first, then alphabetically
        items.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    suspend fun readFile(projectId: String, relativePath: String): String = withContext(Dispatchers.IO) {
        val file = File(getWebsiteDir(projectId), relativePath)
        if (file.exists() && file.isFile) {
            file.readText()
        } else {
            ""
        }
    }

    suspend fun writeFile(projectId: String, relativePath: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(getWebsiteDir(projectId), relativePath)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createFile(projectId: String, relativePath: String, initialContent: String = ""): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(getWebsiteDir(projectId), relativePath)
            if (file.exists()) return@withContext false
            file.parentFile?.mkdirs()
            file.writeText(initialContent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createFolder(projectId: String, relativePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dir = File(getWebsiteDir(projectId), relativePath)
            if (dir.exists()) return@withContext false
            dir.mkdirs()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteItem(projectId: String, relativePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(getWebsiteDir(projectId), relativePath)
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun renameItem(projectId: String, oldRelativePath: String, newName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val oldFile = File(getWebsiteDir(projectId), oldRelativePath)
            if (!oldFile.exists()) return@withContext false
            val newFile = File(oldFile.parentFile, newName)
            if (newFile.exists()) return@withContext false
            oldFile.renameTo(newFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun duplicateItem(projectId: String, relativePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val src = File(getWebsiteDir(projectId), relativePath)
            if (!src.exists()) return@withContext false
            val parent = src.parentFile ?: getWebsiteDir(projectId)
            val nameWithoutExt = src.nameWithoutExtension
            val ext = if (src.extension.isNotEmpty()) ".${src.extension}" else ""
            var copyIndex = 1
            var dest = File(parent, "${nameWithoutExt}_copy$ext")
            while (dest.exists()) {
                copyIndex++
                dest = File(parent, "${nameWithoutExt}_copy$copyIndex$ext")
            }
            if (src.isDirectory) {
                src.copyRecursively(dest)
            } else {
                src.copyTo(dest)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun exportProjectZip(projectId: String): File = withContext(Dispatchers.IO) {
        val projectDir = getProjectDir(projectId)
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val zipFile = File(exportDir, "project_${projectId}_export.zip")
        if (zipFile.exists()) zipFile.delete()

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            projectDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relPath = file.relativeTo(projectDir).path.replace('\\', '/')
                    val entry = ZipEntry(relPath)
                    zos.putNextEntry(entry)
                    FileInputStream(file).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
        zipFile
    }

    suspend fun importProjectZip(inputStream: InputStream, fallbackName: String): ProjectConfig = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, "import_temp_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        try {
            // Unpack with strict path traversal checks
            val canonicalTargetDir = tempDir.canonicalPath
            ZipInputStream(inputStream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name
                    if (!entryName.contains("..")) {
                        val destinationFile = File(tempDir, entryName)
                        val canonicalDestinationFile = destinationFile.canonicalPath
                        if (canonicalDestinationFile.startsWith(canonicalTargetDir)) {
                            if (entry.isDirectory) {
                                destinationFile.mkdirs()
                            } else {
                                destinationFile.parentFile?.mkdirs()
                                FileOutputStream(destinationFile).use { fos ->
                                    zis.copyTo(fos)
                                }
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            // Find project.json or create default
            val configFile = File(tempDir, "project.json")
            val config = if (configFile.exists()) {
                configAdapter.fromJson(configFile.readText()) ?: ProjectConfig(
                    name = fallbackName,
                    packageName = "com.example.${fallbackName.lowercase().replace(Regex("[^a-z0-9]"), "")}"
                )
            } else {
                ProjectConfig(
                    name = fallbackName,
                    packageName = "com.example.${fallbackName.lowercase().replace(Regex("[^a-z0-9]"), "")}"
                )
            }

            // Copy to new project location
            val newProjectDir = getProjectDir(config.id)
            val newWebsiteDir = getWebsiteDir(config.id)

            // Look for website dir or files inside root
            val extractedWebsiteDir = File(tempDir, "website")
            if (extractedWebsiteDir.exists() && extractedWebsiteDir.isDirectory) {
                extractedWebsiteDir.copyRecursively(newWebsiteDir, overwrite = true)
            } else {
                tempDir.listFiles()?.forEach { file ->
                    if (file.name != "project.json") {
                        if (file.isDirectory) {
                            file.copyRecursively(File(newWebsiteDir, file.name), overwrite = true)
                        } else {
                            file.copyTo(File(newWebsiteDir, file.name), overwrite = true)
                        }
                    }
                }
            }

            // Write final project.json
            File(newProjectDir, "project.json").writeText(configAdapter.toJson(config))
            return@withContext config
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
