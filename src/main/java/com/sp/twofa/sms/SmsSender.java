package com.sp.twofa.sms;

import org.jboss.logging.Logger;

import com.sp.twofa.sms.sender.AfricasTalkingSender;
import com.sp.twofa.sms.sender.InfobipSender;
import com.sp.twofa.sms.sender.MessageBirdSender;
import com.sp.twofa.sms.sender.SinchSender;
import com.sp.twofa.sms.sender.TwilioSender;

/**
 * Contract for SMS delivery vendors.
 */
public interface SmsSender {

    Logger LOG = Logger.getLogger(SmsSender.class);

    void send(String toE164, String message) throws Exception;

    static SmsSender fromConfig(SmsConfig cfg) {
        if (cfg == null || cfg.vendor() == null) {
            LOG.error("sms.vendor missing in configuration");
            throw new IllegalStateException("sms.vendor must be configured");
        }
        String vendor = cfg.vendor().toLowerCase();
        LOG.debugf("Resolving SmsSender for vendor=%s", vendor);
        return switch (vendor) {
            case "infobip" -> new InfobipSender(cfg);
            case "africastalking" -> new AfricasTalkingSender(cfg);
            case "twilio" -> new TwilioSender(cfg);
            case "sinch" -> new SinchSender(cfg);
            case "messagebird" -> new MessageBirdSender(cfg);
            default -> {
                LOG.errorf("Unsupported SMS vendor configured: %s", cfg.vendor());
                throw new IllegalArgumentException("Unsupported SMS vendor: " + cfg.vendor());
            }
        };
    }
}
