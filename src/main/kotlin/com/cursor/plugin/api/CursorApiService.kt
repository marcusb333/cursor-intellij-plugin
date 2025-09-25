package com.cursor.plugin.api

import com.cursor.plugin.api.models.ChatRequest
import com.cursor.plugin.api.models.ChatResponse
import com.cursor.plugin.util.ServiceHelper.getAgentsUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class CursorApiService(
    private val httpClient: HttpClient,
) {
    suspend fun sendMessage(request: ChatRequest): ChatResponse {
        val response =
            httpClient.post(getAgentsUrl()) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${getAgentsUrl()}")
                setBody(request)
            }

        return response.body()
    }
}
