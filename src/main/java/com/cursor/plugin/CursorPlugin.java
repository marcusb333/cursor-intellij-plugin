package com.cursor.plugin;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * Main plugin class for the Cursor AI IntelliJ IDEA plugin.
 *
 * <p>This class serves as the entry point for the plugin and implements {@link StartupActivity}
 * to initialize the Cursor AI service when a project is opened. It ensures that the
 * {@link CursorAIService} is properly instantiated and ready to handle AI-powered code
 * assistance requests.</p>
 *
 * <p>The plugin provides integration with Cursor AI API to offer features such as:</p>
 * <ul>
 *   <li>Code explanation and analysis</li>
 *   <li>Code generation and completion</li>
 *   <li>AI-powered assistance through a dedicated tool window</li>
 * </ul>
 *
 * @author Cursor AI Plugin Team
 * @version 0.0.4
 * @since 1.0
 * @see CursorAIService
 * @see StartupActivity
 */
@Service
public final class CursorPlugin implements StartupActivity {
    
    @Override
    public void runActivity(@NotNull Project project) {
        // Initialize Cursor AI service
        CursorAIService.getInstance(project);
    }
}