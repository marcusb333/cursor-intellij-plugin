package com.cursor.plugin

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Core service class that handles communication with the Cursor AI API.
 *
 * <p>This service provides the main interface for sending requests to the Cursor AI API
 * and processing responses. It manages HTTP connections, API authentication, and handles
 * both successful responses and error conditions gracefully.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Asynchronous API communication using OkHttp client</li>
 *   <li>Multiple API key configuration methods (environment variables, system properties)</li>
 *   <li>Robust error handling and user-friendly error messages</li>
 *   <li>JSON request/response processing with Gson</li>
 *   <li>Configurable timeouts for reliable operation</li>
 * </ul>
 *
 * <p>API Key Configuration:</p>
 * <p>The service supports multiple methods for providing the Cursor API key:</p>
 * <ol>
 *   <li>Environment variable: {@code CURSOR_API_KEY}</li>
 *   <li>Environment variable: {@code cursor_api_key}</li>
 *   <li>System property: {@code cursor.api.key}</li>
 * </ol>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * CursorAIService service = CursorAIService.getInstance(project);
 * service.sendMessage("Explain this code", codeContext, new CursorAIResponseCallback() {
 *     public void onSuccess(String response) {
 *         // Handle successful response
 *     }
 *     public void onError(String error) {
 *         // Handle error
 *     }
 * });
 * }</pre>
 *
 * @author Cursor AI Plugin Team
 * @version 0.0.4
 * @since 1.0
 * @see CursorAIResponseCallback
 * @see Service
 */
@Service
class CursorAIService private constructor(
    private val project: Project,
    private val apiUrl: String = CURSOR_API_URL
) {
    companion object {
        private const val CURSOR_API_URL = "https://api.cursor.com/v1/chat/completions"
        private const val CONNECT_TIMEOUT_SECONDS = 30L
        private const val READ_TIMEOUT_SECONDS = 60L
        private const val WRITE_TIMEOUT_SECONDS = 60L
        
        /**
         * Gets the singleton instance of CursorAIService for the given project.
         * 
         * @param project The IntelliJ project instance
         * @return The CursorAIService instance for this project
         */
        fun getInstance(project: Project): CursorAIService {
            return project.getService(CursorAIService::class.java)
        }
        
        /**
         * Creates a test instance of CursorAIService for testing purposes.
         * This method is only intended for use in unit tests.
         * 
         * @param project The IntelliJ project instance
         * @return A new CursorAIService instance for testing
         */
        internal fun createForTesting(project: Project): CursorAIService {
            return CursorAIService(project)
        }
        
        /**
         * Creates a test instance of CursorAIService with a custom API URL for testing.
         * This method is only intended for use in unit tests.
         * 
         * @param project The IntelliJ project instance
         * @param testApiUrl The custom API URL to use for testing
         * @return A new CursorAIService instance for testing
         */
        internal fun createForTesting(project: Project, testApiUrl: String): CursorAIService {
            return CursorAIService(project, testApiUrl)
        }
    }

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    private val gson: Gson = Gson()

    fun sendMessage(message: String, context: String, callback: CursorAIResponseCallback) {
        val apiKey = getApiKey()
        if (apiKey.isNullOrEmpty()) {
            val errorMessage = "Cursor API key not found. Please set it using one of these methods:\n" +
                    "1. System property: -Dcursor.api.key=your_key_here\n" +
                    "2. Environment variable: CURSOR_API_KEY=your_key_here\n" +
                    "3. Environment variable: cursor_api_key=your_key_here\n\n" +
                    "For more details, see the plugin documentation."
            callback.onError(errorMessage)
            return
        }
        
        // Log API key status for debugging (without exposing the actual key)
        println("Cursor Plugin: API key found (length: ${apiKey.length} characters)")

        val requestBody = JsonObject().apply {
            addProperty("model", "gpt-4")
            addProperty("prompt", message)
            addProperty("context", context)
            addProperty("max_tokens", 1000)
            addProperty("temperature", 0.7)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            requestBody.toString()
        )
        
        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("Charset", "utf-8")
            .post(body)
            .build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Network error: ${e.message}")
            }
            
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.onError("API error: ${response.code} ${response.message}")
                    return
                }
                
                try {
                    val responseBody = response.body?.string()
                    val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                    val content = jsonResponse.getAsJsonArray("choices")
                        .get(0)
                        .asJsonObject
                        .getAsJsonObject("message")
                        .get("content")
                        .asString
                    
                    callback.onSuccess(content)
                } catch (e: Exception) {
                    callback.onError("Failed to parse response: ${e.message}")
                }
            }
        })
    }

    internal fun getApiKey(): String? {
        // Try multiple sources for the API key, in order of preference:

        // 1. Environment variable CURSOR_API_KEY
        var apiKey = System.getenv("CURSOR_API_KEY")
        if (!apiKey.isNullOrBlank()) {
            return apiKey.trim()
        }

        // 2. Alternative environment variable (lowercase)
        apiKey = System.getenv("cursor_api_key")
        if (!apiKey.isNullOrBlank()) {
            return apiKey.trim()
        }

        // 3. System property (e.g., -Dcursor.api.key=xxx)
        apiKey = System.getProperty("cursor.api.key")
        if (!apiKey.isNullOrBlank()) {
            return apiKey.trim()
        }

        // 4. TODO: Future implementation - read from IntelliJ settings
        // This would be where we'd read from the plugin's settings panel
        // when that feature is implemented

        return null // No API key found
    }
    
    /**
     * Callback interface for handling responses from the Cursor AI API.
     *
     * <p>This interface defines the contract for handling both successful responses
     * and error conditions when communicating with the Cursor AI service. Implementations
     * should handle both success and error cases appropriately.</p>
     *
     * <p>The callback methods are invoked asynchronously on the HTTP client's thread pool,
     * so implementations may need to switch to the EDT (Event Dispatch Thread) for UI updates.</p>
     *
     * @since 1.0
     * @see CursorAIService#sendMessage(String, String, CursorAIResponseCallback)
     */
    interface CursorAIResponseCallback {
        fun onSuccess(response: String)
        fun onError(error: String)
    }
}