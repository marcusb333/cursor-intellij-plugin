package com.cursor.plugin.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.gson.gson

object KtorClient {
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    fun createHttpClient(): HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                gson {
                    setLenient()
                }
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                requestTimeoutMillis = 60_000
                socketTimeoutMillis = 60_000
            }

            install(Logging) {
                level = LogLevel.BODY
            }

            install(HttpRequestRetry) {
                maxRetries = 3
                retryOnExceptionIf { _, cause ->
                    cause is Exception
                }
            }
        }
}
