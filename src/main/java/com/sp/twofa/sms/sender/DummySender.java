package com.sp.twofa.sms.sender;

import org.jboss.logging.Logger;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.SmsSender;

/**
 * Test sender that only logs messages and pairs with the dummy vendor
 * to keep OTP generation deterministic (all 8s).
 */
public class DummySender implements SmsSender {

    private static final Logger LOG = Logger.getLogger(DummySender.class);
    private final SmsConfig config;

    public DummySender(SmsConfig config) {
        this.config = config;
    }

    @Override
    public void send(String toE164, String message) {
        String masked = mask(toE164);
        LOG.warnf("[Dummy SMS] to=%s chars=%d", masked, message != null ? message.length() : 0);
    }

    private String mask(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "***" + phone.substring(phone.length() - 2);
    }
}
