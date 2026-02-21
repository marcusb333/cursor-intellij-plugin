package io.threethirtythree.plugin.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent state component for Cursor plugin settings
 */
@State(
    name = "CursorPluginSettings",
    storages = [Storage("CursorPlugin.xml")]
)
@Service
class CursorSettingsState : PersistentStateComponent<CursorSettingsState> {
    
    // Non-sensitive settings stored in XML
    var apiEndpoint: String = DEFAULT_API_ENDPOINT
    var model: String = DEFAULT_MODEL
    var maxTokens: Int = DEFAULT_MAX_TOKENS
    var temperature: Double = DEFAULT_TEMPERATURE
    var timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS
    var lastConnectionTestTime: Long = 0
    var lastConnectionTestSuccess: Boolean = false
    var lastConnectionTestMessage: String = ""
    
    companion object {
        const val DEFAULT_API_ENDPOINT = "https://api.cursor.com/v1"
        const val DEFAULT_MODEL = "gpt-3.5-turbo"
        const val DEFAULT_MAX_TOKENS = 1000
        const val DEFAULT_TEMPERATURE = 0.7
        const val DEFAULT_TIMEOUT_SECONDS = 30

        /** Common Cursor API model IDs (OpenAI-compatible) */
        val AVAILABLE_MODELS: Array<String> = arrayOf(
            "gpt-3.5-turbo",
            "gpt-4",
            "gpt-4-turbo",
            "gpt-4o",
            "gpt-4o-mini",
            "claude-3-5-sonnet-20241022",
            "claude-3-opus-20240229",
        )
        private const val API_KEY_SERVICE_NAME = "CursorPlugin"
        private const val API_KEY_USERNAME = "apiKey"
        
        val instance: CursorSettingsState
            get() = ApplicationManager.getApplication().getService(CursorSettingsState::class.java)
    }
    
    override fun getState(): CursorSettingsState = this
    
    override fun loadState(state: CursorSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    /**
     * Gets the API key from secure storage
     */
    fun getApiKey(): String? {
        return try {
            val credentialAttributes = createCredentialAttributes()
            val credentials = PasswordSafe.instance.get(credentialAttributes)
            credentials?.getPasswordAsString()
        } catch (e: Exception) {
            // Handle case where PasswordSafe is not available (e.g., in tests)
            null
        }
    }

    /**
     * Sets the API key in secure storage
     */
    fun setApiKey(apiKey: String?) {
        try {
            val credentialAttributes = createCredentialAttributes()
            val credentials = if (apiKey.isNullOrBlank()) {
                null
            } else {
                Credentials(API_KEY_USERNAME, apiKey)
            }
            PasswordSafe.instance.set(credentialAttributes, credentials)
        } catch (e: Exception) {
            // Handle case where PasswordSafe is not available (e.g., in tests)
            // Do nothing
        }
    }
    
    /**
     * Checks if an API key is configured
     */
    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }
    
    /**
     * Gets a masked version of the API key for display
     */
    fun getMaskedApiKey(): String {
        val apiKey = getApiKey()
        return if (apiKey.isNullOrBlank()) {
            ""
        } else if (apiKey.length > 8) {
            "*".repeat(apiKey.length - 4) + apiKey.takeLast(4)
        } else {
            "*".repeat(apiKey.length)
        }
    }
    
    /**
     * Resets all settings to defaults
     */
    fun resetToDefaults() {
        apiEndpoint = DEFAULT_API_ENDPOINT
        model = DEFAULT_MODEL
        maxTokens = DEFAULT_MAX_TOKENS
        temperature = DEFAULT_TEMPERATURE
        timeoutSeconds = DEFAULT_TIMEOUT_SECONDS
        lastConnectionTestTime = 0
        lastConnectionTestSuccess = false
        lastConnectionTestMessage = ""
        setApiKey(null)
    }
    
    /**
     * Updates the last connection test results
     */
    fun updateConnectionTestResults(success: Boolean, message: String) {
        lastConnectionTestTime = System.currentTimeMillis()
        lastConnectionTestSuccess = success
        lastConnectionTestMessage = message
    }
    
    private fun createCredentialAttributes(): CredentialAttributes {
        return CredentialAttributes(
            generateServiceName("CursorPlugin", API_KEY_SERVICE_NAME)
        )
    }
}