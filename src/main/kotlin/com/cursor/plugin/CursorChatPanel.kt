package com.cursor.plugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI

import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

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
 * <p>The panel manages the communication with {@link CursorAIService} and handles
 * the presentation of both user messages and AI responses in a clear, organized
 * format. It ensures proper threading for UI updates and provides error handling
 * for network and API issues.</p>
 *
 * @author Cursor AI Plugin Team
 * @version 0.0.4
 * @since 1.0
 * @see CursorAIService
 * @see CursorToolWindowFactory
 * @see javax.swing.JPanel
 */
class CursorChatPanel private constructor(private val project: Project) : JPanel() {
    companion object {
        /**
         * Creates a new instance of CursorChatPanel for the given project.
         * 
         * @param project The IntelliJ project instance
         * @return A new CursorChatPanel instance
         */
        fun create(project: Project): CursorChatPanel {
            return CursorChatPanel(project)
        }
    }
    
    private val chatArea: JBTextArea
    private val inputField: JBTextField
    private val sendButton: JButton
    private val clearButton: JButton
    private val aiService: CursorAIService
    
    init {
        this.aiService = CursorAIService.getInstance(project)
        
        layout = BorderLayout()
        border = JBUI.Borders.empty(10)
        
        // Create chat area
        chatArea = JBTextArea().apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            background = Color(45, 45, 45)
            foreground = Color(255, 255, 255)
        }
        
        val scrollPane = JBScrollPane(chatArea).apply {
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }
        
        // Create input panel
        val inputPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(5, 0)
        }
        
        inputField = JBTextField().apply {
            preferredSize = Dimension(0, 30)
            addActionListener(SendMessageAction())
        }
        
        sendButton = JButton("Send").apply {
            preferredSize = Dimension(80, 30)
            addActionListener(SendMessageAction())
        }
        
        clearButton = JButton("Clear").apply {
            preferredSize = Dimension(80, 30)
            addActionListener {
                chatArea.text = ""
                inputField.text = ""
            }
        }
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 0)).apply {
            add(clearButton)
            add(sendButton)
        }
        
        inputPanel.add(inputField, BorderLayout.CENTER)
        inputPanel.add(buttonPanel, BorderLayout.EAST)
        
        // Add components
        add(scrollPane, BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)
        
        // Add welcome message
        appendToChat("🤖 Welcome to Cursor AI Assistant!\n")
        appendToChat("Type your questions or requests below. I can help with:\n")
        appendToChat("• Code generation and suggestions\n")
        appendToChat("• Code explanation and documentation\n")
        appendToChat("• Bug fixes and optimizations\n")
        appendToChat("• General programming questions\n\n")
    }
    
    private fun appendToChat(text: String) {
        SwingUtilities.invokeLater {
            chatArea.append(text)
            chatArea.caretPosition = chatArea.document.length
        }
    }
    
    private fun getCurrentContext(): String {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor == null) {
            return "No file currently open"
        }
        
        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText
        
        if (!selectedText.isNullOrBlank()) {
            return "Selected code:\n$selectedText"
        }
        
        // Get current line or surrounding context
        val offset = editor.caretModel.offset
        val documentText = editor.document.text
        
        // Extract some context around the cursor
        val start = maxOf(0, offset - 200)
        val end = minOf(documentText.length, offset + 200)
        val context = documentText.substring(start, end)
        
        return "Current file context:\n$context"
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
            
            // Get context
            val context = getCurrentContext()
            
            // Send to AI
            appendToChat("🤖 Cursor AI: ")
            aiService.sendMessage(message, context, object : CursorAIService.CursorAIResponseCallback {
                override fun onSuccess(response: String) {
                    appendToChat("$response\n\n")
                }
                
                override fun onError(error: String) {
                    appendToChat("❌ Error: $error\n\n")
                }
            })
        }
    }
}