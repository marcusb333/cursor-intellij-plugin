package com.cursor.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.CaretModelImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.junit.jupiter.MockitoExtension

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*

/**
 * Test class for [GenerateCodeAction].
 * 
 * This class contains unit tests for the GenerateCodeAction, covering:
 * <ul>
 *   <li>Code generation with valid user input</li>
 *   <li>Handling of cancelled user input</li>
 *   <li>Action update behavior based on project and editor availability</li>
 *   <li>Integration with CursorAIService for code generation</li>
 * </ul>
 * 
 * Tests verify that the action properly interacts with the AI service and
 * handles user input scenarios correctly.
 * 
 * @author Cursor Plugin Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
class GenerateCodeActionTest {

    @Mock
    private lateinit var mockProject: Project
    @Mock
    private lateinit var mockEditor: Editor
    @Mock
    private lateinit var mockDocument: Document
    @Mock
    private lateinit var mockCaretModel: CaretModelImpl
    @Mock
    private lateinit var mockPresentation: Presentation
    @Mock
    private lateinit var mockAiService: CursorAIService

    private lateinit var action: GenerateCodeAction

    @BeforeEach
    fun setUp() {
        action = GenerateCodeAction()
        
        lenient().`when`(mockEditor.document).thenReturn(mockDocument)
        lenient().`when`(mockEditor.caretModel).thenReturn(mockCaretModel)
        lenient().`when`(mockCaretModel.offset).thenReturn(10)
        lenient().`when`(mockProject.getService(CursorAIService::class.java)).thenReturn(mockAiService)
    }

    @Test
    fun testActionPerformedWithValidInput() {
        // Given
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)

        mockStatic(Messages::class.java).use { messagesMock ->
            messagesMock.`when`<String> {
                Messages.showInputDialog(
                    eq(mockProject),
                    eq("Describe the code you want to generate:"),
                    eq("Generate Code with Cursor AI"),
                    any(),
                    eq(""),
                    isNull()
                )
            }.thenReturn("Create a simple calculator")

            // When
            action.actionPerformed(event)

            // Then
            // Verify that the AI service was called
            verify(mockAiService).sendMessage(anyString(), anyString(), any())
        }
    }

    @Test
    fun testActionPerformedWithCancelledInput() {
        // Given
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)

        mockStatic(Messages::class.java).use { messagesMock ->
            messagesMock.`when`<String?> {
                Messages.showInputDialog(
                    eq(mockProject),
                    eq("Describe the code you want to generate:"),
                    eq("Generate Code with Cursor AI"),
                    any(),
                    eq(""),
                    isNull()
                )
            }.thenReturn(null)

            // When
            action.actionPerformed(event)

            // Then
            verify(mockDocument, never()).insertString(anyInt(), anyString())
        }
    }

    @Test
    fun testUpdateWithValidProjectAndEditor() {
        // Given
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        lenient().`when`(event.presentation).thenReturn(mockPresentation)

        // When
        action.update(event)

        // Then
        verify(mockPresentation).isEnabled = true
    }

    @Test
    fun testUpdateWithNullProject() {
        // Given
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(null)
        lenient().`when`(event.presentation).thenReturn(mockPresentation)

        // When
        action.update(event)

        // Then
        verify(mockPresentation).setEnabled(false)
    }
}