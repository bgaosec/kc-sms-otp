package com.yourco.sms.otp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sp.twofa.sms.auth.SmsOtpAuthenticator;
import com.yourco.sms.otp.testdoubles.FakeAuthenticationFlowContext;
import com.yourco.sms.otp.testdoubles.FakeUserModel;

class RateLimitAndExpiryTest {

    private SmsOtpAuthenticator authenticator;
    private FakeAuthenticationFlowContext context;

    @BeforeEach
    void setup() {
        authenticator = new SmsOtpAuthenticator(null);
        FakeUserModel user = new FakeUserModel("tester")
                .attribute("phoneNumber", "+12025550123")
                .attribute("phoneNumberVerified", "true");
        context = new FakeAuthenticationFlowContext()
                .user(user)
                .config("sms.vendor", "dummy")
                .config("sms.resendSeconds", "60")
                .config("sms.ttlSeconds", "1")
                .config("sms.maxAttempts", "2");
    }

    @Test
    void resendBeforeCooldownIsRejected() {
        authenticator.authenticate(context);
        context.request().put("resend", "true");

        authenticator.action(context);

        assertThat(context.forms().lastError()).isEqualTo("smsResendCooldown");
    }

    @Test
    void expiredCodeTriggersError() {
        authenticator.authenticate(context);
        String code = context.authSession().getAuthNote("spSmsOtpCode");
        context.authSession().setAuthNote("spSmsOtpExp", Long.toString(System.currentTimeMillis() - 2000));
        context.request().put("smsCode", code);

        authenticator.action(context);

        assertThat(context.forms().lastError()).isEqualTo("smsCodeExpired");
    }

    @Test
    void exceedingAttemptsFailsExecution() {
        authenticator.authenticate(context);
        context.request().put("smsCode", "0000");

        authenticator.action(context);
        authenticator.action(context);
        authenticator.action(context);

        assertThat(context.lastError()).isEqualTo(org.keycloak.authentication.AuthenticationFlowError.INVALID_CREDENTIALS);
    }
}
