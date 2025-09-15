package com.cursor.plugin;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Core service class that handles communication with the Cursor AI API.
 *
 * <p>This service provides the main interface for sending requests to the Cursor AI API
 * and processing responses. It manages HTTP connections, API authentication, and handles
 * both successful responses and error conditions gracefully.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Asynchronous API communication using OkHttp client</li>
 *   <li>Multiple API key configuration methods (environment variables, system properties)</li>
 *   <li>Robust error handling and user-friendly error messages</li>
 *   <li>JSON request/response processing with Gson</li>
 *   <li>Configurable timeouts for reliable operation</li>
 * </ul>
 *
 * <p>API Key Configuration:</p>
 * <p>The service supports multiple methods for providing the Cursor API key:</p>
 * <ol>
 *   <li>Environment variable: {@code CURSOR_API_KEY}</li>
 *   <li>Environment variable: {@code cursor_api_key}</li>
 *   <li>System property: {@code cursor.api.key}</li>
 * </ol>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * CursorAIService service = CursorAIService.getInstance(project);
 * service.sendMessage("Explain this code", codeContext, new CursorAIResponseCallback() {
 *     public void onSuccess(String response) {
 *         // Handle successful response
 *     }
 *     public void onError(String error) {
 *         // Handle error
 *     }
 * });
 * }</pre>
 *
 * @author Cursor AI Plugin Team
 * @version 0.0.4
 * @since 1.0
 * @see CursorAIResponseCallback
 * @see Service
 */
@Service
public final class CursorAIService {
    private static final String CURSOR_API_URL = "https://api.cursor.com/v1/chat/completions";

    private final Project project;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String apiUrl;
    
    public CursorAIService(Project project) {
        this(project, CURSOR_API_URL);
    }
    
    // Package-private constructor for testing
    CursorAIService(Project project, String apiUrl) {
        this.project = project;
        this.apiUrl = apiUrl;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }
    
    public static CursorAIService getInstance(Project project) {
        return project.getService(CursorAIService.class);
    }
    
    public void sendMessage(String message, String context, CursorAIResponseCallback callback) {
        String apiKey = getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            String errorMessage = "Cursor API key not found. Please set it using one of these methods:\n" +
                    "1. System property: -Dcursor.api.key=your_key_here\n" +
                    "2. Environment variable: CURSOR_API_KEY=your_key_here\n" +
                    "3. Environment variable: cursor_api_key=your_key_here\n\n" +
                    "For more details, see the plugin documentation.";
            callback.onError(errorMessage);
            return;
        }
        
        // Log API key status for debugging (without exposing the actual key)
        System.out.println("Cursor Plugin: API key found (length: " + apiKey.length() + " characters)");

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4");
        requestBody.addProperty("prompt", message);
        requestBody.addProperty("context", context);
        requestBody.addProperty("max_tokens", 1000);
        requestBody.addProperty("temperature", 0.7);
        
        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("API error: " + response.code() + " " + response.message());
                    return;
                }
                
                try {
                    String responseBody = response.body().string();
                    JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                    String content = jsonResponse.getAsJsonArray("choices")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content")
                            .getAsString();
                    
                    callback.onSuccess(content);
                } catch (Exception e) {
                    callback.onError("Failed to parse response: " + e.getMessage());
                }
            }
        });
    }

    String getApiKey() {
        // Try multiple sources for the API key, in order of preference:

        // 1. Environment variable CURSOR_API_KEY
        String apiKey = System.getenv("CURSOR_API_KEY");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey.trim();
        }

        // 2. Alternative environment variable (lowercase)
        apiKey = System.getenv("cursor_api_key");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey.trim();
        }

        // 3. Env property (e.g., -Dcursor.api.key=xxx)
        apiKey = System.getProperty("cursor.api.key");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey.trim();
        }

        // 4. TODO: Future implementation - read from IntelliJ settings
        // This would be where we'd read from the plugin's settings panel
        // when that feature is implemented

        return null; // No API key found
    }
    
    /**
     * Callback interface for handling responses from the Cursor AI API.
     *
     * <p>This interface defines the contract for handling both successful responses
     * and error conditions when communicating with the Cursor AI service. Implementations
     * should handle both success and error cases appropriately.</p>
     *
     * <p>The callback methods are invoked asynchronously on the HTTP client's thread pool,
     * so implementations may need to switch to the EDT (Event Dispatch Thread) for UI updates.</p>
     *
     * @since 1.0
     * @see CursorAIService#sendMessage(String, String, CursorAIResponseCallback)
     */
    public interface CursorAIResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
