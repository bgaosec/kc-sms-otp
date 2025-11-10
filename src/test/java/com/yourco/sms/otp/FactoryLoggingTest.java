package com.yourco.sms.otp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sp.twofa.sms.auth.SmsOtpAuthenticatorFactory;
import com.sp.twofa.sms.requiredaction.VerifyPhoneRequiredActionFactory;

class FactoryLoggingTest {

    private Handler handler;
    private final List<LogRecord> records = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() {
        System.setProperty("org.jboss.logging.provider", "jdk");
        handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        handler.setLevel(Level.ALL);
        Logger.getLogger("").addHandler(handler);
    }

    @AfterEach
    void tearDown() {
        Logger.getLogger("").removeHandler(handler);
        records.clear();
    }

    @Test
    void factoriesEmitStartupLogs() {
        SmsOtpAuthenticatorFactory authFactory = new SmsOtpAuthenticatorFactory();
        VerifyPhoneRequiredActionFactory actionFactory = new VerifyPhoneRequiredActionFactory();

        authFactory.init(null);
        actionFactory.init(null);
        authFactory.postInit(null);
        actionFactory.postInit(null);

        assertThat(records.stream()
                .map(LogRecord::getMessage)
                .filter(msg -> msg != null && msg.contains("SMS OTP (SP)")))
                .isNotEmpty();
        assertThat(records.stream()
                .map(LogRecord::getMessage)
                .filter(msg -> msg != null && msg.contains("Verify Phone Number via SMS")))
                .isNotEmpty();
    }
}
