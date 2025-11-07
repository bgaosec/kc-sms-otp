# sp-2fa-sms

Custom Keycloak (24+) authenticator that adds SMS-based OTP as an alternative login factor with a pluggable vendor layer plus a "Verify Phone" required action.

## Features
- SMS OTP authenticator (`sp-2fa-sms`) for Browser Flow marked as **ALTERNATIVE** to existing TOTP.
- Verify Phone required action ensures users have a verified `phoneNumber` profile attribute.
- OTP enforcement: configurable digits (4-10), TTL (default 5 min), max attempts (default 5), resend cooldown (default 45 s).
- Vendor abstraction with ready-to-use connectors for Infobip, Africa's Talking, Twilio, Sinch, and MessageBird.
- Config resolved from authenticator config UI, realm attributes, or environment variables so secrets can stay outside Keycloak exports.
- FreeMarker templates (`sms-otp.ftl`, `verify-phone.ftl`) with localized messages.

## Build
Prerequisites: Java 17+ and Maven 3.8+

```bash
mvn package
```

The resulting JAR is located at `target/sp-2fa-sms-0.1.0-SNAPSHOT.jar`.

## Install into Keycloak
1. Copy the built JAR into your Keycloak providers directory, e.g. `cp target/sp-2fa-sms-0.1.0-SNAPSHOT.jar $KEYCLOAK_HOME/providers/`.
2. (Optional) add any vendor certs or CA bundles required for outbound HTTPS.
3. Rebuild the Keycloak image or run `bin/kc.sh build` (or `kc.bat build`) to re-augment.
4. Restart Keycloak.

## Configure Realm
1. **Authenticator:**
   - Go to *Authentication → Flows → Browser*.
   - Add execution → select `SMS OTP (SP)` and set Requirement = `ALTERNATIVE`.
   - In the execution actions menu choose *Config* and fill in SMS settings.
2. **Required Action:**
   - Go to *Authentication → Required Actions*.
   - Enable `Verify Phone Number via SMS` and mark it as *Default Action* if every user must verify.
3. **User Profile:** ensure `phoneNumber` and `phoneNumberVerified` attributes exist (classic Keycloak requires manual creation or user profile config).

## Configuration Keys
| Key | Description |
| --- | --- |
| `sms.vendor` | One of `infobip`, `africastalking`, `twilio`, `sinch`, `messagebird` (required). |
| `sms.fromNumber` | Sender address / phone number (required for most vendors). |
| `sms.apiKey` | API key / token (vendor dependent). |
| `sms.apiSecret` | Secret / auth token (Twilio auth token, Sinch token, etc.). |
| `sms.accountSid` | Account SID / username / service plan id (Twilio SID, Africa's Talking username, Sinch service plan). |
| `sms.baseUrl` | Override base URL for vendors that require regional endpoints. |
| `sms.region` | Optional region identifier. |
| `sms.timeoutMs` | HTTP timeout (default 7000). |
| `sms.ttlSeconds` | OTP expiration window (default 300). |
| `sms.otpLength` | Digits per OTP (4-10, default 6). |
| `sms.resendSeconds` | Cooldown before resend is enabled (default 45). |
| `sms.maxAttempts` | Maximum failed attempts before execution fails (default 5). |

The same keys can be provided via **realm attributes** or **environment variables** (`sms.vendor` → `SMS_VENDOR`, etc.). Precedence: authenticator config → realm attribute → environment.

## Verify Phone Required Action
- Form allows entering a phone number, sending/resending a code, and submitting the OTP.
- Stores verified data in user attributes: `phoneNumber` and `phoneNumberVerified=true`.
- Uses the same resend/TTL settings resolved via `SmsConfig`.

## SMS Vendor Notes
- **Twilio**: requires `sms.accountSid`, `sms.apiSecret` (Auth Token), and `sms.fromNumber`.
- **Africa's Talking**: set `sms.accountSid` to your username, `sms.apiKey` to the API key, optional `sms.fromNumber` for short code/sender id.
- **Infobip** + **MessageBird**: optionally set `sms.baseUrl` to the regional endpoint.
- **Sinch**: `sms.accountSid` = Service Plan ID, `sms.apiSecret` = API token.

## Development Tips
- Add additional vendors by implementing `com.sp.twofa.sms.SmsSender` and extending the switch in `SmsSender.fromConfig`.
- Additional languages: drop new `messages_xx.properties` in `src/main/resources/theme-resources/messages/`.
- To test flows locally, enable DEBUG logging for `com.sp.twofa.sms` and Keycloak's `AuthenticationService` (OTP values are never logged).
