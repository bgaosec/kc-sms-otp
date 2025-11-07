package com.sp.twofa.sms;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

    public static SmsConfig from(KeycloakSession session, AuthenticatorConfigModel cfg) {
        Map<String, String> direct = cfg != null && cfg.getConfig() != null ? cfg.getConfig() : Collections.emptyMap();
        RealmModel realm = session != null ? session.getContext().getRealm() : null;
        Map<String, String> realmAttrs = realm != null ? realm.getAttributes() : Collections.emptyMap();
        return from(direct, realmAttrs);
    }

    public static SmsConfig from(KeycloakSession session) {
        RealmModel realm = session != null ? session.getContext().getRealm() : null;
        Map<String, String> realmAttrs = realm != null ? realm.getAttributes() : Collections.emptyMap();
        return from(Collections.emptyMap(), realmAttrs);
    }

    public static SmsConfig from(AuthenticatorConfigModel cfg) {
        Map<String, String> direct = cfg != null && cfg.getConfig() != null ? cfg.getConfig() : Collections.emptyMap();
        return from(direct, Collections.emptyMap());
    }

    private static SmsConfig from(Map<String, String> primary, Map<String, String> secondary) {
        String vendor = resolve("sms.vendor", primary, secondary);
        String apiKey = resolve("sms.apiKey", primary, secondary);
        String apiSecret = resolve("sms.apiSecret", primary, secondary);
        String accountSid = resolve("sms.accountSid", primary, secondary);
        String fromNumber = resolve("sms.fromNumber", primary, secondary);
        String baseUrl = resolve("sms.baseUrl", primary, secondary);
        String region = resolve("sms.region", primary, secondary);
        int timeout = parseInt(resolve("sms.timeoutMs", primary, secondary), 7000);
        int ttl = parseInt(resolve("sms.ttlSeconds", primary, secondary), 300);
        int otpLength = clamp(parseInt(resolve("sms.otpLength", primary, secondary), 6), 4, 10);
        int resend = parseInt(resolve("sms.resendSeconds", primary, secondary), 45);
        int attempts = parseInt(resolve("sms.maxAttempts", primary, secondary), 5);
        return new SmsConfig(vendor, apiKey, apiSecret, accountSid, fromNumber, baseUrl, region, timeout, ttl, otpLength, resend, attempts);
    }

    private static String resolve(String key, Map<String, String> primary, Map<String, String> secondary) {
        if (primary.containsKey(key)) {
            return emptyToNull(primary.get(key));
        }
        if (secondary.containsKey(key)) {
            return emptyToNull(secondary.get(key));
        }
        String envKey = key.replace('.', '_').toUpperCase(Locale.ROOT);
        return emptyToNull(System.getenv(envKey));
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
