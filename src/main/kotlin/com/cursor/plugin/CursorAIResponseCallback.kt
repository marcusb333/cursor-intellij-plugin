package com.cursor.plugin

interface CursorAIResponseCallback {
    fun onSuccess(response: String)

    fun onError(error: String)
}
