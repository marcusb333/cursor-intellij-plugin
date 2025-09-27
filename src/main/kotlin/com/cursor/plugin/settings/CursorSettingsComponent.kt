package com.cursor.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent

/**
 * UI component for Cursor plugin settings
 */
class CursorSettingsComponent {
    private val panel: JPanel
    private val apiKeyField = JBPasswordField()
    private val showApiKeyCheckbox = JCheckBox("Show")
    private val testConnectionButton = JButton("Test Connection")
    private val connectionStatusLabel = JBLabel()
    private val connectionDetailsArea = JTextArea(3, 0)
    private val apiEndpointField = JBTextField()
    private val timeoutField = JBTextField()
    private val resetButton = JButton("Reset to Defaults")

    private var isTestingConnection = false
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    init {
        panel = createPanel()
        setupListeners()
        loadCurrentSettings()
    }

    fun getPanel(): JComponent = panel

    fun isModified(): Boolean {
        val settings = CursorSettingsState.instance
        return settings.getApiKey() != String(apiKeyField.password) ||
            settings.apiEndpoint != apiEndpointField.text ||
            settings.timeoutSeconds != timeoutField.text.toIntOrNull()
    }

    fun apply() {
        val settings = CursorSettingsState.instance
        settings.setApiKey(String(apiKeyField.password))
        settings.apiEndpoint = apiEndpointField.text
        settings.timeoutSeconds = timeoutField.text.toIntOrNull() ?: CursorSettingsState.DEFAULT_TIMEOUT_SECONDS
    }

    fun reset() {
        loadCurrentSettings()
    }

    private fun createPanel(): JPanel {
        // API Key section
        val apiKeyPanel =
            JPanel(BorderLayout()).apply {
                add(apiKeyField, BorderLayout.CENTER)
                add(showApiKeyCheckbox, BorderLayout.EAST)
            }

        // Connection test section
        val connectionTestPanel =
            JPanel(BorderLayout()).apply {
                add(testConnectionButton, BorderLayout.WEST)
                add(connectionStatusLabel, BorderLayout.CENTER)
            }

        // Connection details area
        connectionDetailsArea.apply {
            isEditable = false
            font = UIUtil.getLabelFont()
            background = UIUtil.getPanelBackground()
            border = JBUI.Borders.empty()
        }

        // Advanced settings panel
        val advancedPanel =
            JPanel(GridBagLayout()).apply {
                border = JBUI.Borders.empty()
                val gbc =
                    GridBagConstraints().apply {
                        gridx = 0
                        gridy = 0
                        anchor = GridBagConstraints.WEST
                        insets = JBUI.insets(5)
                    }

                // API Endpoint
                add(JBLabel("API Endpoint:"), gbc)
                gbc.gridx = 1
                gbc.fill = GridBagConstraints.HORIZONTAL
                gbc.weightx = 1.0
                add(apiEndpointField, gbc)

                // Timeout
                gbc.gridx = 0
                gbc.gridy = 1
                gbc.fill = GridBagConstraints.NONE
                gbc.weightx = 0.0
                add(JBLabel("Timeout (seconds):"), gbc)
                gbc.gridx = 1
                gbc.fill = GridBagConstraints.HORIZONTAL
                gbc.weightx = 1.0
                add(timeoutField, gbc)
            }

        // Main form
        return FormBuilder
            .createFormBuilder()
            .addLabeledComponent(JBLabel("API Key:"), apiKeyPanel, 1, false)
            .addComponent(connectionTestPanel)
            .addComponent(connectionDetailsArea)
            .addSeparator()
            .addComponent(
                JBLabel("Advanced Settings").apply {
                    font = font.deriveFont(Font.BOLD)
                },
            ).addComponent(advancedPanel)
            .addComponentFillVertically(JPanel(), 0)
            .addComponent(resetButton)
            .panel
    }

