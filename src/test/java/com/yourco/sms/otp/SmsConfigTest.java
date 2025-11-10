package com.yourco.sms.otp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sp.twofa.sms.SmsConfig;

class SmsConfigTest {

    @Test
    void shouldApplyDefaultsWhenMissing() {
        Map<String, String> empty = Collections.<String, String>emptyMap();
        SmsConfig config = SmsConfig.forTest(empty, empty, key -> null);
        assertThat(config.vendor()).isNull();
        assertThat(config.ttlSeconds()).isEqualTo(300);
        assertThat(config.otpLength()).isEqualTo(6);
        assertThat(config.maxAttempts()).isEqualTo(5);
    }

    @Test
    void shouldClampOtpLength() {
        Map<String, String> direct = Map.of("sms.vendor", "TwIlIo", "sms.otpLength", "12");
        SmsConfig config = SmsConfig.forTest(direct, Collections.emptyMap(), key -> null);
        assertThat(config.vendor()).isEqualTo("TwIlIo");
        assertThat(config.otpLength()).isEqualTo(10);
    }

    @Test
    void shouldReadFromEnvironmentWhenNotProvided() {
        Map<String, String> env = new HashMap<>();
        env.put("SMS_VENDOR", "dummy");
        env.put("SMS_TTLSECONDS", "120");
        SmsConfig config = SmsConfig.forTest(Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(), env::get);
        assertThat(config.vendor()).isEqualTo("dummy");
        assertThat(config.ttlSeconds()).isEqualTo(120);
    }

    @Test
    void realmAttributesActAsSecondary() {
        Map<String, String> realm = Map.of("sms.vendor", "infobip", "sms.timeoutMs", "9000");
        SmsConfig config = SmsConfig.forTest(Collections.emptyMap(), realm, key -> null);
        assertThat(config.vendor()).isEqualTo("infobip");
        assertThat(config.timeoutMs()).isEqualTo(9000);
    }
}
