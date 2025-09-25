package com.cursor.plugin.api

import com.cursor.plugin.api.models.ChatRequest
import com.cursor.plugin.api.models.ChatResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OpenAIApiService(
    private val httpClient: HttpClient,
) {
    suspend fun sendMessage(
        apiKey: String,
        request: ChatRequest,
    ): ChatResponse {
        val response =
            httpClient.post("https://api.openai.com/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                setBody(request)
            }

        return response.body()
    }
}
