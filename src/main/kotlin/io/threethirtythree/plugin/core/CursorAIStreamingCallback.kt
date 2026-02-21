package io.threethirtythree.plugin.core

/**
 * Callback interface for streaming AI responses.
 *
 * <p>Extends [CursorAIResponseCallback] to support incremental updates as the AI
 * generates its response. [onChunk] is called for each piece of content as it
 * arrives; [onSuccess] is called with the complete response when streaming finishes.</p>
 */
interface CursorAIStreamingCallback : CursorAIResponseCallback {
    /**
     * Called when a new chunk of the response is received.
     *
     * @param chunk The incremental text content from the AI
     */
    fun onChunk(chunk: String)
}
