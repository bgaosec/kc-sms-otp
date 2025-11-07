package com.sp.twofa.sms.sender;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.SmsSender;

public class TwilioSender extends AbstractHttpSmsSender implements SmsSender {

    public TwilioSender(SmsConfig config) {
        super(config);
    }

    @Override
    public void send(String toE164, String message) throws Exception {
        if (config.accountSid() == null || config.apiSecret() == null) {
            throw new IllegalStateException("Twilio requires sms.accountSid and sms.apiSecret (auth token)");
        }
        if (config.fromNumber() == null) {
            throw new IllegalStateException("Twilio requires sms.fromNumber");
        }
        String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", config.accountSid());
        String body = "To=" + encode(toE164) +
                "&From=" + encode(config.fromNumber()) +
                "&Body=" + encode(message);
        var response = postForm(url, body, config.accountSid(), config.apiSecret());
        ensureSuccess(response, "Twilio");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
