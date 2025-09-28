package io.threethirtythree.plugin.core

interface CursorAIResponseCallback {
    fun onSuccess(response: String)

    fun onError(error: String)
}
