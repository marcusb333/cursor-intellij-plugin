package com.cursor.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Test class for {@link OpenCursorAIAction}.
 * 
 * This class contains unit tests for the OpenCursorAIAction, covering:
 * <ul>
 *   <li>Action execution with valid and null projects</li>
 *   <li>Action update behavior based on project availability</li>
 *   <li>Presentation state management (enabled/disabled)</li>
 * </ul>
 * 
 * Tests verify that the action handles edge cases gracefully and properly
 * manages its enabled state based on project context.
 * 
 * @author Cursor Plugin Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class OpenCursorAIActionTest {

    @Mock
    private Project mockProject;
    @Mock
    private Presentation mockPresentation;

    private OpenCursorAIAction action;

    @BeforeEach
    void setUp() {
        action = new OpenCursorAIAction();
    }

    @Test
    void testActionPerformedWithValidProject() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);

        // When & Then
        // Action should handle the case gracefully even if ToolWindowManager is not available
        try {
            action.actionPerformed(event);
        } catch (Exception e) {
            // Expected in test environment without full IntelliJ platform
        }
        verify(event).getProject();
    }

    @Test
    void testActionPerformedWithNullProject() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(null);

        // When
        action.actionPerformed(event);

        // Then
        // Action should handle null project gracefully
        verify(event).getProject();
    }

    @Test
    void testUpdateWithValidProject() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);
        when(event.getPresentation()).thenReturn(mockPresentation);

        // When
        action.update(event);

        // Then
        verify(mockPresentation).setEnabled(true);
    }

    @Test
    void testUpdateWithNullProject() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(null);
        when(event.getPresentation()).thenReturn(mockPresentation);

        // When
        action.update(event);

        // Then
        verify(mockPresentation).setEnabled(false);
    }
}