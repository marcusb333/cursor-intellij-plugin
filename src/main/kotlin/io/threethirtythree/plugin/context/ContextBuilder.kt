package io.threethirtythree.plugin.context

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * Builds rich context about the current editor state for AI requests.
 *
 * <p>Includes file path, language, open files, and optionally symbol at cursor
 * to provide better context for the AI.</p>
 */
object ContextBuilder {

    private const val MAX_CONTEXT_CHARS = 2000
    private const val MAX_OPEN_FILES = 5

    fun buildContext(project: Project): String {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor == null) {
            return buildMinimalContext(project)
        }

        val parts = mutableListOf<String>()

        // Current file info
        val virtualFile = FileDocumentManager.getInstance().getFile(editor.document)
        if (virtualFile != null) {
            parts.add("Current file: ${virtualFile.path}")
            val fileType = virtualFile.fileType
            if (fileType.name != "Unknown") {
                parts.add("Language: ${fileType.name}")
            }
        }

        // Open files
        val openFiles = FileEditorManager.getInstance(project).openFiles
        if (openFiles.isNotEmpty()) {
            val fileList = openFiles.take(MAX_OPEN_FILES).joinToString(", ") { it.name }
            parts.add("Open files: $fileList")
        }

        // Selection or cursor context
        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText

        if (!selectedText.isNullOrBlank()) {
            parts.add("")
            parts.add("Selected code:")
            parts.add(truncateIfNeeded(selectedText, MAX_CONTEXT_CHARS))
        } else {
            // Context around cursor
            val offset = editor.caretModel.offset
            val documentText = editor.document.text

            // Try to get symbol at cursor (method/class name)
            val symbolAtCursor = getSymbolAtCursor(project, virtualFile, offset)
            if (symbolAtCursor != null) {
                parts.add("")
                parts.add("Symbol at cursor: $symbolAtCursor")
            }

            val start = maxOf(0, offset - 500)
            val end = minOf(documentText.length, offset + 500)
            val context = documentText.substring(start, end)

            parts.add("")
            parts.add("Code around cursor:")
            parts.add(truncateIfNeeded(context, MAX_CONTEXT_CHARS))
        }

        return parts.joinToString("\n")
    }

    private fun buildMinimalContext(project: Project): String {
        val openFiles = FileEditorManager.getInstance(project).openFiles
        return if (openFiles.isNotEmpty()) {
            "No file currently focused. Open files: ${openFiles.take(MAX_OPEN_FILES).joinToString(", ") { it.name }}"
        } else {
            "No file currently open"
        }
    }

    private fun getSymbolAtCursor(project: Project, virtualFile: VirtualFile?, offset: Int): String? {
        if (virtualFile == null) return null
        return try {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return null
            val element = psiFile.findElementAt(offset) ?: return null
            val named = PsiTreeUtil.getParentOfType(element, PsiNamedElement::class.java)
            named?.name
        } catch (e: Exception) {
            null
        }
    }

    private fun truncateIfNeeded(text: String, maxChars: Int): String {
        return if (text.length <= maxChars) text
        else text.take(maxChars) + "\n... (truncated)"
    }
}
