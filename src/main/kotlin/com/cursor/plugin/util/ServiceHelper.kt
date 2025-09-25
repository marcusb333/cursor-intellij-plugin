package com.cursor.plugin.util

private const val CURSOR_API_KEY = "CURSOR_API_KEY"
private const val CURSOR_AGENT_BASE_URL = "https://api.cursor.com/v0/agents"

internal object ServiceHelper {
    fun getEnvVar(name: String): String? = System.getenv(name)

    fun getApiKey(): String? = getEnvVar(CURSOR_API_KEY)

    fun getAgentsUrl() = CURSOR_AGENT_BASE_URL

    fun getAgentFromIdUrl(agentId: String) = "$CURSOR_AGENT_BASE_URL/$agentId"

    fun getAgentConversationsUrl(agentId: String) = "$CURSOR_AGENT_BASE_URL/$agentId/conversation"
}
