package com.cursor.plugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import org.jetbrains.annotations.NotNull

/**
 * Factory class for creating and managing the Cursor AI tool window in IntelliJ IDEA.
 *
 * <p>This factory is responsible for creating the tool window that houses the Cursor AI
 * chat interface. It implements IntelliJ's {@code ToolWindowFactory} interface to integrate
 * seamlessly with the IDE's tool window system.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Creating the tool window content when first accessed</li>
 *   <li>Initializing the chat panel and its components</li>
 *   <li>Setting up the tool window layout and appearance</li>
 *   <li>Managing tool window lifecycle and state</li>
 *   <li>Ensuring proper integration with IntelliJ's UI framework</li>
 * </ul>
 *
 * <p>The factory creates a tool window that contains a {@link CursorChatPanel} which
 * provides the main user interface for interacting with the Cursor AI service. The
 * tool window is designed to be:</p>
 * <ul>
 *   <li>Dockable and resizable within the IDE</li>
 *   <li>Persistent across IDE sessions</li>
 *   <li>Accessible through various IDE mechanisms</li>
 *   <li>Integrated with the project context</li>
 * </ul>
 *
 * <p>Tool Window Features:</p>
 * <ul>
 *   <li>Chat-based interface for AI interactions</li>
 *   <li>Code snippet sharing and analysis</li>
 *   <li>Conversation history management</li>
 *   <li>Context-aware assistance</li>
 * </ul>
 *
 * @author Cursor AI Plugin Team
 * @version 0.5.0
 * @since 1.0
 * @see CursorChatPanel
 * @see OpenCursorAIAction
 * @see com.intellij.openapi.wm.ToolWindowFactory
 */
class CursorToolWindowFactory : ToolWindowFactory {
    
    override fun createToolWindowContent(@NotNull project: Project, @NotNull toolWindow: ToolWindow) {
        val chatPanel = CursorChatPanel.create(project)
        val content = ContentFactory.getInstance().createContent(chatPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}