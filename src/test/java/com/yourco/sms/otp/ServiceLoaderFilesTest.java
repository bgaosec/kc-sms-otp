package com.yourco.sms.otp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class ServiceLoaderFilesTest {

    @Test
    void authenticatorFactoryRegistered() throws IOException {
        Path path = Path.of("src/main/resources/META-INF/services/org.keycloak.authentication.AuthenticatorFactory");
        assertThat(path).exists();
        List<String> lines = Files.readAllLines(path);
        assertThat(lines).contains("com.sp.twofa.sms.auth.SmsOtpAuthenticatorFactory");
    }

    @Test
    void requiredActionFactoryRegistered() throws IOException {
        Path path = Path.of("src/main/resources/META-INF/services/org.keycloak.authentication.RequiredActionFactory");
        assertThat(path).exists();
        List<String> lines = Files.readAllLines(path);
        assertThat(lines).contains("com.sp.twofa.sms.requiredaction.VerifyPhoneRequiredActionFactory");
    }
}
