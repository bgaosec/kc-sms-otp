package com.sp.twofa.sms.sender;

import java.util.List;
import java.util.Map;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.SmsSender;

public class MessageBirdSender extends AbstractHttpSmsSender implements SmsSender {

    public MessageBirdSender(SmsConfig config) {
        super(config);
    }

    @Override
    public void send(String toE164, String message) throws Exception {
        if (config.apiKey() == null) {
            throw new IllegalStateException("MessageBird requires sms.apiKey");
        }
        if (config.fromNumber() == null) {
            throw new IllegalStateException("MessageBird requires sms.fromNumber (originator)");
        }
        String base = config.baseUrl() != null ? config.baseUrl() : "https://rest.messagebird.com";
        Map<String, Object> payload = Map.of(
                "originator", config.fromNumber(),
                "recipients", List.of(toE164),
                "body", message
        );
        var response = postJson(base + "/messages", payload, "Authorization", "AccessKey " + config.apiKey());
        ensureSuccess(response, "MessageBird");
    }
}
