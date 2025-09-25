package com.cursor.plugin.service

import com.cursor.plugin.agent.Agent
import com.cursor.plugin.api.CursorApiService
import com.cursor.plugin.api.KtorClient
import com.cursor.plugin.api.models.ChatRequest
import com.cursor.plugin.util.ServiceHelper.getApiKey
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Service(Service.Level.PROJECT)
class CursorAIService(
    private val project: Project,
) : ChatServiceInterface {
    companion object {
        fun getInstance(project: Project): CursorAIService = project.getService(CursorAIService::class.java)
    }

    // Class-level coroutine scope for managing all async operations
    private val serviceJob = SupervisorJob()
    internal val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val httpClient = KtorClient.createHttpClient()
    private val cursorApiService = CursorApiService(httpClient)

    /**
     * Properly dispose of the service and cancel all running coroutines
     */
    fun dispose() {
        serviceJob.cancel()
    }

    override fun sendMessage(
        message: String?,
        context: String,
        action: AnAction,
        callback: CursorAIResponseCallback,
    ) {
        if (message.isNullOrBlank()) return

        val apiKey = getApiKey()
        if (apiKey.isNullOrEmpty()) {
            callback.onError("API key not configured. Please set your Cursor API key in Settings.")
            return
        }

        // Use the class-level coroutine scope for the API call
        serviceScope.launch {
            try {
                val request =
                    ChatRequest(
                        model = "gpt-4",
                        prompt = message,
                        context = context,
                        maxTokens = 1000,
                        temperature = 0.7,
                    )

                val chatResponse =
                    cursorApiService.sendMessage(
                        request = request,
                    )

                if (chatResponse.choices.isNotEmpty()) {
                    val content =
                        chatResponse.choices
                            .first()
                            .message.content
                    callback.onSuccess(content)
                } else {
                    callback.onError("No response content received from Cursor API")
                }
            } catch (e: Exception) {
                callback.onError("Network error: ${e.message}")
            } finally {
                dispose()
            }
        }
    }

    override fun launchAgent(
        action: AnAction,
        callback: CursorAIResponseCallback,
    ) {
        TODO("Not yet implemented")
    }

    override fun getAgents(
        action: AnAction,
        callback: CursorAIResponseCallback,
    ): List<Agent> {
        TODO("Not yet implemented")
    }

    override fun getAgentById(
        agentId: String,
        callback: CursorAIResponseCallback,
    ): Agent {
        TODO("Not yet implemented")
    }
}
