package io.threethirtythree.plugin.core

import com.intellij.openapi.actionSystem.AnAction

interface ChatServiceInterface {
    fun getApiKey(): String?

    fun sendMessage(
        message: String?,
        context: String,
        action: AnAction,
        callback: CursorAIResponseCallback,
    )
}
