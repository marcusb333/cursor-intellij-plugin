package com.cursor.plugin

import com.cursor.plugin.settings.CursorSettingsState
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service(Service.Level.PROJECT)
class CompletionsChatAsyncService(
    val project: Project,
) : ChatServiceInterface {
    // Class-level coroutine scope for managing all async operations
    private val serviceJob = SupervisorJob()
    internal val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val httpClient: HttpClient
        get() {
            val timeout = CursorSettingsState.instance.timeoutSeconds.toLong()
            return HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .build()
        }

    private val gson: Gson by lazy {
        Gson()
    }

    companion object {
        fun getInstance(project: Project): CompletionsChatAsyncService = project.getService(CompletionsChatAsyncService::class.java)
    }

    /**
     * Properly dispose of the service and cancel all running coroutines
     */
    fun dispose() {
        serviceJob.cancel()
    }

    override fun getApiKey(): String? {
        // 1. Check settings first
        val settingsApiKey = CursorSettingsState.instance.getApiKey()
        if (!settingsApiKey.isNullOrEmpty()) {
            return settingsApiKey.trim()
        }
        
        // 2. Fall back to environment variable OPENAI_API_KEY
        var apiKey: String? = null
        try {
            apiKey = System.getenv("OPENAI_API_KEY")
            if (!apiKey.isNullOrEmpty()) {
                return apiKey.trim()
            }
        } catch (e: NullPointerException) {
            // Ignore and try next method
        }
        return null // No API key found
    }

    override fun sendMessage(
        message: String?,
        context: String,
        action: AnAction,
        callback: CursorAIResponseCallback,
    ) {
        if (message.isNullOrBlank()) return

        // Retrieve API key from multiple possible sources
        val apiKey = getApiKey()
        if (apiKey.isNullOrEmpty()) {
            val errorMessage =
                "OpenAI API key not found. Please configure it in:\n" +
                    "1. Plugin Settings: File → Settings → Cursor AI\n" +
                    "2. Or set environment variable: export OPENAI_API_KEY=your_key_here\n" +
                    "For more details, see the plugin documentation."
            callback.onError(errorMessage)
            return
        }

        // Use the class-level coroutine scope for the API call
        serviceScope.launch {
            try {
                // Create the request body
                val requestBody =
                    JsonObject().apply {
                        addProperty("model", "gpt-3.5-turbo")
                        addProperty("max_tokens", 1000)
                        addProperty("temperature", 0.7)

                        val messages = JsonArray()
                        val userMessage =
                            JsonObject().apply {
                                addProperty("role", "user")
                                addProperty("content", message)
                            }
                        messages.add(userMessage)
                        add("messages", messages)
                    }

                // Create HTTP request
                val settings = CursorSettingsState.instance
                val apiEndpoint = settings.apiEndpoint
                val timeout = settings.timeoutSeconds.toLong()
                
                val request =
                    HttpRequest
                        .newBuilder()
                        .uri(URI.create("$apiEndpoint/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer $apiKey")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                        .timeout(Duration.ofSeconds(timeout))
                        .build()

                // Make the API call
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() == 200) {
                    val responseJson = gson.fromJson(response.body(), JsonObject::class.java)
                    val choices = responseJson.getAsJsonArray("choices")
                    if (choices.size() > 0) {
                        val firstChoice = choices.get(0).asJsonObject
                        val messageObj = firstChoice.getAsJsonObject("message")
                        val content = messageObj.get("content").asString
                        callback.onSuccess(content)
                    } else {
                        callback.onError("No response content received from OpenAI API")
                    }
                } else {
                    callback.onError("OpenAI API error: ${response.statusCode()} - ${response.body()}")
                }
            } catch (e: Exception) {
                callback.onError("Error communicating with OpenAI API: ${e.message}")
            }
        }
    }
}
