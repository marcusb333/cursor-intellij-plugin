package com.cursor.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.CaretModelImpl;
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
class GenerateCodeActionTest {

    @Mock
    private Project mockProject;
    @Mock
    private Editor mockEditor;
    @Mock
    private Document mockDocument;
    @Mock
    private CaretModelImpl mockCaretModel;
    @Mock
    private Presentation mockPresentation;
    @Mock
    private CursorAIService mockAiService;

    private GenerateCodeAction action;

    @BeforeEach
    void setUp() throws Exception {
        action = new GenerateCodeAction();
        
        lenient().when(mockEditor.getDocument()).thenReturn(mockDocument);
        lenient().when(mockEditor.getCaretModel()).thenReturn(mockCaretModel);
        lenient().when(mockCaretModel.getOffset()).thenReturn(10);
        lenient().when(mockProject.getService(CursorAIService.class)).thenReturn(mockAiService);
    }

    @Test
    void testActionPerformedWithValidInput() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor);

        try (MockedStatic<Messages> messagesMock = mockStatic(Messages.class)) {
            messagesMock.when(() -> Messages.showInputDialog(
                    eq(mockProject),
                    eq("Describe the code you want to generate:"),
                    eq("Generate Code with Cursor AI"),
                    any(),
                    eq(""),
                    isNull()
            )).thenReturn("Create a simple calculator");

            // When
            action.actionPerformed(event);

            // Then
            // Verify that the AI service was called
            verify(mockAiService).sendMessage(eq("Create a simple calculator"), anyString(), any());
        }
    }

    @Test
    void testActionPerformedWithCancelledInput() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor);

        try (MockedStatic<Messages> messagesMock = mockStatic(Messages.class)) {
            messagesMock.when(() -> Messages.showInputDialog(
                    eq(mockProject),
                    eq("Describe the code you want to generate:"),
                    eq("Generate Code with Cursor AI"),
                    any(),
                    eq(""),
                    isNull()
            )).thenReturn(null);

            // When
            action.actionPerformed(event);

            // Then
            verify(mockDocument, never()).insertString(anyInt(), anyString());
        }
    }

    @Test
    void testUpdateWithValidProjectAndEditor() {
        // Given
        AnActionEvent event = mock(AnActionEvent.class);
        when(event.getProject()).thenReturn(mockProject);
        when(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor);
        lenient().when(event.getPresentation()).thenReturn(mockPresentation);

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
        lenient().when(event.getPresentation()).thenReturn(mockPresentation);

        // When
        action.update(event);

        // Then
        verify(mockPresentation).setEnabled(false);
    }
}