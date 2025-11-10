package com.yourco.sms.otp.driver;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.sp.twofa.sms.auth.SmsOtpAuthenticator;
import com.sp.twofa.sms.requiredaction.VerifyPhoneRequiredAction;
import com.yourco.sms.otp.testdoubles.FakeAuthenticationFlowContext;
import com.yourco.sms.otp.testdoubles.FakeRequiredActionContext;
import com.yourco.sms.otp.testdoubles.FakeUserModel;

/**
 * Standalone driver that simulates verify-phone + SMS OTP flows using the dummy sender.
 */
public class StandaloneDriver {

    public record DriverResult(boolean phoneVerified, boolean otpSucceeded) {}

    public static void main(String[] args) {
        DriverResult result = new StandaloneDriver().run(System.in, System.out);
        System.exit(result.phoneVerified && result.otpSucceeded ? 0 : 1);
    }

    public DriverResult run(InputStream in, PrintStream out) {
        Scanner scanner = new Scanner(in);
        FakeUserModel user = new FakeUserModel("cli-user");

        out.print("Enter phone number in E.164 format: ");
        String phone = scanner.nextLine().trim();

        FakeRequiredActionContext requiredActionContext = new FakeRequiredActionContext()
                .user(user)
                .config("sms.vendor", "dummy");
        VerifyPhoneRequiredAction requiredAction = new VerifyPhoneRequiredAction(null);

        requiredActionContext.request().put("phoneNumber", phone).put("send", "true");
        requiredAction.processAction(requiredActionContext);
        String verificationCode = requiredActionContext.authSession().getAuthNote("spVerifyCode");
        out.printf("Dummy sender dispatched verification code %s%n", verificationCode);

        out.print("Enter verification code: ");
        String submittedCode = scanner.nextLine().trim();
        requiredActionContext.request().formParameters().clear();
        requiredActionContext.request().put("verificationCode", submittedCode);
        requiredAction.processAction(requiredActionContext);
        boolean verified = "true".equals(user.getFirstAttribute("phoneNumberVerified"));
        out.printf("Phone verification %s%n", verified ? "succeeded" : "failed");

        FakeAuthenticationFlowContext flowContext = new FakeAuthenticationFlowContext()
                .user(user)
                .config("sms.vendor", "dummy");
        SmsOtpAuthenticator authenticator = new SmsOtpAuthenticator(null);
        authenticator.authenticate(flowContext);
        String otp = flowContext.authSession().getAuthNote("spSmsOtpCode");
        out.printf("Dummy sender dispatched login OTP %s%n", otp);
        out.print("Enter login OTP: ");
        String submittedOtp = scanner.nextLine().trim();
        flowContext.request().put("smsCode", submittedOtp);
        authenticator.action(flowContext);

        boolean loginSuccess = flowContext.successCalled();
        out.printf("Login %s%n", loginSuccess ? "succeeded" : "failed");

        return new DriverResult(verified, loginSuccess);
    }
}
