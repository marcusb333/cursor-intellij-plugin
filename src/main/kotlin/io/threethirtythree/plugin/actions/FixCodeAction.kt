package io.threethirtythree.plugin.actions

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
 * IntelliJ IDEA action that provides AI-powered bug detection and fix suggestions.
 *
 * <p>This action allows users to select code in the editor and request bug analysis
 * and fix suggestions from the Cursor AI service. It integrates with IntelliJ's
 * action system and appears in the context menu when code is selected.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Identifies potential bugs in selected code</li>
 *   <li>Provides suggested fixes from the Cursor AI service</li>
 *   <li>User-friendly error handling and feedback</li>
 *   <li>Seamless integration with IntelliJ's editor interface</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <p>Users can access this action by:</p>
 * <ol>
 *   <li>Selecting code in the editor</li>
 *   <li>Right-clicking to open the context menu</li>
 *   <li>Choosing the "Fix Code with Cursor" option</li>
 * </ol>
 *
 * @author Marcus Bowden
 * @version 0.6.0
 * @since 1.0
 * @see CompletionsChatAsyncService
 * @see com.intellij.openapi.actionSystem.AnAction
 */
class FixCodeAction : AnAction() {
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
                "Please select some code to analyze for bugs.",
                "No Code Selected",
            )
            return
        }

        val aiService = io.threethirtythree.plugin.service.CompletionsChatAsyncService.getInstance(project)
        val prompt = "Identify bugs and suggest fixes for this code:\n\n$selectedText"
        val context = "Bug detection and fix request"

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Analyzing code for bugs...", true) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        aiService.sendMessage(
                            message = prompt,
                            context,
                            action = this@FixCodeAction,
                            callback =
                                object : io.threethirtythree.plugin.core.CursorAIResponseCallback {
                                    override fun onSuccess(response: String) {
                                        SwingUtilities.invokeLater {
                                            Messages.showMessageDialog(
                                                project,
                                                response,
                                                "Bug Detection & Fix Suggestions",
                                                Messages.getInformationIcon(),
                                            )
                                        }
                                    }

                                    override fun onError(error: String) {
                                        SwingUtilities.invokeLater {
                                            Messages.showErrorDialog(
                                                project,
                                                "Error analyzing code: $error",
                                                "Error",
                                            )
                                        }
                                    }
                                },
                        )
                    } catch (e: Exception) {
                        SwingUtilities.invokeLater {
                            Messages.showErrorDialog(
                                project,
                                "Error analyzing code: ${e.message}",
                                "Error",
                            )
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
