package com.cursor.plugin.service

import com.cursor.plugin.agent.Agent
import com.intellij.openapi.actionSystem.AnAction

interface ChatServiceInterface {
    fun launchAgentMessage(
        action: AnAction,
        callback: CursorAIResponseCallback,
    )

    fun followUpAgentMessage(
        action: AnAction,
        callback: CursorAIResponseCallback,
    )

    fun getAgents(
        action: AnAction,
        callback: CursorAIResponseCallback,
    ): List<Agent>

    fun getAgentById(
        agentId: String,
        callback: CursorAIResponseCallback,
    ): Agent

    fun sendMessage(
        message: String?,
        context: String,
        action: AnAction,
        callback: CursorAIResponseCallback,
    )
}
