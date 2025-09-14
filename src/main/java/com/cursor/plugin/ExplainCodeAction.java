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