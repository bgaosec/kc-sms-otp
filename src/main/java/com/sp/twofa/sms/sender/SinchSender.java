package com.sp.twofa.sms.sender;

import java.util.List;
import java.util.Map;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.SmsSender;

public class SinchSender extends AbstractHttpSmsSender implements SmsSender {

    public SinchSender(SmsConfig config) {
        super(config);
    }

    @Override
    public void send(String toE164, String message) throws Exception {
        if (config.accountSid() == null || config.apiSecret() == null) {
            throw new IllegalStateException("Sinch requires sms.accountSid (service plan id) and sms.apiSecret (token)");
        }
        if (config.fromNumber() == null) {
            throw new IllegalStateException("Sinch requires sms.fromNumber");
        }
        String url = String.format("https://sms.api.sinch.com/xms/v1/%s/batches", config.accountSid());
        Map<String, Object> payload = Map.of(
                "from", config.fromNumber(),
                "to", List.of(toE164),
                "body", message
        );
        var response = postJson(url, payload, "Authorization", basic(config.accountSid(), config.apiSecret()));
        ensureSuccess(response, "Sinch");
    }

    private String basic(String user, String pass) {
        String creds = user + ":" + pass;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(creds.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
