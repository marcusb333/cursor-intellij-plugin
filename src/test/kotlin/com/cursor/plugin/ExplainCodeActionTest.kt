package com.cursor.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
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
 * Test class for [ExplainCodeAction].
 * 
 * This class contains unit tests for the ExplainCodeAction, covering:
 * <ul>
 *   <li>Code explanation with valid text selection</li>
 *   <li>Handling of no text selection scenarios</li>
 *   <li>Action update behavior based on selection availability</li>
 *   <li>Integration with CursorAIService for code explanation</li>
 * </ul>
 * 
 * Tests verify that the action properly validates text selection and
 * provides appropriate user feedback when no code is selected.
 * 
 * @author Cursor Plugin Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
class ExplainCodeActionTest {

    @Mock
    private lateinit var mockProject: Project
    @Mock
    private lateinit var mockEditor: Editor
    @Mock
    private lateinit var mockSelectionModel: SelectionModel
    @Mock
    private lateinit var mockPresentation: Presentation
    @Mock
    private lateinit var mockAiService: CursorAIService

    private lateinit var action: ExplainCodeAction

    @BeforeEach
    fun setUp() {
        action = ExplainCodeAction()
        lenient().`when`(mockEditor.selectionModel).thenReturn(mockSelectionModel)
        lenient().`when`(mockProject.getService(CursorAIService::class.java)).thenReturn(mockAiService)
    }

    @Test
    fun testActionPerformedWithValidSelection() {
        // Given
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        `when`(mockSelectionModel.selectedText).thenReturn("public class Test { }")

        mockStatic(Messages::class.java).use { messagesMock ->
            // When
            action.actionPerformed(event)

            // Then
            messagesMock.verify({
                Messages.showWarningDialog(
                    eq(mockProject),
                    eq("Please select some code to explain."),
                    eq("No Code Selected")
                )
            }, never())
        }
    }

    @Test
    fun testActionPerformedWithNoSelection() {
        // Given
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        `when`(mockSelectionModel.selectedText).thenReturn(null)

        mockStatic(Messages::class.java).use { messagesMock ->
            // When
            action.actionPerformed(event)

            // Then
            messagesMock.verify({
                Messages.showWarningDialog(
                    eq(mockProject),
                    eq("Please select some code to explain."),
                    eq("No Code Selected")
                )
            })
        }
    }

    @Test
    fun testUpdateWithValidSelection() {
        // Given
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        `when`(event.presentation).thenReturn(mockPresentation)
        `when`(mockSelectionModel.selectedText).thenReturn("public class Test { }")

        // When
        action.update(event)

        // Then
        verify(mockPresentation).isEnabled = true
    }

    @Test
    fun testUpdateWithNoSelection() {
        // Given
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        `when`(event.presentation).thenReturn(mockPresentation)
        `when`(mockSelectionModel.selectedText).thenReturn(null)

        // When
        action.update(event)

        // Then
        verify(mockPresentation).isEnabled = false
    }
}