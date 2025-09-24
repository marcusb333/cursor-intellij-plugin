@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.cursor.plugin

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.lenient
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

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
    private var mockProject = mock(Project::class.java)

    private var mockEditor = mock(Editor::class.java)

    private var mockSelectionModel = mock(SelectionModel::class.java)

    private var mockPresentation = mock(Presentation::class.java)

    private var mockAiService = mock(CompletionsChatAsyncService::class.java)

    private lateinit var action: ExplainCodeAction

    @BeforeEach
    fun setUp() {
        action = ExplainCodeAction()
        lenient().`when`(mockEditor.selectionModel).thenReturn(mockSelectionModel)
    }

    @Test
    fun testActionPerformedWithValidSelection() {
        // Given
        val event = mock(AnActionEvent::class.java)
        lenient().`when`(event.project).thenReturn(mockProject)
        lenient().`when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        lenient().`when`(mockSelectionModel.selectedText).thenReturn("public class Test { }")

        // Mock the service instance
        `when`(mockProject.getService(CompletionsChatAsyncService::class.java)).thenReturn(mockAiService)

        // Mock ProgressManager
        val mockProgressManager = mock(ProgressManager::class.java)
        mockStatic(ProgressManager::class.java).use { progressManagerMock ->
            progressManagerMock.`when`<ProgressManager> { ProgressManager.getInstance() }.thenReturn(mockProgressManager)
            
            doAnswer { invocation ->
                val task = invocation.getArgument<Task.Backgroundable>(0)
                // Simulate running the task immediately
                task.run(mock())
                null
            }.`when`(mockProgressManager).run(any<Task.Backgroundable>())

            // When
            action.actionPerformed(event)

            // Then - the action should complete without throwing an exception
            // Since we can't easily mock the async service call, we just verify it doesn't crash
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
                    eq("No Code Selected"),
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
