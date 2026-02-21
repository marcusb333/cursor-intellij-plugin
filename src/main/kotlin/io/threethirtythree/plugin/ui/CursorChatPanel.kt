package io.threethirtythree.plugin.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.Gray
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

/**
 * Main UI component for the Cursor AI chat interface within the tool window.
 *
 * <p>This panel provides a comprehensive chat-based interface for users to interact
 * with the Cursor AI service. It extends JPanel to integrate seamlessly with
 * IntelliJ's Swing-based UI framework and provides a rich, interactive experience
 * for AI-assisted development.</p>
 *
 * <p>Core Features:</p>
 * <ul>
 *   <li>Real-time chat interface with the Cursor AI service</li>
 *   <li>Message history and conversation management</li>
 *   <li>Code snippet formatting and syntax highlighting</li>
 *   <li>Copy/paste functionality for code sharing</li>
 *   <li>Responsive layout that adapts to tool window resizing</li>
 * </ul>
 *
 * <p>User Interface Components:</p>
 * <ul>
 *   <li>Message display area with scrollable history</li>
 *   <li>Text input field for user messages</li>
 *   <li>Send button and keyboard shortcuts</li>
 *   <li>Status indicators for API communication</li>
 *   <li>Context menu for additional actions</li>
 * </ul>
 *
 * <p>Interaction Capabilities:</p>
 * <ul>
 *   <li>Multi-turn conversations with context retention</li>
 *   <li>Code analysis and explanation requests</li>
 *   <li>Code generation and refactoring suggestions</li>
 *   <li>Project-specific assistance and guidance</li>
 *   <li>Integration with editor selection and cursor position</li>
 * </ul>
 *
 * <p>The panel manages the communication with {@link CompletionsChatAsyncService} and handles
 * the presentation of both user messages and AI responses in a clear, organized
 * format. It ensures proper threading for UI updates and provides error handling
 * for network and API issues.</p>
 *
 * @author Marcus Bowden
 * @version 0.6.0
 * @since 1.0
 * @see CompletionsChatAsyncService
 * @see CursorToolWindowFactory
 * @see javax.swing.JPanel
 */
