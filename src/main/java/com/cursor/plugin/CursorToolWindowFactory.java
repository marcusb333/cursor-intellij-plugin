package com.cursor.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class CursorToolWindowFactory implements ToolWindowFactory {
    
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        CursorChatPanel chatPanel = new CursorChatPanel(project);
        Content content = ContentFactory.getInstance().createContent(chatPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}