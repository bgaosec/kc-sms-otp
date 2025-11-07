package com.sp.twofa.sms.requiredaction;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class VerifyPhoneRequiredActionFactory implements RequiredActionFactory {

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
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
