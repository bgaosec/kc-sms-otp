package com.sp.twofa.sms.auth;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.SmsSender;
import com.sp.twofa.sms.requiredaction.VerifyPhoneRequiredActionFactory;

public class SmsOtpAuthenticator implements Authenticator {

    public static final String ID = "sp-2fa-sms";
    private static final Logger LOG = Logger.getLogger(SmsOtpAuthenticator.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String NOTE_CODE = "spSmsOtpCode";
    private static final String NOTE_EXP = "spSmsOtpExp";
    private static final String NOTE_ATTEMPTS = "spSmsOtpAttempts";
    private static final String NOTE_LAST_SEND = "spSmsOtpLastSend";

    private final KeycloakSession session;

    public SmsOtpAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        String phone = user != null ? user.getFirstAttribute("phoneNumber") : null;
        boolean verified = Boolean.parseBoolean(Objects.requireNonNullElse(user != null ? user.getFirstAttribute("phoneNumberVerified") : null, "false"));
        if (LOG.isDebugEnabled()) {
            LOG.debugf("SMS OTP authenticate invoked for user=%s verified=%s hasPhone=%s", user != null ? user.getUsername() : "n/a", verified, phone != null);
        }
        if (phone == null || !verified) {
            LOG.debugf("User %s has no verified phone number for SMS OTP", user != null ? user.getUsername() : "?");
            context.attempted();
            return;
        }

        SmsConfig config = SmsConfig.from(session, context.getAuthenticatorConfig());
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        if (shouldSendNew(authSession, config)) {
            sendNewOtp(context, phone, config);
        }
        challenge(context, config, phone, null);
    }

    private boolean shouldSendNew(AuthenticationSessionModel session, SmsConfig config) {
        String code = session.getAuthNote(NOTE_CODE);
        String exp = session.getAuthNote(NOTE_EXP);
        if (code == null || exp == null) {
            return true;
        }
        long expiry = parseLong(exp);
        if (Instant.now().isAfter(Instant.ofEpochMilli(expiry))) {
            return true;
        }
        return false;
    }

