package com.sp.twofa.sms.auth;

import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class SmsOtpAuthenticatorFactory implements AuthenticatorFactory {

    private static final Logger LOG = Logger.getLogger(SmsOtpAuthenticatorFactory.class);

    private static final Requirement[] REQUIREMENT_CHOICES = {
            Requirement.ALTERNATIVE,
            Requirement.DISABLED
    };

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = List.of(
            property("sms.vendor", ProviderConfigProperty.STRING_TYPE, "SMS Vendor", "infobip, africastalking, twilio, sinch, messagebird", true),
            property("sms.fromNumber", ProviderConfigProperty.STRING_TYPE, "From Number", "Sender id or phone", true),
            property("sms.baseUrl", ProviderConfigProperty.STRING_TYPE, "Base URL", "Optional override for vendor endpoint", false),
            property("sms.apiKey", ProviderConfigProperty.PASSWORD, "API Key", "Vendor API key or token", false),
            property("sms.apiSecret", ProviderConfigProperty.PASSWORD, "API Secret", "Secret/token", false),
            property("sms.accountSid", ProviderConfigProperty.STRING_TYPE, "Account SID / Username", "Twilio SID, Africa's Talking username or Sinch plan id", false),
            property("sms.region", ProviderConfigProperty.STRING_TYPE, "Region", "Optional vendor region", false),
            property("sms.timeoutMs", ProviderConfigProperty.STRING_TYPE, "HTTP Timeout (ms)", "Request timeout; default 7000", false),
            property("sms.ttlSeconds", ProviderConfigProperty.STRING_TYPE, "OTP TTL (seconds)", "Validity window; default 300", false),
            property("sms.otpLength", ProviderConfigProperty.STRING_TYPE, "OTP Length", "Digits between 4-10; default 6", false),
            property("sms.resendSeconds", ProviderConfigProperty.STRING_TYPE, "Resend Cooldown (seconds)", "Default 45", false),
            property("sms.maxAttempts", ProviderConfigProperty.STRING_TYPE, "Max Attempts", "Default 5", false)
    );

    private static ProviderConfigProperty property(String name, String type, String label, String helpText, boolean required) {
        ProviderConfigProperty p = new ProviderConfigProperty();
        p.setName(name);
        p.setType(type);
        p.setLabel(label);
        p.setHelpText(helpText);
        p.setRequired(required);
        return p;
    }

    @Override
    public String getHelpText() {
        return "Send and validate SMS OTP codes using pluggable vendor backends.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        LOG.debugf("Creating authenticator instance for realm=%s", session.getContext().getRealm() != null ? session.getContext().getRealm().getName() : "n/a");
        return new SmsOtpAuthenticator(session);
    }

    @Override
    public void init(Scope config) {
        LOG.infof("Initializing %s provider (build %s)", getDisplayType(), version());
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        LOG.infof("%s provider registered successfully", getDisplayType());
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return SmsOtpAuthenticator.ID;
    }

    @Override
    public String getDisplayType() {
        return "SMS OTP (SP)";
    }

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    private String version() {
        Package pkg = SmsOtpAuthenticatorFactory.class.getPackage();
        return pkg != null && pkg.getImplementationVersion() != null
                ? pkg.getImplementationVersion()
                : "dev";
    }
}
