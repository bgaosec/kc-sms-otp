package com.sp.twofa.sms.sender;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.twofa.sms.SmsConfig;

/**
 * Shared HTTP helper for vendor implementations.
 */
abstract class AbstractHttpSmsSender {
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected final SmsConfig config;
    private final HttpClient client;

    protected AbstractHttpSmsSender(SmsConfig config) {
        this.config = config;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.timeoutMs()))
                .build();
    }

    protected HttpResponse<String> postJson(String url, Object payload, String apiKeyHeader, String apiKey) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(config.timeoutMs()))
                .header("Content-Type", "application/json");
        if (apiKeyHeader != null && apiKey != null) {
            builder.header(apiKeyHeader, apiKey);
        }
        HttpRequest request = builder
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(payload)))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> postForm(String url, String formBody, String basicUser, String basicPassword) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(config.timeoutMs()))
                .header("Content-Type", "application/x-www-form-urlencoded");
        if (basicUser != null && basicPassword != null) {
            String creds = basicUser + ":" + basicPassword;
            String encoded = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
            builder.header("Authorization", "Basic " + encoded);
        }
        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(formBody)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected void ensureSuccess(HttpResponse<String> response, String vendorName) throws Exception {
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            throw new Exception(vendorName + " SMS request failed with HTTP " + status);
        }
    }
}
