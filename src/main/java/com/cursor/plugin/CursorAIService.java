package com.cursor.plugin;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public final class CursorAIService {
    private static final String CURSOR_API_URL = "https://api.cursor.com/v1/chat/completions";
    private static final String API_KEY_PROPERTY = "cursor.api.key";
    
    private final Project project;
    private final OkHttpClient httpClient;
    private final Gson gson;
    
    public CursorAIService(Project project) {
        this.project = project;
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
            callback.onError("API key not configured. Please set your Cursor API key in Settings.");
            return;
        }
        
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
                .url(CURSOR_API_URL)
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
                    String content = jsonResponse.getAsJsonObject("choices")
                            .getAsJsonArray("choices")
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
    
    private String getApiKey() {
        // In a real implementation, this would read from IntelliJ settings
        // For now, we'll use a system property or environment variable
        String apiKey = System.getProperty(API_KEY_PROPERTY);
        if (apiKey == null) {
            apiKey = System.getenv("CURSOR_API_KEY");
        }
        return apiKey;
    }
    
    public interface CursorAIResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}