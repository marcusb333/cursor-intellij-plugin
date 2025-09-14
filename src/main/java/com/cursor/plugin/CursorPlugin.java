package com.cursor.plugin;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

@Service
public class CursorPlugin implements StartupActivity {
    
    @Override
    public void runActivity(@NotNull Project project) {
        // Initialize Cursor AI service
        CursorAIService.getInstance(project);
    }
}