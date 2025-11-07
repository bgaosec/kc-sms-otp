package com.sp.twofa.sms.requiredaction;

import java.time.Instant;

import jakarta.ws.rs.core.MultivaluedMap;

import org.jboss.logging.Logger;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import com.sp.twofa.sms.SmsConfig;
import com.sp.twofa.sms.SmsSender;

public class VerifyPhoneRequiredAction implements RequiredActionProvider {

    private static final Logger LOG = Logger.getLogger(VerifyPhoneRequiredAction.class);
    private static final String NOTE_PHONE = "spVerifyPhone";
    private static final String NOTE_CODE = "spVerifyCode";
    private static final String NOTE_EXP = "spVerifyExp";
    private static final String NOTE_LAST_SEND = "spVerifyLastSend";

    private final KeycloakSession session;

    public VerifyPhoneRequiredAction(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        UserModel user = context.getUser();
        boolean verified = Boolean.parseBoolean(user.getFirstAttribute("phoneNumberVerified"));
        boolean alreadyQueued = user.getRequiredActionsStream()
                .anyMatch(VerifyPhoneRequiredActionFactory.PROVIDER_ID::equals);
        LOG.debugf("verify-phone evaluateTriggers user=%s verified=%s alreadyQueued=%s", user.getUsername(), verified, alreadyQueued);
        if (!verified && !alreadyQueued) {
            user.addRequiredAction(VerifyPhoneRequiredActionFactory.PROVIDER_ID);
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        render(context, null, false);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String phoneInput = params.getFirst("phoneNumber");
        if (params.containsKey("send")) {
            if (phoneInput == null || phoneInput.isBlank()) {
                render(context, "verifyPhoneMissing", false);
                return;
            }
            authSession.setAuthNote(NOTE_PHONE, phoneInput.trim());
            try {
                sendCode(context, phoneInput.trim());
                render(context, "verifyPhoneCodeSent", true);
            } catch (RuntimeException ex) {
                LOG.errorf(ex, "Failed to send verification SMS for user=%s", context.getUser().getUsername());
                render(context, "verifyPhoneSendFailed", false);
            }
            return;
        }
        String submittedCode = params.getFirst("verificationCode");
        String storedCode = authSession.getAuthNote(NOTE_CODE);
        long exp = parseLong(authSession.getAuthNote(NOTE_EXP));
        if (storedCode == null || Instant.now().isAfter(Instant.ofEpochMilli(exp))) {
            render(context, "verifyPhoneCodeExpired", false);
            return;
        }
        if (submittedCode == null || submittedCode.isBlank()) {
            render(context, "verifyPhoneMissingCode", false);
            return;
        }
        if (!storedCode.equals(submittedCode.trim())) {
            render(context, "verifyPhoneInvalidCode", false);
            return;
        }
        UserModel user = context.getUser();
        String phone = authSession.getAuthNote(NOTE_PHONE);
        if (phone != null) {
            user.setSingleAttribute("phoneNumber", phone);
        }
        user.setSingleAttribute("phoneNumberVerified", "true");
        LOG.debugf("Phone verification completed for user=%s", user.getUsername());
        cleanup(authSession);
        context.success();
    }

    private void sendCode(RequiredActionContext context, String phone) {
        SmsConfig config = SmsConfig.from(session);
        SmsSender sender = SmsSender.fromConfig(config);
        String code = generateCode(config.otpLength());
        long expiry = System.currentTimeMillis() + (config.ttlSeconds() * 1000L);
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        long last = parseLong(authSession.getAuthNote(NOTE_LAST_SEND));
        long now = System.currentTimeMillis();
        if ((now - last) / 1000 < config.resendIntervalSeconds()) {
            throw new RuntimeException("Cooldown active");
        }
        try {
            LOG.debugf("Sending verification SMS via vendor=%s to maskedPhone=%s", config.vendor(), mask(phone));
            sender.send(phone, "Your verification code is " + code);
        } catch (Exception e) {
            LOG.error("Failed to send phone verification SMS", e);
            throw new RuntimeException("send failed", e);
        }
        authSession.setAuthNote(NOTE_CODE, code);
        authSession.setAuthNote(NOTE_EXP, Long.toString(expiry));
        authSession.setAuthNote(NOTE_LAST_SEND, Long.toString(now));
    }

    private void render(RequiredActionContext context, String messageKey, boolean success) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        long last = parseLong(authSession.getAuthNote(NOTE_LAST_SEND));
        SmsConfig config = SmsConfig.from(session);
        long now = System.currentTimeMillis();
        long seconds = Math.max(0, config.resendIntervalSeconds() - ((now - last) / 1000));
        String existing = context.getUser().getFirstAttribute("phoneNumber");
        var form = context.form();
        form.setAttribute("currentPhone", existing);
        form.setAttribute("pendingPhone", authSession.getAuthNote(NOTE_PHONE));
        form.setAttribute("resendSeconds", seconds);
        if (LOG.isDebugEnabled()) {
            LOG.debugf("Rendering verify-phone form user=%s resendSeconds=%d", context.getUser().getUsername(), seconds);
        }
        if (messageKey != null) {
            if (success) {
                form.setInfo(messageKey);
            } else {
                form.setError(messageKey);
            }
        }
        context.challenge(form.createForm("verify-phone.ftl"));
    }

    private void cleanup(AuthenticationSessionModel authSession) {
        authSession.removeAuthNote(NOTE_PHONE);
        authSession.removeAuthNote(NOTE_CODE);
        authSession.removeAuthNote(NOTE_EXP);
        authSession.removeAuthNote(NOTE_LAST_SEND);
    }

    private String generateCode(int length) {
        java.security.SecureRandom random = new java.security.SecureRandom();
        int max = (int) Math.pow(10, length);
        int min = (int) Math.pow(10, length - 1);
        int number = random.nextInt(max - min) + min;
        return String.format("%0" + length + "d", number);
    }

    private long parseLong(String value) {
        try {
            return value == null ? 0 : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private String mask(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "***" + phone.substring(phone.length() - 2);
    }

    @Override
    public void close() {
    }
}