    private fun setupListeners() {
        // Show/Hide API Key
        showApiKeyCheckbox.addActionListener {
            val echoChar = if (showApiKeyCheckbox.isSelected) 0.toChar() else '•'
            apiKeyField.echoChar = echoChar
        }

        // Test Connection button
        testConnectionButton.addActionListener { e ->
            testConnection()
        }

        // Reset button
        resetButton.addActionListener {
            val result =
                JOptionPane.showConfirmDialog(
                    panel,
                    "Are you sure you want to reset all settings to defaults?",
                    "Reset Settings",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                )
            if (result == JOptionPane.YES_OPTION) {
                CursorSettingsState.instance.resetToDefaults()
                loadCurrentSettings()
            }
        }

        // Validate timeout field
        timeoutField.document.addDocumentListener(
            object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    validateTimeoutField()
                }
            },
        )
    }

    private fun loadCurrentSettings() {
        val settings = CursorSettingsState.instance
        apiKeyField.text = settings.getApiKey() ?: ""
        apiEndpointField.text = settings.apiEndpoint
        timeoutField.text = settings.timeoutSeconds.toString()

        // Update connection status if available
        if (settings.lastConnectionTestTime > 0) {
            updateConnectionStatus(
                settings.lastConnectionTestSuccess,
                settings.lastConnectionTestMessage,
                settings.lastConnectionTestTime,
            )
        }
    }

    private fun testConnection() {
        if (isTestingConnection) return

        isTestingConnection = true
        testConnectionButton.isEnabled = false
        connectionStatusLabel.text = "Testing connection..."
        connectionStatusLabel.foreground = UIUtil.getLabelForeground()
        connectionDetailsArea.text = ""

        // Apply current settings temporarily for testing
        val tempApiKey = String(apiKeyField.password)
        val tempEndpoint = apiEndpointField.text
        val tempTimeout = timeoutField.text.toIntOrNull() ?: CursorSettingsState.DEFAULT_TIMEOUT_SECONDS

        ApplicationManager.getApplication().executeOnPooledThread {
            val testService = ApiConnectionTestService()
            val result = testService.testConnection(tempApiKey, tempEndpoint, tempTimeout)

            SwingUtilities.invokeLater {
                isTestingConnection = false
                testConnectionButton.isEnabled = true

                val settings = CursorSettingsState.instance
                settings.updateConnectionTestResults(result.success, result.message)

                updateConnectionStatus(result.success, result.message, System.currentTimeMillis())

                if (result.success) {
                    connectionDetailsArea.text =
                        buildString {
                            append("Response time: ${result.responseTimeMs}ms\n")
                            if (result.model != null) {
                                append("Model: ${result.model}\n")
                            }
                            if (result.additionalInfo != null) {
                                append(result.additionalInfo)
                            }
                        }
                } else {
                    connectionDetailsArea.text = result.errorDetails ?: ""
                }
            }
        }
    }

    private fun updateConnectionStatus(
        success: Boolean,
        message: String,
        timestamp: Long,
    ) {
        connectionStatusLabel.text =
            if (success) {
                "✓ $message"
            } else {
                "✗ $message"
            }
        connectionStatusLabel.foreground =
            if (success) {
                Color(0, 128, 0) // Green
            } else {
                Color(200, 0, 0) // Red
            }

        if (timestamp > 0) {
            connectionStatusLabel.toolTipText = "Last tested: ${dateFormat.format(Date(timestamp))}"
        }
    }

    private fun validateTimeoutField(): Boolean {
        val text = timeoutField.text
        val timeout = text.toIntOrNull()
        return when {
            text.isBlank() -> {
                timeoutField.putClientProperty("JComponent.outline", null)
                true
            }
            timeout == null || timeout < 1 || timeout > 300 -> {
                timeoutField.putClientProperty("JComponent.outline", "error")
                false
            }
            else -> {
                timeoutField.putClientProperty("JComponent.outline", null)
                true
            }
        }
    }
}
