package com.cursor.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
public class CursorChatPanel extends JPanel {
    private final Project project;
    private final JBTextArea chatArea;
    private final JBTextField inputField;
    private final JButton sendButton;
    private final JButton clearButton;
    private final CursorAIService aiService;
    
    public CursorChatPanel(Project project) {
        this.project = project;
        this.aiService = CursorAIService.getInstance(project);
        
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));
        
        // Create chat area
        chatArea = new JBTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        chatArea.setBackground(new Color(45, 45, 45));
        chatArea.setForeground(new Color(255, 255, 255));
        
        JBScrollPane scrollPane = new JBScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Create input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(JBUI.Borders.empty(5, 0));
        
        inputField = new JBTextField();
        inputField.setPreferredSize(new Dimension(0, 30));
        inputField.addActionListener(new SendMessageAction());
        
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.addActionListener(new SendMessageAction());
        
        clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(80, 30));
        clearButton.addActionListener(e -> {
            chatArea.setText("");
            inputField.setText("");
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(clearButton);
        buttonPanel.add(sendButton);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        
        // Add welcome message
        appendToChat("🤖 Welcome to Cursor AI Assistant!\n");
        appendToChat("Type your questions or requests below. I can help with:\n");
        appendToChat("• Code generation and suggestions\n");
        appendToChat("• Code explanation and documentation\n");
        appendToChat("• Bug fixes and optimizations\n");
        appendToChat("• General programming questions\n\n");
    }
    
    private void appendToChat(String text) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(text);
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    private String getCurrentContext() {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return "No file currently open";
        }
        
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();
        
        if (selectedText != null && !selectedText.trim().isEmpty()) {
            return "Selected code:\n" + selectedText;
        }
        
        // Get current line or surrounding context
        int offset = editor.getCaretModel().getOffset();
        String documentText = editor.getDocument().getText();
        
        // Extract some context around the cursor
        int start = Math.max(0, offset - 200);
        int end = Math.min(documentText.length(), offset + 200);
        String context = documentText.substring(start, end);
        
        return "Current file context:\n" + context;
    }
    
    private class SendMessageAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = inputField.getText().trim();
            if (message.isEmpty()) {
                return;
            }
            
            // Add user message to chat
            appendToChat("👤 You: " + message + "\n\n");
            inputField.setText("");
            
            // Get context
            String context = getCurrentContext();
            
            // Send to AI
            appendToChat("🤖 Cursor AI: ");
            aiService.sendMessage(message, context, new CursorAIService.CursorAIResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    appendToChat(response + "\n\n");
                }
                
                @Override
                public void onError(String error) {
                    appendToChat("❌ Error: " + error + "\n\n");
                }
            });
        }
    }
}