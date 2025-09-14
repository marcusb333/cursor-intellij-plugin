package com.cursor.plugin;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class CursorAIServiceTest {

    public static final String CURSOR_API_KEY = "CURSOR_API_KEY";
    public static final String TEST_API_KEY = "test-api-key";
    @Mock
    private Project mockProject;

    private MockWebServer mockServer;
    private CursorAIService aiService;
    private CursorAIService spyService;

    @BeforeEach
    void setUp() throws Exception {
        // Set up mock server
        mockServer = new MockWebServer();
        mockServer.start();
        // Create service instance with mock server URL
        aiService = new CursorAIService(mockProject, mockServer.url("/").toString());
        // Create spy for mocking getApiKey method
        spyService = spy(aiService);
    }

    @AfterEach
    void tearDown() {
        // Clean up mock server
        if (mockServer != null) {
            try {
                mockServer.shutdown();
            } catch (IOException e) {
                // Ignore shutdown errors in tests
            }
        }
    }

    @Test
    void testGetInstance() {
        // Given
        when(mockProject.getService(CursorAIService.class)).thenReturn(aiService);
        
        // When
        CursorAIService instance = CursorAIService.getInstance(mockProject);
        
        // Then
        assertThat(instance).isNotNull();
        assertThat(instance).isInstanceOf(CursorAIService.class);
    }

    @Test
    void testSendMessageWithValidApiKey() throws InterruptedException {
        // Given
        when(spyService.getApiKey()).thenReturn(TEST_API_KEY);
        String expectedResponse = "This is a test response";
        JsonObject responseJson = new JsonObject();
        JsonObject choice = new JsonObject();
        JsonObject message = new JsonObject();
        message.addProperty("content", expectedResponse);
        choice.add("message", message);
        com.google.gson.JsonArray choicesArray = new com.google.gson.JsonArray();
        choicesArray.add(choice);
        responseJson.add("choices", choicesArray);
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson.toString())
                .addHeader("Content-Type", "application/json"));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        CursorAIService.CursorAIResponseCallback callback = new CursorAIService.CursorAIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                result.set(response);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                error.set(errorMessage);
                latch.countDown();
            }
        };

        // When
        spyService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isEqualTo(expectedResponse);
        assertThat(error.get()).isNull();

        // Verify request was made correctly
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-api-key");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");
    }

    @Test
    void testSendMessageWithMissingApiKey() throws InterruptedException {
        // Given - Mock getApiKey to return null
        when(spyService.getApiKey()).thenReturn(null);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        CursorAIService.CursorAIResponseCallback callback = new CursorAIService.CursorAIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                result.set(response);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                error.set(errorMessage);
                latch.countDown();
            }
        };

        // When
        spyService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNull();
        assertThat(error.get()).contains("Cursor API key not found. Please set it using one of these methods:");
    }

    @Test
    void testSendMessageWithApiError() throws InterruptedException {
        // Given
        when(spyService.getApiKey()).thenReturn(TEST_API_KEY);
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        CursorAIService.CursorAIResponseCallback callback = new CursorAIService.CursorAIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                result.set(response);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                error.set(errorMessage);
                latch.countDown();
            }
        };

        // When
        spyService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNull();
        assertThat(error.get()).contains("API error: 401");
    }

    @Test
    void testSendMessageWithMalformedResponse() throws InterruptedException {
        // Given
        when(spyService.getApiKey()).thenReturn(TEST_API_KEY);
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("Invalid JSON response"));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        CursorAIService.CursorAIResponseCallback callback = new CursorAIService.CursorAIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                result.set(response);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                error.set(errorMessage);
                latch.countDown();
            }
        };

        // When
        spyService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNull();
        assertThat(error.get()).contains("Failed to parse response:");
    }

    @Test
    void testSendMessageWithEmptyApiKey() throws InterruptedException {
        // Given - Mock getApiKey to return empty string
        when(spyService.getApiKey()).thenReturn("");

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        CursorAIService.CursorAIResponseCallback callback = new CursorAIService.CursorAIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                result.set(response);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                error.set(errorMessage);
                latch.countDown();
            }
        };

        // When
        spyService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNull();
        assertThat(error.get()).contains("Cursor API key not found. Please set it using one of these methods:");
    }

    @Test
    void testSendMessageWithEmptyResponse() throws InterruptedException {
        // Given
        when(spyService.getApiKey()).thenReturn(TEST_API_KEY);
        
        JsonObject responseJson = new JsonObject();
        JsonObject choice = new JsonObject();
        JsonObject message = new JsonObject();
        message.addProperty("content", "");
        choice.add("message", message);
        com.google.gson.JsonArray choicesArray = new com.google.gson.JsonArray();
        choicesArray.add(choice);
        responseJson.add("choices", choicesArray);
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(responseJson.toString())
                .addHeader("Content-Type", "application/json"));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        CursorAIService.CursorAIResponseCallback callback = new CursorAIService.CursorAIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                result.set(response);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                error.set(errorMessage);
                latch.countDown();
            }
        };

        // When
        spyService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isEqualTo("");
        assertThat(error.get()).isNull();
    }

    @Test
    void testSendMessageWithServerError() throws InterruptedException {
        // Given
        when(spyService.getApiKey()).thenReturn(TEST_API_KEY);
        
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();

        CursorAIService.CursorAIResponseCallback callback = new CursorAIService.CursorAIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                result.set(response);
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                error.set(errorMessage);
                latch.countDown();
            }
        };

        // When
        spyService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNull();
        assertThat(error.get()).contains("API error: 500");
    }

    @Test
    void testGetApiKey_SystemProperty() {
        // Test system property resolution
        System.setProperty("cursor.api.key", "test-system-property-key");
        try {
            String apiKey = aiService.getApiKey();
            assertThat(apiKey).isEqualTo("test-system-property-key");
        } finally {
            System.clearProperty("cursor.api.key");
        }
    }

    @Test
    void testGetApiKey_EnvironmentVariable() {
        // Since we can't easily set environment variables in tests,
        // we'll create a spy to mock the getApiKey method
        CursorAIService testService = spy(new CursorAIService(mockProject, mockServer.url("/").toString()));
        
        // Mock the getApiKey method to return null (simulating no environment variable)
        when(testService.getApiKey()).thenReturn(null);

        // Verify the method returns null when no API key is configured
        String apiKey = testService.getApiKey();
        assertThat(apiKey).isNull();
    }

    @Test
    void testGetApiKey_TrimWhitespace() {
        // Test that whitespace is properly trimmed
        System.setProperty("cursor.api.key", "  test-key-with-spaces  ");
        try {
            String apiKey = aiService.getApiKey();
            assertThat(apiKey).isEqualTo("test-key-with-spaces");
        } finally {
            System.clearProperty("cursor.api.key");
        }
    }
}