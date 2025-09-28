package io.threethirtythree.plugin.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApiConnectionTestServiceTest {
    private val testService = ApiConnectionTestService()

    @Test
    fun testConnectionWithNullApiKey() {
        val result = testService.testConnection(null, "https://api.openai.com/v1", 30)

        assertFalse(result.success)
        assertEquals("API key is not configured", result.message)
        assertNotNull(result.errorDetails)
        assertTrue(result.errorDetails!!.contains("Please enter your OpenAI API key"))
    }

    @Test
    fun testConnectionWithEmptyApiKey() {
        val result = testService.testConnection("", "https://api.openai.com/v1", 30)

        assertFalse(result.success)
        assertEquals("API key is not configured", result.message)
    }

    @Test
    fun testConnectionWithInvalidEndpoint() {
        val result = testService.testConnection("sk-test123", "https://invalid-endpoint-that-does-not-exist.com", 5)

        assertFalse(result.success)
        assertTrue(result.message.contains("Cannot resolve host") || result.message.contains("Connection failed"))
        assertNotNull(result.errorDetails)
    }

    @Test
    fun testResultDataStructure() {
        // Test success result
        val successResult =
            ApiConnectionTestService.TestResult(
                success = true,
                message = "Connected",
                responseTimeMs = 250,
                model = "gpt-3.5-turbo",
                additionalInfo = "Working correctly",
            )

        assertTrue(successResult.success)
        assertEquals("Connected", successResult.message)
        assertEquals(250L, successResult.responseTimeMs)
        assertEquals("gpt-3.5-turbo", successResult.model)
        assertEquals("Working correctly", successResult.additionalInfo)
        assertNull(successResult.errorDetails)

        // Test error result
        val errorResult =
            ApiConnectionTestService.TestResult(
                success = false,
                message = "Failed",
                errorDetails = "Invalid API key",
            )

        assertFalse(errorResult.success)
        assertEquals("Failed", errorResult.message)
        assertEquals(0L, errorResult.responseTimeMs)
        assertNull(errorResult.model)
        assertNull(errorResult.additionalInfo)
        assertEquals("Invalid API key", errorResult.errorDetails)
    }

    @Test
    fun testTimeoutHandling() {
        // Test with very short timeout (likely to fail)
        val result = testService.testConnection("sk-test123", "https://api.openai.com/v1", 1)

        // Should either timeout or fail authentication
        assertFalse(result.success)
        assertNotNull(result.message)
        assertTrue(result.responseTimeMs >= 0)
    }
}
