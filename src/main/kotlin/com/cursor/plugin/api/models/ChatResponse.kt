package com.cursor.plugin.api.models

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @SerializedName("choices")
    val choices: List<Choice>,
    
    @SerializedName("usage")
    val usage: Usage? = null
)

data class Choice(
    @SerializedName("message")
    val message: ChatMessage,
    
    @SerializedName("finish_reason")
    val finishReason: String? = null,
    
    @SerializedName("index")
    val index: Int? = null
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null,
    
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null,
    
    @SerializedName("total_tokens")
    val totalTokens: Int? = null
)