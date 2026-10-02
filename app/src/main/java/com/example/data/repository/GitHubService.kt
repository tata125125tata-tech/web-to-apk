package com.example.data.repository

import com.example.data.model.BuildLogEntry
import com.example.data.model.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GitHubRepoItem(
    val name: String,
    val fullName: String,
    val defaultBranch: String,
    val isPrivate: Boolean,
    val htmlUrl: String
)

data class WorkflowRunInfo(
    val id: Long,
    val status: String, // "queued", "in_progress", "completed"
    val conclusion: String?, // "success", "failure", "cancelled", null
    val htmlUrl: String,
    val createdAt: String
)

class GitHubService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun verifyToken(token: String): Result<String> = withContext(Dispatchers.IO) {
        if (token.isBlank()) return@withContext Result.failure(Exception("GitHub Personal Access Token is required."))
        try {
            val request = Request.Builder()
                .url("https://api.github.com/user")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val login = json.optString("login", "User")
                    Result.success(login)
                } else {
                    Result.failure(Exception("GitHub Auth failed (${response.code}): ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listUserRepos(token: String): Result<List<GitHubRepoItem>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/user/repos?sort=updated&per_page=30")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "[]"
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<GitHubRepoItem>()
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        list.add(
                            GitHubRepoItem(
                                name = item.getString("name"),
                                fullName = item.getString("full_name"),
                                defaultBranch = item.optString("default_branch", "main"),
                                isPrivate = item.optBoolean("private", false),
                                htmlUrl = item.optString("html_url", "")
                            )
                        )
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Failed to list repos: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRepo(token: String, name: String, isPrivate: Boolean): Result<GitHubRepoItem> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("name", name)
                put("description", "Generated Android WebView App via Web2APK IDE")
                put("private", isPrivate)
                put("auto_init", true)
            }
            val request = Request.Builder()
                .url("https://api.github.com/user/repos")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "{}"
                    val json = JSONObject(body)
                    Result.success(
                        GitHubRepoItem(
                            name = json.getString("name"),
                            fullName = json.getString("full_name"),
                            defaultBranch = json.optString("default_branch", "main"),
                            isPrivate = json.optBoolean("private", false),
                            htmlUrl = json.optString("html_url", "")
                        )
                    )
                } else {
                    Result.failure(Exception("Failed to create repository: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun triggerWorkflowDispatch(
        token: String,
        owner: String,
        repo: String,
        workflowFileName: String = "build-apk.yml",
        ref: String = "main",
        buildType: String = "debug"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("ref", ref)
                put("inputs", JSONObject().apply {
                    put("build_type", buildType)
                })
            }
            val request = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo/actions/workflows/$workflowFileName/dispatches")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 204) {
                    Result.success("Workflow dispatch triggered successfully for $owner/$repo on branch '$ref'")
                } else {
                    val err = response.body?.string() ?: ""
                    Result.failure(Exception("Failed to trigger workflow (HTTP ${response.code}): $err"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentWorkflowRuns(
        token: String,
        owner: String,
        repo: String
    ): Result<List<WorkflowRunInfo>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo/actions/runs?per_page=5")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: "{}"
                    val json = JSONObject(body)
                    val runsArray = json.optJSONArray("workflow_runs") ?: JSONArray()
                    val list = mutableListOf<WorkflowRunInfo>()
                    for (i in 0 until runsArray.length()) {
                        val item = runsArray.getJSONObject(i)
                        list.add(
                            WorkflowRunInfo(
                                id = item.getLong("id"),
                                status = item.getString("status"),
                                conclusion = if (item.isNull("conclusion")) null else item.getString("conclusion"),
                                htmlUrl = item.optString("html_url", ""),
                                createdAt = item.optString("created_at", "")
                            )
                        )
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("Failed to get workflow runs: HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
