package com.cursor.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExplainCodeActionTest {

    @Mock
    private Project mockProject;
    @Mock
    private Editor mockEditor;
    @Mock
    private SelectionModel mockSelectionModel;
    @Mock
    private Presentation mockPresentation;
    @Mock
    private CursorAIService mockAiService;

    private ExplainCodeAction action;

    @BeforeEach
    void setUp() {
        action = new ExplainCodeAction();
        lenient().when(mockEditor.getSelectionModel()).thenReturn(mockSelectionModel);
        lenient().when(mockProject.getService(CursorAIService.class)).thenReturn(mockAiService);
    }

    @Test
    void testActionPerformedWithValidSelection() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor);
        when(mockSelectionModel.getSelectedText()).thenReturn("public class Test { }");

        try (MockedStatic<Messages> messagesMock = mockStatic(Messages.class)) {
            // When
            action.actionPerformed(event);

            // Then
            messagesMock.verify(() -> Messages.showWarningDialog(
                    eq(mockProject),
                    eq("Please select some code to explain."),
                    eq("No Code Selected")
            ), never());
        }
    }

    @Test
    void testActionPerformedWithNoSelection() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor);
        when(mockSelectionModel.getSelectedText()).thenReturn(null);

        try (MockedStatic<Messages> messagesMock = mockStatic(Messages.class)) {
            // When
            action.actionPerformed(event);

            // Then
            messagesMock.verify(() -> Messages.showWarningDialog(
                    eq(mockProject),
                    eq("Please select some code to explain."),
                    eq("No Code Selected")
            ));
        }
    }

    @Test
    void testUpdateWithValidSelection() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor);
        when(event.getPresentation()).thenReturn(mockPresentation);
        when(mockSelectionModel.getSelectedText()).thenReturn("public class Test { }");

        // When
        action.update(event);

        // Then
        verify(mockPresentation).setEnabled(true);
    }

    @Test
    void testUpdateWithNoSelection() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor);
        when(event.getPresentation()).thenReturn(mockPresentation);
        when(mockSelectionModel.getSelectedText()).thenReturn(null);

        // When
        action.update(event);

        // Then
        verify(mockPresentation).setEnabled(false);
    }
}