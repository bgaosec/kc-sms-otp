package com.sp.twofa.sms.sender;

import java.util.List;
import java.util.Map;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.SmsSender;

public class InfobipSender extends AbstractHttpSmsSender implements SmsSender {

    public InfobipSender(SmsConfig config) {
        super(config);
    }

    @Override
    public void send(String toE164, String message) throws Exception {
        if (config.apiKey() == null) {
            throw new IllegalStateException("Infobip requires sms.apiKey");
        }
        String base = config.baseUrl() != null ? config.baseUrl() : "https://api.infobip.com";
        Map<String, Object> payload = Map.of(
                "messages", List.of(
                        Map.of(
                                "from", config.fromNumber(),
                                "destinations", List.of(Map.of("to", toE164)),
                                "text", message
                        ))
        );
        var response = postJson(base + "/sms/2/text/advanced", payload, "Authorization", "App " + config.apiKey());
        ensureSuccess(response, "Infobip");
    }
}
