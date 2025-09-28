package io.threethirtythree.plugin.service

import io.threethirtythree.plugin.settings.CursorSettingsState
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.http.HttpTimeoutException

@Service(Service.Level.PROJECT)
class CompletionsChatAsyncService(
    val project: Project,
) : io.threethirtythree.plugin.core.ChatServiceInterface {
    companion object {
        private val LOG = Logger.getInstance(CompletionsChatAsyncService::class.java)
        
        fun getInstance(project: Project): CompletionsChatAsyncService = project.getService(CompletionsChatAsyncService::class.java)
    }
    
    // Class-level coroutine scope for managing all async operations
    private val serviceJob = SupervisorJob()
    internal val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val httpClient: HttpClient by lazy {
        val timeout = CursorSettingsState.instance.timeoutSeconds.toLong()
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(timeout))
            .build()
    }

    private val gson: Gson by lazy {
        Gson()
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

        // 2. Fall back to environment variable CURSOR_API_KEY
        try {
            val envApiKey = System.getenv("CURSOR_API_KEY")
            if (!envApiKey.isNullOrEmpty()) {
                LOG.debug("Using API key from environment variable")
                return envApiKey.trim()
            }
        } catch (e: SecurityException) {
            LOG.warn("Security exception when accessing environment variable CURSOR_API_KEY", e)
        } catch (e: Exception) {
            LOG.warn("Unexpected error accessing environment variable CURSOR_API_KEY", e)
        }
        return null // No API key found
    }

    override fun sendMessage(
        message: String?,
        context: String,
        action: AnAction,
        callback: io.threethirtythree.plugin.core.CursorAIResponseCallback,
    ) {
        if (message.isNullOrBlank()) return

        // Retrieve API key from multiple possible sources
        val apiKey = getApiKey()
        if (apiKey.isNullOrEmpty()) {
            LOG.warn("No API key found for Cursor API request")
            val errorMessage =
                "Cursor API key not found. Please configure it in:\n" +
                    "1. Plugin Settings: File → Settings → Cursor AI\n" +
                    "2. Or set environment variable: export CURSOR_API_KEY=your_key_here\n" +
                    "For more details, see the plugin documentation."
            callback.onError(errorMessage)
            return
        }
        
        LOG.debug("Sending message to Cursor API: ${message?.take(100)}...")

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
                        callback.onError("No response content received from Cursor API")
                    }
                } else {
                    val errorBody = response.body()
                    LOG.error("Cursor API returned error status ${response.statusCode()}: $errorBody")
                    callback.onError("Cursor API error (${response.statusCode()}): ${errorBody.take(200)}")
                }
            } catch (e: ConnectException) {
                LOG.error("Failed to connect to Cursor API", e)
                callback.onError("Unable to connect to Cursor API. Please check your internet connection and try again.")
            } catch (e: SocketTimeoutException) {
                LOG.error("Request timeout when calling Cursor API", e)
                callback.onError("Request timed out. The API may be slow or unavailable. Please try again.")
            } catch (e: HttpTimeoutException) {
                LOG.error("HTTP timeout when calling Cursor API", e)
                callback.onError("Request timed out. Please check your connection and try again.")
            } catch (e: SecurityException) {
                LOG.error("Security exception when calling Cursor API", e)
                callback.onError("Security error occurred. Please check your API key and permissions.")
            } catch (e: Exception) {
                LOG.error("Unexpected error communicating with Cursor API", e)
                callback.onError("Unexpected error: ${e.message ?: "Unknown error occurred"}")
            }
        }
    }
}
