package com.cursor.plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
// Removed IntelliJ Platform test framework dependency
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

@ExtendWith(MockitoExtension.class)
class CursorAIServiceTest {

    @Mock
    private Project mockProject;

    private MockWebServer mockServer;
    private CursorAIService aiService;
    private String originalApiKey;

    @BeforeEach
    void setUp() throws Exception {
        
        // Store original API key
        originalApiKey = System.getProperty("cursor.api.key");
        
        // Set up mock server
        mockServer = new MockWebServer();
        mockServer.start();
        
        // Create service instance with mock server URL
        aiService = new CursorAIService(mockProject, mockServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original API key
        if (originalApiKey != null) {
            System.setProperty("cursor.api.key", originalApiKey);
        } else {
            System.clearProperty("cursor.api.key");
        }
        
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
        System.setProperty("cursor.api.key", "test-api-key");
        
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
        aiService.sendMessage("Test message", "Test context", callback);

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
        // Given
        System.clearProperty("cursor.api.key");
        System.clearProperty("CURSOR_API_KEY");

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
        aiService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNull();
        assertThat(error.get()).isEqualTo("API key not configured. Please set your Cursor API key in Settings.");
    }


    @Test
    void testSendMessageWithApiError() throws InterruptedException {
        // Given
        System.setProperty("cursor.api.key", "test-api-key");
        
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
        aiService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNull();
        assertThat(error.get()).contains("API error: 401");
    }


    @Test
    void testSendMessageWithMalformedResponse() throws InterruptedException {
        // Given
        System.setProperty("cursor.api.key", "test-api-key");
        
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
        aiService.sendMessage("Test message", "Test context", callback);

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(result.get()).isNull();
        assertThat(error.get()).contains("Failed to parse response:");
    }


}