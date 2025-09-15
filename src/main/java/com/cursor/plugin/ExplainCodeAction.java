package com.cursor.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

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
 * @see CursorAIService
 * @see com.intellij.openapi.actionSystem.AnAction
 */
public class ExplainCodeAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (project == null || editor == null) {
            return;
        }

        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();

        if (selectedText == null || selectedText.trim().isEmpty()) {
            Messages.showWarningDialog(
                    project,
                    "Please select some code to explain.",
                    "No Code Selected"
            );
            return;
        }

        CursorAIService aiService = CursorAIService.getInstance(project);
        String prompt = "Please explain this code:\n\n" + selectedText;
        String context = "Code explanation request";

        aiService.sendMessage(prompt, context, new CursorAIService.CursorAIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                SwingUtilities.invokeLater(() -> {
                    Messages.showMessageDialog(
                            project,
                            response,
                            "Code Explanation",
                            Messages.getInformationIcon()
                    );
                });
            }

            @Override
            public void onError(String error) {
                SwingUtilities.invokeLater(() -> {
                    Messages.showErrorDialog(project, "Error explaining code: " + error, "Error");
                });
            }
        });
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        boolean hasSelection = editor != null &&
                              editor.getSelectionModel().getSelectedText() != null &&
                              !editor.getSelectionModel().getSelectedText().trim().isEmpty();

        e.getPresentation().setEnabled(e.getProject() != null && hasSelection);
    }
}