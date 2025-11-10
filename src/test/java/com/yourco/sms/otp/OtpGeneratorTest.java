package com.yourco.sms.otp;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.auth.SmsOtpAuthenticator;

class OtpGeneratorTest {

    private static Method generator;

    @BeforeAll
    static void setup() throws Exception {
        generator = SmsOtpAuthenticator.class.getDeclaredMethod("generateCode", com.sp.twofa.sms.SmsConfig.class);
        generator.setAccessible(true);
    }

    private String invoke(SmsConfig config) {
        try {
            return (String) generator.invoke(new com.sp.twofa.sms.auth.SmsOtpAuthenticator(null), config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SmsConfig config(String vendor, int length) {
        return new SmsConfig(vendor, null, null, null, null, null, null, 7000, 300, length, 45, 5);
    }

    @Test
    void shouldRespectLengthAndDigits() {
        String code = invoke(config("twilio", 6));
        assertThat(code).hasSize(6).matches("\\d{6}");
    }

    @Test
    void shouldGenerateDifferentCodesForNonDummyVendors() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            codes.add(invoke(config("twilio", 6)));
        }
        assertThat(codes).hasSizeGreaterThan(1);
    }

    @Test
    void dummyVendorAlwaysReturnsEights() {
        String code = invoke(config("dummy", 4));
        assertThat(code).isEqualTo("8888");
    }
}
