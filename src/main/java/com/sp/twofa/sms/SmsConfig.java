package com.sp.twofa.sms;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * Immutable value object that resolves SMS and OTP related configuration
 * from the authenticator config map, realm attributes, or environment variables
 * (in that order).
 */
public record SmsConfig(
        String vendor,
        String apiKey,
        String apiSecret,
        String accountSid,
        String fromNumber,
        String baseUrl,
        String region,
        int timeoutMs,
        int ttlSeconds,
        int otpLength,
        int resendIntervalSeconds,
        int maxAttempts
) {

    private static final Logger LOG = Logger.getLogger(SmsConfig.class);

    public static SmsConfig from(KeycloakSession session, AuthenticatorConfigModel cfg) {
        Map<String, String> direct = cfg != null && cfg.getConfig() != null ? cfg.getConfig() : Collections.emptyMap();
        RealmModel realm = session != null ? session.getContext().getRealm() : null;
        Map<String, String> realmAttrs = realm != null ? realm.getAttributes() : Collections.emptyMap();
        return from(direct, realmAttrs, System::getenv);
    }

    public static SmsConfig from(KeycloakSession session) {
        RealmModel realm = session != null ? session.getContext().getRealm() : null;
        Map<String, String> realmAttrs = realm != null ? realm.getAttributes() : Collections.emptyMap();
        return from(Collections.emptyMap(), realmAttrs, System::getenv);
    }

    public static SmsConfig from(AuthenticatorConfigModel cfg) {
        Map<String, String> direct = cfg != null && cfg.getConfig() != null ? cfg.getConfig() : Collections.emptyMap();
        return from(direct, Collections.emptyMap(), System::getenv);
    }

    public static SmsConfig forTest(Map<String, String> primary, Map<String, String> secondary, java.util.function.Function<String, String> envLookup) {
        return from(primary, secondary, envLookup);
    }

    static SmsConfig from(Map<String, String> primary, Map<String, String> secondary, java.util.function.Function<String, String> envLookup) {
        String vendor = resolve("sms.vendor", primary, secondary, envLookup);
        String apiKey = resolve("sms.apiKey", primary, secondary, envLookup);
        String apiSecret = resolve("sms.apiSecret", primary, secondary, envLookup);
        String accountSid = resolve("sms.accountSid", primary, secondary, envLookup);
        String fromNumber = resolve("sms.fromNumber", primary, secondary, envLookup);
        String baseUrl = resolve("sms.baseUrl", primary, secondary, envLookup);
        String region = resolve("sms.region", primary, secondary, envLookup);
        int timeout = parseInt(resolve("sms.timeoutMs", primary, secondary, envLookup), 7000);
        int ttl = parseInt(resolve("sms.ttlSeconds", primary, secondary, envLookup), 300);
        int otpLength = clamp(parseInt(resolve("sms.otpLength", primary, secondary, envLookup), 6), 4, 10);
        int resend = parseInt(resolve("sms.resendSeconds", primary, secondary, envLookup), 45);
        int attempts = parseInt(resolve("sms.maxAttempts", primary, secondary, envLookup), 5);
        SmsConfig resolved = new SmsConfig(vendor, apiKey, apiSecret, accountSid, fromNumber, baseUrl, region, timeout, ttl, otpLength, resend, attempts);
        if (LOG.isDebugEnabled()) {
            LOG.debugf("Resolved SmsConfig vendor=%s ttl=%d otpLength=%d resend=%d attempts=%d timeout=%d",
                    vendor, ttl, otpLength, resend, attempts, timeout);
        }
        return resolved;
    }

    private static String resolve(String key, Map<String, String> primary, Map<String, String> secondary, java.util.function.Function<String, String> envLookup) {
        if (primary.containsKey(key)) {
            return emptyToNull(primary.get(key));
        }
        if (secondary.containsKey(key)) {
            return emptyToNull(secondary.get(key));
        }
        String envKey = key.replace('.', '_').toUpperCase(Locale.ROOT);
        return emptyToNull(envLookup.apply(envKey));
    }

    private static String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static int parseInt(String value, int defaultValue) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .map(v -> {
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException ex) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
