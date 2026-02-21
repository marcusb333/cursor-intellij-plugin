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
 * IntelliJ IDEA action that provides AI-powered code refactoring functionality.
 *
 * <p>This action allows users to select code and request refactoring suggestions
 * from the Cursor AI service. Users can optionally specify a refactoring goal
 * (e.g., "extract method", "simplify", "improve readability").</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Context-aware refactoring using selected code</li>
 *   <li>Optional refactoring goal for targeted improvements</li>
 *   <li>Direct replacement of selected code with refactored version</li>
 *   <li>Integration with the Cursor AI API</li>
 * </ul>
 *
 * @author Marcus Bowden
 * @version 0.6.0
 * @since 1.0
 * @see CompletionsChatAsyncService
 * @see com.intellij.openapi.actionSystem.AnAction
 */
class RefactorCodeAction : AnAction() {
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
                "Please select some code to refactor.",
                "No Code Selected",
            )
            return
        }

        val refactorGoal =
            Messages.showInputDialog(
                project,
                "Describe the refactoring goal (optional, e.g., 'extract method', 'simplify', 'improve readability'):",
                "Refactor Code with Cursor AI",
                Messages.getQuestionIcon(),
                "improve",
                null,
            ) ?: "improve"

        val selectionStart = selectionModel.selectionStart
        val selectionEnd = selectionModel.selectionEnd

        val aiService = io.threethirtythree.plugin.service.CompletionsChatAsyncService.getInstance(project)
        val prompt = buildString {
            append("Refactor this code")
            if (refactorGoal.isNotBlank()) {
                append(" to $refactorGoal")
            }
            append(":\n\n$selectedText")
        }
        val context = "Code refactoring request"

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Refactoring code...", true) {
                override fun run(indicator: ProgressIndicator) {
                    try {
                        aiService.sendMessage(
                            message = prompt,
                            context,
                            action = this@RefactorCodeAction,
                            callback =
                                object : io.threethirtythree.plugin.core.CursorAIResponseCallback {
                                    override fun onSuccess(response: String) {
                                        SwingUtilities.invokeLater {
                                            val result =
                                                Messages.showYesNoDialog(
                                                    project,
                                                    "Refactored code:\n\n$response\n\nDo you want to replace the selected code with this?",
                                                    "Code Refactored",
                                                    Messages.getQuestionIcon(),
                                                )

                                            if (result == Messages.YES) {
                                                editor.document.replaceString(selectionStart, selectionEnd, response)
                                            }
                                        }
                                    }

                                    override fun onError(error: String) {
                                        SwingUtilities.invokeLater {
                                            Messages.showErrorDialog(
                                                project,
                                                "Error refactoring code: $error",
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
                                "Error refactoring code: ${e.message}",
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
