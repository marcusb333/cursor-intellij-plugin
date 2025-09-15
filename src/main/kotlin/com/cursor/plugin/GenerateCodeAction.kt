package com.cursor.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.jetbrains.annotations.NotNull

import javax.swing.*

/**
 * IntelliJ IDEA action that provides AI-powered code generation functionality.
 *
 * <p>This action enables users to generate code using artificial intelligence by providing
 * prompts or descriptions of what they want to create. It integrates seamlessly with
 * IntelliJ's editor and can insert generated code at the current cursor position.</p>
 *
 * <p>Key capabilities:</p>
 * <ul>
 *   <li>Interactive code generation based on natural language prompts</li>
 *   <li>Context-aware generation using surrounding code for better results</li>
 *   <li>Direct insertion of generated code into the editor</li>
 *   <li>Integration with the Cursor AI API for intelligent code completion</li>
 *   <li>Support for various programming languages and frameworks</li>
 * </ul>
 *
 * <p>Workflow:</p>
 * <ol>
 *   <li>User positions cursor where code should be generated</li>
 *   <li>Invokes the "Generate Code" action</li>
 *   <li>Provides a description or prompt for the desired code</li>
 *   <li>AI generates appropriate code based on context and prompt</li>
 *   <li>Generated code is inserted at the cursor position</li>
 * </ol>
 *
 * <p>This action is particularly useful for:</p>
 * <ul>
 *   <li>Boilerplate code generation</li>
 *   <li>Function and method creation</li>
 *   <li>Class and interface scaffolding</li>
 *   <li>Test case generation</li>
 *   <li>Documentation and comment creation</li>
 * </ul>
 *
 * @author Cursor AI Plugin Team
 * @version 0.0.4
 * @since 1.0
 * @see CursorAIService
 * @see com.intellij.openapi.actionSystem.AnAction
 */
class GenerateCodeAction : AnAction() {
    
    override fun actionPerformed(@NotNull e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        
        if (project == null || editor == null) {
            return
        }
        
        val prompt = Messages.showInputDialog(
            project,
            "Describe the code you want to generate:",
            "Generate Code with Cursor AI",
            Messages.getQuestionIcon(),
            "",
            null
        )
        
        if (!prompt.isNullOrBlank()) {
            val aiService = CursorAIService.getInstance(project)
            val context = "Generate code for: $prompt"
            
            aiService.sendMessage(prompt, context, object : CursorAIService.CursorAIResponseCallback {
                override fun onSuccess(response: String) {
                    SwingUtilities.invokeLater {
                        val result = Messages.showYesNoDialog(
                            project,
                            "Generated code:\n\n$response\n\nDo you want to insert this code at the cursor position?",
                            "Code Generated",
                            Messages.getQuestionIcon()
                        )
                        
                        if (result == Messages.YES) {
                            editor.document.insertString(
                                editor.caretModel.offset,
                                response
                            )
                        }
                    }
                }
                
                override fun onError(error: String) {
                    SwingUtilities.invokeLater {
                        Messages.showErrorDialog(project, "Error generating code: $error", "Error")
                    }
                }
            })
        }
    }
    
    override fun update(@NotNull e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null && e.getData(CommonDataKeys.EDITOR) != null
    }
}