package com.yourco.sms.otp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.yourco.sms.otp.driver.StandaloneDriver;

class StandaloneDriverIT {

    @Test
    void driverCompletesHappyPath() {
        String script = """
                +12025550123
                888888
                888888
                """;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        StandaloneDriver.DriverResult result = new StandaloneDriver()
                .run(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)), new PrintStream(out));

        assertThat(result.phoneVerified()).isTrue();
        assertThat(result.otpSucceeded()).isTrue();
        assertThat(out.toString(StandardCharsets.UTF_8)).contains("succeeded");
    }
}
