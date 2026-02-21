@file:Suppress("ktlint:standard:no-wildcard-imports")

package io.threethirtythree.plugin.actions

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
import org.mockito.Mockito.lenient
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import io.threethirtythree.plugin.service.CompletionsChatAsyncService

@ExtendWith(MockitoExtension::class)
class RefactorCodeActionTest {
    private var mockProject = mock(Project::class.java)

    private var mockEditor = mock(Editor::class.java)

    private var mockSelectionModel = mock(SelectionModel::class.java)

    private var mockPresentation = mock(Presentation::class.java)

    private var mockAiService = mock(CompletionsChatAsyncService::class.java)

    private lateinit var action: RefactorCodeAction

    @BeforeEach
    fun setUp() {
        action = RefactorCodeAction()
        lenient().`when`(mockEditor.selectionModel).thenReturn(mockSelectionModel)
    }

    @Test
    fun testActionPerformedWithValidSelection() {
        val event = mock(AnActionEvent::class.java)
        lenient().`when`(event.project).thenReturn(mockProject)
        lenient().`when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        lenient().`when`(mockSelectionModel.selectedText).thenReturn("int x = 1 + 2;")
        lenient().`when`(mockSelectionModel.selectionStart).thenReturn(0)
        lenient().`when`(mockSelectionModel.selectionEnd).thenReturn(12)

        `when`(mockProject.getService(CompletionsChatAsyncService::class.java)).thenReturn(mockAiService)

        mockStatic(Messages::class.java).use { messagesMock ->
            messagesMock.`when`<String?> {
                Messages.showInputDialog(
                    eq(mockProject),
                    any(),
                    eq("Refactor Code with Cursor AI"),
                    any(),
                    eq("improve"),
                    any(),
                )
            }.thenReturn("simplify")

            val mockProgressManager = mock(ProgressManager::class.java)
            mockStatic(ProgressManager::class.java).use { progressManagerMock ->
                progressManagerMock.`when`<ProgressManager> { ProgressManager.getInstance() }.thenReturn(mockProgressManager)

                doAnswer { invocation ->
                    val task = invocation.getArgument<Task.Backgroundable>(0)
                    task.run(mock())
                    null
                }.`when`(mockProgressManager).run(any<Task.Backgroundable>())

                action.actionPerformed(event)
            }
        }
    }

    @Test
    fun testActionPerformedWithNoSelection() {
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        `when`(mockSelectionModel.selectedText).thenReturn(null)

        mockStatic(Messages::class.java).use { messagesMock ->
            action.actionPerformed(event)

            messagesMock.verify({
                Messages.showWarningDialog(
                    eq(mockProject),
                    eq("Please select some code to refactor."),
                    eq("No Code Selected"),
                )
            })
        }
    }

    @Test
    fun testUpdateWithValidSelection() {
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        `when`(event.presentation).thenReturn(mockPresentation)
        `when`(mockSelectionModel.selectedText).thenReturn("int x = 1 + 2;")

        action.update(event)

        verify(mockPresentation).isEnabled = true
    }

    @Test
    fun testUpdateWithNoSelection() {
        val event = mock(AnActionEvent::class.java)
        `when`(event.project).thenReturn(mockProject)
        `when`(event.getData(CommonDataKeys.EDITOR)).thenReturn(mockEditor)
        `when`(event.presentation).thenReturn(mockPresentation)
        `when`(mockSelectionModel.selectedText).thenReturn(null)

        action.update(event)

        verify(mockPresentation).isEnabled = false
    }
}
