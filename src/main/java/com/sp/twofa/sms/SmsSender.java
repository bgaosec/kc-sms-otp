package com.sp.twofa.sms;

import com.sp.twofa.sms.sender.AfricasTalkingSender;
import com.sp.twofa.sms.sender.InfobipSender;
import com.sp.twofa.sms.sender.MessageBirdSender;
import com.sp.twofa.sms.sender.SinchSender;
import com.sp.twofa.sms.sender.TwilioSender;

/**
 * Contract for SMS delivery vendors.
 */
public interface SmsSender {

    void send(String toE164, String message) throws Exception;

    static SmsSender fromConfig(SmsConfig cfg) {
        if (cfg == null || cfg.vendor() == null) {
            throw new IllegalStateException("sms.vendor must be configured");
        }
        return switch (cfg.vendor().toLowerCase()) {
            case "infobip" -> new InfobipSender(cfg);
            case "africastalking" -> new AfricasTalkingSender(cfg);
            case "twilio" -> new TwilioSender(cfg);
            case "sinch" -> new SinchSender(cfg);
            case "messagebird" -> new MessageBirdSender(cfg);
            default -> throw new IllegalArgumentException("Unsupported SMS vendor: " + cfg.vendor());
        };
    }
}
