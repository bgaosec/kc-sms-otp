package com.sp.twofa.sms.requiredaction;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class VerifyPhoneRequiredActionFactory implements RequiredActionFactory {

    private static final Logger LOG = Logger.getLogger(VerifyPhoneRequiredActionFactory.class);

    public static final String PROVIDER_ID = "verify-phone-sms";

    @Override
    public String getDisplayText() {
        return "Verify Phone Number via SMS";
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new VerifyPhoneRequiredAction(session);
    }

    @Override
    public void init(Scope config) {
        LOG.infof("Initializing %s required action (build %s)", getDisplayText(), version());
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        LOG.infof("%s required action registered", getDisplayText());
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    private String version() {
        Package pkg = VerifyPhoneRequiredActionFactory.class.getPackage();
        return pkg != null && pkg.getImplementationVersion() != null
                ? pkg.getImplementationVersion()
                : "dev";
    }
}
