package io.threethirtythree.plugin.settings

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Service for testing API connections
 */
class ApiConnectionTestService {
    private val gson = Gson()
    
    data class TestResult(
        val success: Boolean,
        val message: String,
        val responseTimeMs: Long = 0,
        val model: String? = null,
        val additionalInfo: String? = null,
        val errorDetails: String? = null
    )
    
    fun testConnection(
        apiKey: String?,
        endpoint: String,
        timeoutSeconds: Int,
        model: String = "gpt-3.5-turbo",
    ): TestResult {
        if (apiKey.isNullOrBlank()) {
            return TestResult(
                success = false,
                message = "API key is not configured",
                errorDetails = "Please enter your Cursor API key to test the connection."
            )
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .build()
            
            // Use a minimal chat completion request to test the Cursor API
            val requestBody = JsonObject().apply {
                addProperty("model", model)
                add("messages", gson.toJsonTree(listOf(
                    mapOf(
                        "role" to "user",
                        "content" to "Test"
                    )
                )))
                addProperty("max_tokens", 1)
            }
            
            val request = Request.Builder()
                .url("$endpoint/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val responseTime = System.currentTimeMillis() - startTime
            
            return when (response.code) {
                200 -> {
                    val responseBody = response.body?.string()
                    val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                    val model = jsonResponse?.get("model")?.asString
                    
                    TestResult(
                        success = true,
                        message = "Connected successfully",
                        responseTimeMs = responseTime,
                        model = model,
                        additionalInfo = "API is working correctly"
                    )
                }
                401 -> TestResult(
                    success = false,
                    message = "Authentication failed",
                    responseTimeMs = responseTime,
                    errorDetails = "Invalid API key. Please check your API key and try again.\n\n" +
                            "Make sure you're using a valid Cursor API key."
                )
                403 -> TestResult(
                    success = false,
                    message = "Access forbidden",
                    responseTimeMs = responseTime,
                    errorDetails = "Your API key doesn't have access to this endpoint.\n\n" +
                            "Please check your Cursor account permissions."
                )
                429 -> TestResult(
                    success = false,
                    message = "Rate limit exceeded",
                    responseTimeMs = responseTime,
                    errorDetails = "You've exceeded your API rate limit.\n\n" +
                            "Please wait a moment and try again, or check your Cursor usage limits."
                )
                404 -> TestResult(
                    success = false,
                    message = "Endpoint not found",
                    responseTimeMs = responseTime,
                    errorDetails = "The API endpoint was not found.\n\n" +
                            "Please check the API endpoint URL in advanced settings."
                )
                else -> {
                    val errorBody = response.body?.string()
                    val errorMessage = try {
                        val errorJson = gson.fromJson(errorBody, JsonObject::class.java)
                        errorJson?.getAsJsonObject("error")?.get("message")?.asString
                    } catch (e: Exception) {
                        null
                    }
                    
                    TestResult(
                        success = false,
                        message = "API error (${response.code})",
                        responseTimeMs = responseTime,
                        errorDetails = errorMessage ?: "An unexpected error occurred. Response code: ${response.code}"
                    )
                }
            }
        } catch (e: Exception) {
            val responseTime = System.currentTimeMillis() - startTime
            
            return when (e) {
                is java.net.SocketTimeoutException -> TestResult(
                    success = false,
                    message = "Connection timed out",
                    responseTimeMs = responseTime,
                    errorDetails = "The connection timed out after $timeoutSeconds seconds.\n\n" +
                            "Try increasing the timeout in advanced settings or check your internet connection."
                )
                is java.net.UnknownHostException -> TestResult(
                    success = false,
                    message = "Cannot resolve host",
                    responseTimeMs = responseTime,
                    errorDetails = "Cannot connect to the API endpoint.\n\n" +
                            "Please check your internet connection and the API endpoint URL."
                )
                is java.net.ConnectException -> TestResult(
                    success = false,
                    message = "Connection failed",
                    responseTimeMs = responseTime,
                    errorDetails = "Failed to establish connection to the API.\n\n" +
                            "Please check your internet connection and firewall settings."
                )
                else -> TestResult(
                    success = false,
                    message = "Unexpected error",
                    responseTimeMs = responseTime,
                    errorDetails = "An unexpected error occurred:\n${e.message}\n\n" +
                            "Please check your settings and try again."
                )
            }
        }
    }
}