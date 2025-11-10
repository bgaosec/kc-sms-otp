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

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.sender.DummySender;

class DummySenderTest {

    private Handler handler;
    private final List<LogRecord> records = new CopyOnWriteArrayList<>();

    @BeforeEach
    void configureLogging() {
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
        Logger root = Logger.getLogger("");
        handler.setLevel(Level.ALL);
        root.addHandler(handler);
    }

    @AfterEach
    void cleanup() {
        Logger.getLogger("").removeHandler(handler);
        records.clear();
    }

    @Test
    void dummySenderMasksDetails() throws Exception {
        DummySender sender = new DummySender(new SmsConfig("dummy", null, null, null, null,
                null, null, 1000, 60, 6, 30, 5));

        sender.send("+12025550123", "Your verification code is 888888");

        assertThat(records)
                .isNotEmpty()
                .allMatch(r -> !r.getMessage().contains("888888"));
        assertThat(records.get(0).getMessage()).contains("***23");
    }
}
