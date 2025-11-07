package com.sp.twofa.sms.sender;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.SmsSender;

public class AfricasTalkingSender extends AbstractHttpSmsSender implements SmsSender {

    public AfricasTalkingSender(SmsConfig config) {
        super(config);
    }

    @Override
    public void send(String toE164, String message) throws Exception {
        if (config.apiKey() == null || config.accountSid() == null) {
            throw new IllegalStateException("Africa's Talking requires sms.apiKey and sms.accountSid (username)");
        }
        StringBuilder form = new StringBuilder();
        form.append("username=").append(encode(config.accountSid()));
        form.append("&to=").append(encode(toE164));
        form.append("&message=").append(encode(message));
        if (config.fromNumber() != null) {
            form.append("&from=").append(encode(config.fromNumber()));
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.africastalking.com/version1/messaging"))
                .timeout(Duration.ofMillis(config.timeoutMs()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("apiKey", config.apiKey())
                .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
                .build();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.timeoutMs()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ensureSuccess(response, "Africa's Talking");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
