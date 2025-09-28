package io.threethirtythree.plugin.core

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.jetbrains.annotations.NotNull

/**
 * Main plugin class for the Cursor AI IntelliJ IDEA plugin.
 *
 * <p>This class serves as the entry point for the plugin and implements {@link StartupActivity}
 * to initialize the CompletionsChatAsyncService when a project is opened. It ensures that the
 * {@link CompletionsChatAsyncService} is properly instantiated and ready to handle AI-powered code
 * assistance requests.</p>
 *
 * <p>The plugin provides integration with Cursor AI API to offer features such as:</p>
 * <ul>
 *   <li>Code explanation and analysis</li>
 *   <li>Code generation and completion</li>
 *   <li>AI-powered assistance through a dedicated tool window</li>
 * </ul>
*~
 * @author Marcus Bowden
 * @version 0.6.0
 * @since 1.0
 * @see CompletionsChatAsyncService
 * @see StartupActivity
 */
@Service
class CursorPlugin : StartupActivity {
    override fun runActivity(
        @NotNull project: Project,
    ) {
        // Initialize CompletionsChatAsyncService
        io.threethirtythree.plugin.service.CompletionsChatAsyncService.getInstance(project)
    }
}