    private void sendNewOtp(AuthenticationFlowContext context, String phone, SmsConfig config) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String code = generateCode(config.otpLength());
        long expiry = System.currentTimeMillis() + (config.ttlSeconds() * 1000L);
        LOG.debugf("Generated new SMS OTP for session=%s ttl=%d length=%d", authSession.getParentSession().getId(), config.ttlSeconds(), config.otpLength());
        authSession.setAuthNote(NOTE_CODE, code);
        authSession.setAuthNote(NOTE_EXP, Long.toString(expiry));
        authSession.setAuthNote(NOTE_ATTEMPTS, "0");
        authSession.setAuthNote(NOTE_LAST_SEND, Long.toString(System.currentTimeMillis()));
        sendSms(phone, code, config);
    }

    private void resendOtp(AuthenticationFlowContext context, String phone, SmsConfig config) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        long lastSent = parseLong(authSession.getAuthNote(NOTE_LAST_SEND));
        long now = System.currentTimeMillis();
        if ((now - lastSent) / 1000 < config.resendIntervalSeconds()) {
            throw new IllegalStateException("Resend requested before cooldown");
        }
        LOG.debugf("Resending SMS OTP for authSession=%s after cooldown", authSession.getParentSession().getId());
        String code = generateCode(config.otpLength());
        long expiry = System.currentTimeMillis() + (config.ttlSeconds() * 1000L);
        authSession.setAuthNote(NOTE_CODE, code);
        authSession.setAuthNote(NOTE_EXP, Long.toString(expiry));
        authSession.setAuthNote(NOTE_LAST_SEND, Long.toString(now));
        authSession.setAuthNote(NOTE_ATTEMPTS, "0");
        sendSms(phone, code, config);
    }

    private void sendSms(String phone, String code, SmsConfig config) {
        try {
            SmsSender sender = SmsSender.fromConfig(config);
            LOG.debugf("Dispatching SMS OTP using vendor=%s to maskedPhone=%s", config.vendor(), maskPhone(phone));
            sender.send(phone, "Your verification code is " + code);
        } catch (Exception ex) {
            LOG.error("Failed to dispatch SMS OTP", ex);
            throw new RuntimeException("SMS delivery failed");
        }
    }

    private void challenge(AuthenticationFlowContext context, SmsConfig config, String phone, String errorMessage) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        long lastSent = parseLong(authSession.getAuthNote(NOTE_LAST_SEND));
        long now = System.currentTimeMillis();
        long secondsUntilResend = Math.max(0, config.resendIntervalSeconds() - ((now - lastSent) / 1000));
        if (LOG.isDebugEnabled()) {
            LOG.debugf("Rendering SMS OTP form maskedPhone=%s resendSeconds=%d", maskPhone(phone), secondsUntilResend);
        }
        LoginFormsProvider form = context.form();
        form.setAttribute("resendSeconds", secondsUntilResend);
        form.setAttribute("maskedPhone", maskPhone(phone));
        if (errorMessage != null) {
            form.setError(errorMessage);
        }
        context.challenge(form.createForm("sms-otp.ftl"));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        SmsConfig config = SmsConfig.from(session, context.getAuthenticatorConfig());
        UserModel user = context.getUser();
        String phone = user != null ? user.getFirstAttribute("phoneNumber") : null;
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();
        if (params.containsKey("resend")) {
            try {
                resendOtp(context, phone, config);
                challenge(context, config, phone, null);
            } catch (IllegalStateException ex) {
                LOG.debugf("Resend blocked due to cooldown for user=%s", user != null ? user.getUsername() : "n/a");
                challenge(context, config, phone, "smsResendCooldown");
            }
            return;
        }
        String submitted = params.getFirst("smsCode");
        if (submitted == null || submitted.isBlank()) {
            challenge(context, config, phone, "smsMissingCode");
            return;
        }
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        int attempts = (int) parseLong(authSession.getAuthNote(NOTE_ATTEMPTS));
        if (attempts >= config.maxAttempts()) {
            context.failureChallenge(org.keycloak.authentication.AuthenticationFlowError.INVALID_CREDENTIALS,
                    context.form()
                            .setError("smsMaxAttempts")
                            .createErrorPage(Response.Status.BAD_REQUEST));
            return;
        }
        String code = authSession.getAuthNote(NOTE_CODE);
        long expiry = parseLong(authSession.getAuthNote(NOTE_EXP));
        if (code == null || expiry == 0) {
            sendNewOtp(context, phone, config);
            challenge(context, config, phone, null);
            return;
        }
        if (Instant.now().isAfter(Instant.ofEpochMilli(expiry))) {
            challenge(context, config, phone, "smsCodeExpired");
            return;
        }
        if (!code.equals(submitted.trim())) {
            authSession.setAuthNote(NOTE_ATTEMPTS, Integer.toString(attempts + 1));
            LOG.debugf("Invalid SMS OTP provided. attempt=%d/%d", attempts + 1, config.maxAttempts());
            challenge(context, config, phone, "smsCodeInvalid");
            return;
        }
        LOG.debugf("SMS OTP validated for user=%s", user != null ? user.getUsername() : "n/a");
        clearNotes(authSession);
        context.success();
    }

    private void clearNotes(AuthenticationSessionModel session) {
        session.removeAuthNote(NOTE_CODE);
        session.removeAuthNote(NOTE_EXP);
        session.removeAuthNote(NOTE_ATTEMPTS);
        session.removeAuthNote(NOTE_LAST_SEND);
    }

    private String generateCode(int length) {
        int max = (int) Math.pow(10, length);
        int min = (int) Math.pow(10, length - 1);
        int number = RANDOM.nextInt(max - min) + min;
        return String.format("%0" + length + "d", number);
    }

    private long parseLong(String value) {
        try {
            return value == null ? 0 : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        String last = phone.substring(phone.length() - 2);
        return "***" + last;
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        boolean verified = Boolean.parseBoolean(user.getFirstAttribute("phoneNumberVerified"));
        boolean alreadyQueued = user.getRequiredActionsStream()
                .anyMatch(VerifyPhoneRequiredActionFactory.PROVIDER_ID::equals);
        if (!verified && !alreadyQueued) {
            user.addRequiredAction(VerifyPhoneRequiredActionFactory.PROVIDER_ID);
        }
    }

    @Override
    public void close() {
    }
}
