package com.yourco.sms.otp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sp.twofa.sms.auth.SmsOtpAuthenticator;
import com.yourco.sms.otp.testdoubles.FakeAuthenticationFlowContext;
import com.yourco.sms.otp.testdoubles.FakeUserModel;

class AuthenticatorFlowUnitTest {

    @Test
    void successfulFlowMarksContextSuccess() {
        FakeUserModel user = new FakeUserModel("user")
                .attribute("phoneNumber", "+12025550123")
                .attribute("phoneNumberVerified", "true");
        FakeAuthenticationFlowContext ctx = new FakeAuthenticationFlowContext()
                .user(user)
                .config("sms.vendor", "dummy");

        SmsOtpAuthenticator authenticator = new SmsOtpAuthenticator(null);
        authenticator.authenticate(ctx);
        assertThat(ctx.forms().lastTemplate()).isEqualTo("sms-otp.ftl");

        String code = ctx.authSession().getAuthNote("spSmsOtpCode");
        ctx.request().put("smsCode", code);
        authenticator.action(ctx);

        assertThat(ctx.successCalled()).isTrue();
    }

    @Test
    void missingPhoneLeadsToAttempted() {
        FakeUserModel user = new FakeUserModel("user");
        FakeAuthenticationFlowContext ctx = new FakeAuthenticationFlowContext()
                .user(user)
                .config("sms.vendor", "dummy");

        SmsOtpAuthenticator authenticator = new SmsOtpAuthenticator(null);
        authenticator.authenticate(ctx);

        assertThat(ctx.attemptedCalled()).isTrue();
    }
}
