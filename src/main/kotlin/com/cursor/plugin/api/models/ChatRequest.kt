package com.cursor.plugin.api.models

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("model")
    val model: String,
    
    @SerializedName("prompt")
    val prompt: String? = null,
    
    @SerializedName("context")
    val context: String? = null,
    
    @SerializedName("max_tokens")
    val maxTokens: Int,
    
    @SerializedName("temperature")
    val temperature: Double,
    
    @SerializedName("messages")
    val messages: List<ChatMessage>? = null
)

data class ChatMessage(
    @SerializedName("role")
    val role: String,
    
    @SerializedName("content")
    val content: String
)