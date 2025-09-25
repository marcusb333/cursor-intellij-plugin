package com.cursor.plugin.service

interface CursorAIResponseCallback {
    fun onSuccess(response: String)

    fun onError(error: String)
}
