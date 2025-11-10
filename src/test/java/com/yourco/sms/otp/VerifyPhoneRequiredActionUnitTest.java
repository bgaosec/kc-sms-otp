package com.yourco.sms.otp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sp.twofa.sms.requiredaction.VerifyPhoneRequiredAction;
import com.yourco.sms.otp.testdoubles.FakeRequiredActionContext;
import com.yourco.sms.otp.testdoubles.FakeUserModel;

class VerifyPhoneRequiredActionUnitTest {

    @Test
    void completesVerificationFlow() {
        FakeUserModel user = new FakeUserModel("user");
        FakeRequiredActionContext context = new FakeRequiredActionContext()
                .user(user)
                .config("sms.vendor", "dummy");

        VerifyPhoneRequiredAction action = new VerifyPhoneRequiredAction(null);
        context.request().put("phoneNumber", "+447700900123").put("send", "true");
        action.processAction(context);

        String code = context.authSession().getAuthNote("spVerifyCode");
        context.request().formParameters().clear();
        context.request().put("verificationCode", code);
        action.processAction(context);

        assertThat(user.getFirstAttribute("phoneNumber")).isEqualTo("+447700900123");
        assertThat(user.getFirstAttribute("phoneNumberVerified")).isEqualTo("true");
        assertThat(context.getRecordedStatus()).isEqualTo(org.keycloak.authentication.RequiredActionContext.Status.SUCCESS);
    }

    @Test
    void invalidCodeKeepsActionInProgress() {
        FakeUserModel user = new FakeUserModel("user");
        FakeRequiredActionContext context = new FakeRequiredActionContext()
                .user(user)
                .config("sms.vendor", "dummy");
        VerifyPhoneRequiredAction action = new VerifyPhoneRequiredAction(null);

        context.request().put("phoneNumber", "+11234567890").put("send", "true");
        action.processAction(context);
        context.request().formParameters().clear();
        context.request().put("verificationCode", "0000");
        action.processAction(context);

        assertThat(context.getRecordedStatus()).isEqualTo(org.keycloak.authentication.RequiredActionContext.Status.CHALLENGE);
        assertThat(user.getFirstAttribute("phoneNumberVerified")).isNull();
    }
}
