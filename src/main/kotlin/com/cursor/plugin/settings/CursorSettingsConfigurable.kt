package com.cursor.plugin.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Configurable implementation for Cursor plugin settings
 */
class CursorSettingsConfigurable : Configurable {
    private var settingsComponent: CursorSettingsComponent? = null
    
    override fun getDisplayName(): String = "Cursor AI"
    
    override fun getHelpTopic(): String? = null
    
    override fun createComponent(): JComponent {
        settingsComponent = CursorSettingsComponent()
        return settingsComponent!!.getPanel()
    }
    
    override fun isModified(): Boolean {
        return settingsComponent?.isModified() ?: false
    }
    
    override fun apply() {
        settingsComponent?.apply()
    }
    
    override fun reset() {
        settingsComponent?.reset()
    }
    
    override fun disposeUIResources() {
        settingsComponent = null
    }
}