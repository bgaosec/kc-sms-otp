# sp-2fa-sms design

## Goals
- Offer SMS OTP as an ALTERNATIVE execution in the browser flow while keeping TOTP available.
- Abstract SMS delivery so multiple vendors can be plugged in/configured without code changes.
- Provide a Verify Phone required action to capture and confirm the user phone number.

## Components

### SmsConfig / SmsSender
- `SmsConfig` resolves effective values from (priority) authenticator config → realm attributes → environment variables.
- Includes vendor credentials plus OTP behavior (length, TTL, resend cooldown, attempts, timeout).
- `SmsSender` factory switches on `sms.vendor` and returns the concrete implementation; a `dummy` sender exists for local testing and only logs payloads.
- Vendor classes live under `com.sp.twofa.sms.sender` and share an HTTP helper based on `java.net.http.HttpClient`.

### SmsOtpAuthenticator
- `Authenticator` id `sp-2fa-sms` with requirement choices `ALTERNATIVE` or `DISABLED`.
- Reads verified phone (`phoneNumber` + `phoneNumberVerified=true`). If missing, it calls `context.attempted()` so the alternative (TOTP) can handle the login.
- Generates an OTP using `SecureRandom`, stores metadata in the authentication session (`code`, `expiry`, `attempts`, `lastSend`).
- Sends the OTP through `SmsSender` and renders `sms-otp.ftl`.
- Enforces TTL, max attempts, and resend cooldown (45s default) while providing localized error messages.
- On success, clears auth notes and returns `context.success()`.
- Automatically queues the Verify Phone required action via `setRequiredActions` when the user is missing a verified phone.

### VerifyPhoneRequiredAction
- Adds itself whenever `phoneNumberVerified` is not truthy.
- Collects a phone number, enforces resend cooldown, and sends a verification SMS using the same config resolution logic (realm attrs/env).
- Stores OTP state in authentication-session notes and updates user attributes once the code matches.
- Template `verify-phone.ftl` renders the UI with resend countdown feedback.

### Themes & Localization
- `sms-otp.ftl` and `verify-phone.ftl` provide UI for login/required action.
- `messages_en.properties` contains string keys used by both forms.

### Packaging & Registration
- Maven project targeting Java 17 / Keycloak 24.0.5.
- `META-INF/services/org.keycloak.provider.ProviderFactory` registers both the authenticator and required-action factories so Keycloak discovers them at runtime.

## Configuration
Key config keys exposed via the authenticator config UI:
- `sms.vendor` (`infobip`, `africastalking`, `twilio`, `sinch`, `messagebird`, `dummy` for dry-runs).
- Credentials (`sms.apiKey`, `sms.apiSecret`, `sms.accountSid`, `sms.fromNumber`, `sms.baseUrl`, `sms.region`).
- Behavior (`sms.timeoutMs`, `sms.ttlSeconds`, `sms.otpLength`, `sms.resendSeconds`, `sms.maxAttempts`).

Realm attributes or environment variables with the same keys can provide defaults/fallbacks for both the authenticator and the required action.

## Security Considerations
- OTPs never logged; only success/failure events recorded.
- Auth-session notes hold transient secrets scoped to the authentication attempt and are cleared on success.
- Enforces TTL, attempt limits, and resend cooldown to reduce brute-force windows.
- Secrets pulled from Keycloak config or env variables, enabling secret injection via Kubernetes/OpenShift.

## Extensibility
- Additional vendors can implement `SmsSender` without touching authenticator logic.
- Failover strategies can be layered by wrapping `SmsSender` before use.
- Message templates localized by adding new `messages_*.properties` files.
