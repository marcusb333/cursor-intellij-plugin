package com.cursor.plugin.core

interface CursorAIResponseCallback {
    fun onSuccess(response: String)

    fun onError(error: String)
}
