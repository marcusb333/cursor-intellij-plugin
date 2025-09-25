package com.cursor.plugin.actions

import com.cursor.plugin.CompletionsChatAsyncService
import com.cursor.plugin.service.CursorAIResponseCallback
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import org.jetbrains.annotations.NotNull
import javax.swing.SwingUtilities

/**
 * IntelliJ IDEA action that provides AI-powered code explanation functionality.
 *
 * <p>This action allows users to select code in the editor and request an explanation
 * from the Cursor AI service. It integrates with IntelliJ's action system and appears
 * in the context menu when code is selected.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Context-aware code analysis using selected text</li>
 *   <li>Integration with the Cursor AI API for intelligent explanations</li>
 *   <li>User-friendly error handling and feedback</li>
 *   <li>Seamless integration with IntelliJ's editor interface</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <p>Users can access this action by:</p>
 * <ol>
 *   <li>Selecting code in the editor</li>
 *   <li>Right-clicking to open the context menu</li>
 *   <li>Choosing the "Explain Code" option</li>
 * </ol>
 *
 * <p>The action will send the selected code to the Cursor AI service along with
 * relevant context and display the explanation to the user.</p>
 *
 * @author Cursor AI Plugin Team
 * @version 0.0.4
 * @since 1.0
 * @see com.cursor.plugin.CompletionsChatAsyncService
 * @see AnAction
 */
class ExplainCodeAction : AnAction() {
    override fun actionPerformed(
        @NotNull e: AnActionEvent,
    ) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (project == null || editor == null) {
            return
        }

        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText

        if (selectedText.isNullOrBlank()) {
            Messages.showWarningDialog(
                project,
                "Please select some code to explain.",
                "No Code Selected",
            )
            return
        }

        val aiService = CompletionsChatAsyncService.Companion.getInstance(project)
        val prompt = "Please explain this code:\n\n$selectedText"
        val context = "Code explanation request"

        // Use IntelliJ's background task to call the service
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Explaining code...", true) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        aiService.sendMessage(
                            message = prompt,
                            context,
                            action = this@ExplainCodeAction,
                            callback =
                                object : CursorAIResponseCallback {
                                    override fun onSuccess(response: String) {
                                        SwingUtilities.invokeLater {
                                            Messages.showMessageDialog(
                                                project,
                                                response,
                                                "Code Explanation",
                                                Messages.getInformationIcon(),
                                            )
                                        }
                                    }

                                    override fun onError(error: String) {
                                        SwingUtilities.invokeLater {
                                            Messages.showErrorDialog(project, "Error explaining code: $error", "Error")
                                        }
                                    }
                                },
                        )
                    } catch (e: Exception) {
                        SwingUtilities.invokeLater {
                            Messages.showErrorDialog(project, "Error explaining code: ${e.message}", "Error")
                        }
                    }
                }
            },
        )
    }

    override fun update(
        @NotNull e: AnActionEvent,
    ) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection =
            editor != null &&
                !editor.selectionModel.selectedText.isNullOrBlank()

        e.presentation.isEnabled = e.project != null && hasSelection
    }
}
