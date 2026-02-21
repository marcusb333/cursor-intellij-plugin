package io.threethirtythree.plugin.chat

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Project-level service that persists chat history.
 *
 * <p>Stores the chat display text so conversations are preserved when the
 * Cursor AI panel is closed and reopened.</p>
 */
@Service(Service.Level.PROJECT)
@State(
    name = "CursorChatHistory",
    storages = [Storage("cursorChatHistory.xml")]
)
class ChatHistoryService : PersistentStateComponent<ChatHistoryService> {
    var chatText: String = ""

    override fun getState(): ChatHistoryService = this

    override fun loadState(state: ChatHistoryService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project: Project): ChatHistoryService =
            project.getService(ChatHistoryService::class.java)
    }
}
