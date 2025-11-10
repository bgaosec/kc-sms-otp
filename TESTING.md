# Testing sp-2fa-sms

## Requirements
- Java 21+
- Maven 3.8+

## Running the suite
Execute all unit + integration tests:

```bash
mvn test
```

This runs JUnit 5 tests covering OTP generation, SmsConfig parsing, resend/expiry logic, service-loader wiring, dummy sender logging, authenticator + required-action flows, WireMock-backed vendor connectors, and the standalone CLI driver.

### Standalone driver
You can run the CLI driver without Keycloak:

```bash
mvn -Dtest=com.yourco.sms.otp.StandaloneDriverIT test
```

or execute it directly:

```bash
mvn -q -DskipTests package
java -cp target/sp-2fa-sms-0.1.0-SNAPSHOT.jar:target/test-classes \
  com.yourco.sms.otp.driver.StandaloneDriver <<'EOI'
+12025550123
888888
888888
EOI
```

## Test doubles
Reusable fakes for `AuthenticationFlowContext`, `RequiredActionContext`, `AuthenticationSessionModel`, `LoginFormsProvider`, `HttpRequest`, and `UserModel` live under `src/test/java/com/yourco/sms/otp/testdoubles/` and enable exercising the SPI outside of Keycloak.
