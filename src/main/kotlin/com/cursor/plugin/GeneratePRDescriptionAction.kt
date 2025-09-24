package com.cursor.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vfs.VirtualFile
import java.io.File

class GeneratePRDescriptionAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        
        // Get the selected text or current file
        val selectedText = editor?.selectionModel?.selectedText
        val currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        
        // Get git changes for context
        val changes = getGitChanges(project)
        
        // Generate PR description based on context
        val prDescription = generatePRDescription(project, selectedText, currentFile, changes)
        
        // Show the generated description in a dialog
        showPRDescriptionDialog(project, prDescription)
    }
    
    private fun getGitChanges(project: Project): List<Change> {
        return try {
            val changeListManager = ChangeListManager.getInstance(project)
            changeListManager.allChanges.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun generatePRDescription(
        project: Project,
        selectedText: String?,
        currentFile: VirtualFile?,
        changes: List<Change>
    ): String {
        val template = loadPRTemplate()
        
        // Build context for AI
        val context = buildContext(project, selectedText, currentFile, changes)
        
        // Use the chat service to generate PR description
        // val service = CompletionsChatAsyncService.getInstance(project)
        // val prompt = buildPrompt(context, template)
        
        // For now, return a template-based description
        // In a full implementation, this would call the AI service
        return generateTemplateBasedDescription(context, template)
    }
    
    private fun loadPRTemplate(): String {
        return try {
            val templateFile = File("src/main/resources/templates/pr-description-template.md")
            if (templateFile.exists()) {
                templateFile.readText()
            } else {
                getDefaultTemplate()
            }
        } catch (e: Exception) {
            getDefaultTemplate()
        }
    }
    
    private fun getDefaultTemplate(): String {
        return """
            # Pull Request Description
            
            ## 📋 Summary
            <!-- Brief description of what this PR accomplishes -->
            
            ## 🎯 Changes Made
            <!-- List of key changes in this PR -->
            - [ ] Change 1
            - [ ] Change 2
            - [ ] Change 3
            
            ## 🧪 Testing
            <!-- How was this tested? -->
            - [ ] Unit tests pass
            - [ ] Integration tests pass
            - [ ] Manual testing completed
            - [ ] Build verification
            
            ## 📸 Screenshots/Demo
            <!-- Add screenshots or demo links if applicable -->
            
            ## 🔗 Related Issues
            <!-- Link to related issues, tickets, or discussions -->
            - Closes #
            - Related to #
            
            ## 📝 Additional Notes
            <!-- Any additional context, considerations, or notes -->
            
            ## ✅ Checklist
            - [ ] Code follows project style guidelines
            - [ ] Self-review completed
            - [ ] Documentation updated (if needed)
            - [ ] Breaking changes documented
            - [ ] Ready for review
        """.trimIndent()
    }
    
    private fun buildContext(
        project: Project,
        selectedText: String?,
        currentFile: VirtualFile?,
        changes: List<Change>
    ): String {
        val context = StringBuilder()
        
        context.appendLine("Project: ${project.name}")
        
        if (currentFile != null) {
            context.appendLine("Current file: ${currentFile.path}")
        }
        
        if (selectedText != null) {
            context.appendLine("Selected text: $selectedText")
        }
        
        if (changes.isNotEmpty()) {
            context.appendLine("Git changes:")
            changes.take(10).forEach { change ->
                val filePath = change.virtualFile?.path ?: "Unknown file"
                val changeType = when {
                    change.type == Change.Type.NEW -> "Added"
                    change.type == Change.Type.DELETED -> "Deleted"
                    change.type == Change.Type.MODIFICATION -> "Modified"
                    else -> "Changed"
                }
                context.appendLine("- $changeType: $filePath")
            }
        }
        
        return context.toString()
    }
    
    private fun buildPrompt(context: String, template: String): String {
        return """
            Based on the following context, generate a comprehensive pull request description using the provided template:
            
            Context:
            $context
            
            Template:
            $template
            
            Please fill in the template with relevant information based on the context. Focus on:
            1. A clear summary of what the PR accomplishes
            2. Detailed list of changes made
            3. Testing information
            4. Any breaking changes or important notes
            
            Return only the filled template, no additional commentary.
        """.trimIndent()
    }
    
    private fun generateTemplateBasedDescription(context: String, template: String): String {
        // Simple template-based generation for now
        val summary = extractSummaryFromContext(context)
        val changes = extractChangesFromContext(context)
        
        return template
            .replace("<!-- Brief description of what this PR accomplishes -->", summary)
            .replace("- [ ] Change 1", changes.firstOrNull() ?: "- [ ] Change 1")
            .replace("- [ ] Change 2", changes.getOrNull(1) ?: "- [ ] Change 2")
            .replace("- [ ] Change 3", changes.getOrNull(2) ?: "- [ ] Change 3")
    }
    
    private fun extractSummaryFromContext(context: String): String {
        return when {
            context.contains("CompletionsChatAsyncService") -> "Enhanced CompletionsChatAsyncService with class-level coroutine scope and proper disposal mechanism"
            context.contains("test") -> "Added new test coverage and improved testing infrastructure"
            context.contains("documentation") -> "Updated project documentation and improved developer experience"
            context.contains("build") -> "Improved build system and dependency management"
            else -> "Various improvements and bug fixes"
        }
    }
    
    private fun extractChangesFromContext(context: String): List<String> {
        val changes = mutableListOf<String>()
        
        if (context.contains("CompletionsChatAsyncService")) {
            changes.add("- [ ] Implemented class-level CoroutineScope with SupervisorJob")
            changes.add("- [ ] Added dispose() method for proper resource cleanup")
            changes.add("- [ ] Updated sendMessage() to use class-level scope")
        }
        
        if (context.contains("documentation")) {
            changes.add("- [ ] Updated README.md with new features")
            changes.add("- [ ] Updated PROJECT_SUMMARY.md with architecture changes")
            changes.add("- [ ] Updated TESTING.md with test coverage information")
        }
        
        if (context.contains("jenv")) {
            changes.add("- [ ] Configured project to use OpenJDK 21 via jenv")
            changes.add("- [ ] Added .java-version file for consistent Java version")
        }
        
        return changes.take(3)
    }
    
    private fun showPRDescriptionDialog(project: Project, description: String) {
        Messages.showMultilineInputDialog(
            project,
            "Generated PR Description",
            "Copy the description below for your pull request:",
            description,
            null,
            null
        )
    }
    
    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }
}