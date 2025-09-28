package io.threethirtythree.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

/**
 * IntelliJ IDEA action that opens the Cursor AI tool window for interactive chat.
 *
 * <p>This action provides users with access to a dedicated tool window where they can
 * interact with the Cursor AI service through a chat-like interface. It serves as the
 * main entry point for extended AI conversations and assistance within the IDE.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Opens or activates the Cursor AI tool window</li>
 *   <li>Provides persistent access to AI chat functionality</li>
 *   <li>Integrates with IntelliJ's tool window system</li>
 *   <li>Allows for extended conversations and context retention</li>
 *   <li>Supports multiple concurrent AI assistance sessions</li>
 * </ul>
 *
 * <p>User Interaction:</p>
 * <p>Users can access this functionality through:</p>
 * <ul>
 *   <li>Main menu actions</li>
 *   <li>Toolbar buttons</li>
 *   <li>Keyboard shortcuts</li>
 *   <li>Context menus</li>
 * </ul>
 *
 * <p>The tool window provides a more comprehensive interface compared to the simple
 * dialog-based interactions of other actions, allowing for:</p>
 * <ul>
 *   <li>Multi-turn conversations</li>
 *   <li>Code sharing and discussion</li>
 *   <li>Project-wide analysis and suggestions</li>
 *   <li>Persistent chat history</li>
 * </ul>
 *
 * @author Cursor AI Plugin Team
 * @version 0.5.0
 * @since 1.0
 * @see CursorToolWindowFactory
 * @see CursorChatPanel
 * @see com.intellij.openapi.actionSystem.AnAction
 */
class OpenCursorAIAction : AnAction() {
    @Throws(IllegalStateException::class)
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Cursor AI")
        try {
            toolWindow?.show(null) ?: run {
                println("Cursor AI tool window not found.")
                throw IllegalStateException("Cursor AI tool window not found.")
            }
        } catch (ex: Exception) {
            // Log or handle the exception as needed
            println("Error showing Cursor AI tool window: ${ex.message}")
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
