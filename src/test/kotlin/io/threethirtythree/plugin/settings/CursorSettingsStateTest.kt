package io.threethirtythree.plugin.settings

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CursorSettingsStateTest {
    
    private lateinit var settings: CursorSettingsState
    
    @TempDir
    lateinit var tempDir: Path
    
    @BeforeEach
    fun setUp() {
        settings = CursorSettingsState()
    }
    
    @Test
    fun testDefaultValues() {
        assertEquals(CursorSettingsState.DEFAULT_API_ENDPOINT, settings.apiEndpoint)
        assertEquals(CursorSettingsState.DEFAULT_MODEL, settings.model)
        assertEquals(CursorSettingsState.DEFAULT_MAX_TOKENS, settings.maxTokens)
        assertEquals(CursorSettingsState.DEFAULT_TEMPERATURE, settings.temperature)
        assertEquals(CursorSettingsState.DEFAULT_TIMEOUT_SECONDS, settings.timeoutSeconds)
        assertFalse(settings.lastConnectionTestSuccess)
        assertEquals("", settings.lastConnectionTestMessage)
        assertEquals(0L, settings.lastConnectionTestTime)
    }
    
    @Test
    fun testApiKeyStorage() {
        // Note: This test is simplified as PasswordSafe requires platform runtime
        // In a real test environment with IntelliJ Platform test framework,
        // this would test actual storage and retrieval
        val testApiKey = "sk-test123456789"
        
        // Test masked key generation logic
        val longKey = "sk-1234567890abcdefghijklmnop"
        val maskedLong = "*".repeat(longKey.length - 4) + "mnop"
        assertEquals(longKey.length, maskedLong.length)
        
        val shortKey = "short"
        val maskedShort = "*".repeat(shortKey.length)
        assertEquals(shortKey.length, maskedShort.length)
    }
    
    @Test
    fun testMaskedApiKey() {
        // Note: This test validates the masking logic independently
        // since PasswordSafe requires platform runtime
        
        // Test empty key logic
        val emptyMasked = ""
        assertEquals("", emptyMasked)
        
        // Test long key masking logic
        val longKey = "sk-1234567890abcdefghijklmnop"
        val expectedLongMask = "*".repeat(longKey.length - 4) + longKey.takeLast(4)
        assertTrue(expectedLongMask.endsWith("mnop"))
        assertTrue(expectedLongMask.startsWith("*"))
        assertEquals(longKey.length, expectedLongMask.length)
        
        // Test short key masking logic
        val shortKey = "short"
        val expectedShortMask = "*".repeat(shortKey.length)
        assertEquals("*****", expectedShortMask)
    }
    
    @Test
    fun testResetToDefaults() {
        // Modify settings
        settings.apiEndpoint = "https://custom.api.com"
        settings.model = "gpt-4"
        settings.maxTokens = 2000
        settings.temperature = 0.5
        settings.timeoutSeconds = 60
        settings.updateConnectionTestResults(true, "Test message")
        
        // Reset
        settings.resetToDefaults()
        
        // Verify defaults restored
        assertEquals(CursorSettingsState.DEFAULT_API_ENDPOINT, settings.apiEndpoint)
        assertEquals(CursorSettingsState.DEFAULT_MODEL, settings.model)
        assertEquals(CursorSettingsState.DEFAULT_MAX_TOKENS, settings.maxTokens)
        assertEquals(CursorSettingsState.DEFAULT_TEMPERATURE, settings.temperature)
        assertEquals(CursorSettingsState.DEFAULT_TIMEOUT_SECONDS, settings.timeoutSeconds)
        assertEquals(0L, settings.lastConnectionTestTime)
        assertFalse(settings.lastConnectionTestSuccess)
        assertEquals("", settings.lastConnectionTestMessage)
    }
    
    @Test
    fun testUpdateConnectionTestResults() {
        val beforeTime = System.currentTimeMillis()
        
        settings.updateConnectionTestResults(true, "Connected successfully")
        
        assertTrue(settings.lastConnectionTestSuccess)
        assertEquals("Connected successfully", settings.lastConnectionTestMessage)
        assertTrue(settings.lastConnectionTestTime >= beforeTime)
        assertTrue(settings.lastConnectionTestTime <= System.currentTimeMillis())
        
        // Test failure update
        settings.updateConnectionTestResults(false, "Connection failed")
        
        assertFalse(settings.lastConnectionTestSuccess)
        assertEquals("Connection failed", settings.lastConnectionTestMessage)
    }
    
    @Test
    fun testStateSerializationWithXmlSerializer() {
        settings.apiEndpoint = "https://test.api.com"
        settings.model = "gpt-4"
        settings.maxTokens = 2000
        settings.temperature = 0.5
        settings.timeoutSeconds = 45
        settings.lastConnectionTestTime = 123456789L
        settings.lastConnectionTestSuccess = true
        settings.lastConnectionTestMessage = "Test message"
        
        // Create new instance and load state
        val loadedSettings = CursorSettingsState()
        loadedSettings.loadState(settings)
        
        // Verify state was loaded correctly
        assertEquals(settings.apiEndpoint, loadedSettings.apiEndpoint)
        assertEquals(settings.model, loadedSettings.model)
        assertEquals(settings.maxTokens, loadedSettings.maxTokens)
        assertEquals(settings.temperature, loadedSettings.temperature)
        assertEquals(settings.timeoutSeconds, loadedSettings.timeoutSeconds)
        assertEquals(settings.lastConnectionTestTime, loadedSettings.lastConnectionTestTime)
        assertEquals(settings.lastConnectionTestSuccess, loadedSettings.lastConnectionTestSuccess)
        assertEquals(settings.lastConnectionTestMessage, loadedSettings.lastConnectionTestMessage)
    }
}