class CursorChatPanel private constructor(
    private val project: Project,
) : JPanel() {
    companion object {
        private val LOG = Logger.getInstance(CursorChatPanel::class.java)
        
        /**
         * Creates a new instance of CursorChatPanel for the given project.
         *
         * @param project The IntelliJ project instance
         * @return A new CursorChatPanel instance
         */
        fun create(project: Project): CursorChatPanel = CursorChatPanel(project)
    }

    private val chatArea: JBTextArea
    private val inputField: JBTextField
    private val sendButton: JButton
    private val clearButton: JButton
    private val copyButton: JButton

    // Coroutine scope for managing async operations
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val aiService: io.threethirtythree.plugin.service.CompletionsChatAsyncService
    private val chatHistoryService: io.threethirtythree.plugin.chat.ChatHistoryService

    init {
        this.aiService = io.threethirtythree.plugin.service.CompletionsChatAsyncService.getInstance(project)
        this.chatHistoryService = io.threethirtythree.plugin.chat.ChatHistoryService.getInstance(project)

        layout = BorderLayout()
        border = JBUI.Borders.empty(10)

        // Create chat area
        chatArea =
            JBTextArea().apply {
                isEditable = false
                lineWrap = true
                wrapStyleWord = true
                font = Font(Font.MONOSPACED, Font.PLAIN, 12)
                background = Gray._45
                foreground = Gray._255
            }

        val scrollPane =
            JBScrollPane(chatArea).apply {
                verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            }

        // Create input panel
        val inputPanel =
            JPanel(BorderLayout()).apply {
                border = JBUI.Borders.empty(5, 0)
            }

        inputField =
            JBTextField().apply {
                preferredSize = Dimension(0, 30)
                addActionListener(SendMessageAction())
            }

        sendButton =
            JButton("Send").apply {
                preferredSize = Dimension(80, 30)
                addActionListener(SendMessageAction())
            }

        clearButton =
            JButton("Clear").apply {
                preferredSize = Dimension(80, 30)
                addActionListener {
                    chatArea.text = ""
                    inputField.text = ""
                    chatHistoryService.chatText = ""
                }
            }

        copyButton =
            JButton("Copy").apply {
                preferredSize = Dimension(80, 30)
                toolTipText = "Copy chat content to clipboard"
                addActionListener {
                    val textToCopy = chatArea.selectedText ?: chatArea.text
                    if (textToCopy.isNotEmpty()) {
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(
                            StringSelection(textToCopy),
                            null,
                        )
                    }
                }
            }

        val buttonPanel =
            JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0)).apply {
                add(copyButton)
                add(clearButton)
                add(sendButton)
            }

        inputPanel.add(inputField, BorderLayout.CENTER)
        inputPanel.add(buttonPanel, BorderLayout.EAST)

        // Add components
        add(scrollPane, BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)

        // Load persisted history or show welcome message
        val savedHistory = chatHistoryService.chatText
        if (savedHistory.isNotBlank()) {
            chatArea.text = savedHistory
            chatArea.caretPosition = chatArea.document.length
        } else {
            appendToChat("🤖 Welcome to Cursor AI Assistant!\n")
            appendToChat("Type your questions or requests below. I can help with:\n")
            appendToChat("• Code generation and suggestions\n")
            appendToChat("• Code explanation and documentation\n")
            appendToChat("• Bug fixes and optimizations\n")
            appendToChat("• General programming questions\n\n")
        }
    }

    private fun appendToChat(text: String, persist: Boolean = true) {
        SwingUtilities.invokeLater {
            chatArea.append(text)
            chatArea.caretPosition = chatArea.document.length
            if (persist) {
                chatHistoryService.chatText = chatArea.text
            }
        }
    }

    private inner class SendMessageAction : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            val message = inputField.text.trim()
            if (message.isEmpty()) {
                return
            }

            // Add user message to chat
            appendToChat("👤 You: $message\n\n")
            inputField.text = ""

            // Get rich context and build full prompt
            val context = io.threethirtythree.plugin.context.ContextBuilder.buildContext(project)
            val fullMessage = buildString {
                append("Context:\n")
                append(context)
                append("\n\nUser question: ")
                append(message)
            }

            // Send to AI
            appendToChat("🤖 Cursor AI: ")
            val dummyAction = object : com.intellij.openapi.actionSystem.AnAction() {
                override fun actionPerformed(e: com.intellij.openapi.actionSystem.AnActionEvent) {
                    // This is just a dummy action for the service call
                }
            }
            
            aiService.sendMessageStreaming(
                message = fullMessage,
                context = context,
                action = dummyAction,
                callback = object : io.threethirtythree.plugin.core.CursorAIStreamingCallback {
                    override fun onChunk(chunk: String) {
                        SwingUtilities.invokeLater {
                            appendToChat(chunk, persist = false)
                        }
                    }

                    override fun onSuccess(response: String) {
                        SwingUtilities.invokeLater {
                            appendToChat("\n\n", persist = true)
                        }
                    }

                    override fun onError(error: String) {
                        SwingUtilities.invokeLater {
                            appendToChat("❌ Error: $error\n\n", persist = true)
                        }
                    }
                }
            )
        }
    }

    /**
     * Cleanup method to properly cancel coroutines when the panel is disposed.
     * This prevents memory leaks and ensures proper resource cleanup.
     */
    fun cleanup() {
        try {
            coroutineScope.cancel()
        } catch (e: Exception) {
            // Log but don't throw - cleanup should be safe
            LOG.warn("Error during CursorChatPanel cleanup", e)
        }
    }
    
    /**
     * Override removeNotify to ensure cleanup when component is removed from UI
     */
    override fun removeNotify() {
        super.removeNotify()
        cleanup()
    }
}
