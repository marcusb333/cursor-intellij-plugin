package com.cursor.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GenerateCodeAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        
        if (project == null || editor == null) {
            return;
        }
        
        String prompt = Messages.showInputDialog(
                project,
                "Describe the code you want to generate:",
                "Generate Code with Cursor AI",
                Messages.getQuestionIcon(),
                "",
                null
        );
        
        if (prompt != null && !prompt.trim().isEmpty()) {
            CursorAIService aiService = CursorAIService.getInstance(project);
            String context = "Generate code for: " + prompt;
            
            aiService.sendMessage(prompt, context, new CursorAIService.CursorAIResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    SwingUtilities.invokeLater(() -> {
                        int result = Messages.showYesNoDialog(
                                project,
                                "Generated code:\n\n" + response + "\n\nDo you want to insert this code at the cursor position?",
                                "Code Generated",
                                Messages.getQuestionIcon()
                        );
                        
                        if (result == Messages.YES) {
                            editor.getDocument().insertString(
                                    editor.getCaretModel().getOffset(),
                                    response
                            );
                        }
                    });
                }
                
                @Override
                public void onError(String error) {
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog(project, "Error generating code: " + error, "Error");
                    });
                }
            });
        }
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null && e.getData(CommonDataKeys.EDITOR) != null);
    }
}