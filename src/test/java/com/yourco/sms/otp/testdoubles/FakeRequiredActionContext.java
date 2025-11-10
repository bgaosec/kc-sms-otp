package com.yourco.sms.otp.testdoubles;

import java.net.URI;

import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionContext.Status;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Minimal RequiredActionContext for exercising VerifyPhoneRequiredAction.
 */
public class FakeRequiredActionContext implements RequiredActionContext {

    private final FakeLoginFormsProvider formsProvider = new FakeLoginFormsProvider();
    private final FakeAuthenticationSessionModel authenticationSession = new FakeAuthenticationSessionModel();
    private final FakeHttpRequest httpRequest = new FakeHttpRequest();
    private final RequiredActionConfigModel configModel = new RequiredActionConfigModel();
    private UserModel user;
    private Response lastChallenge;
    private Status status = Status.CHALLENGE;

    private KeycloakSession session;

    public FakeRequiredActionContext user(UserModel user) {
        this.user = user;
        return this;
    }

    public FakeAuthenticationSessionModel authSession() {
        return authenticationSession;
    }

    public FakeHttpRequest request() {
        return httpRequest;
    }

    public Response lastChallenge() {
        return lastChallenge;
    }

    public Status getRecordedStatus() {
        return status;
    }

    public FakeRequiredActionContext session(KeycloakSession session) {
        this.session = session;
        return this;
    }

    public FakeRequiredActionContext config(String key, String value) {
        if (configModel.getConfig() == null) {
            configModel.setConfig(new java.util.HashMap<>());
        }
        configModel.getConfig().put(key, value);
        return this;
    }

    @Override
    public String getAction() {
        return "verify-phone";
    }

    @Override
    public URI getActionUrl(String code) {
        return URI.create("http://localhost/required-action/" + code);
    }

    @Override
    public URI getActionUrl() {
        return URI.create("http://localhost/required-action");
    }

    @Override
    public LoginFormsProvider form() {
        return formsProvider;
    }

    @Override
    public Response getChallenge() {
        return lastChallenge;
    }

    @Override
    public EventBuilder getEvent() {
        return null;
    }

    @Override
    public UserModel getUser() {
        return user;
    }

    @Override
    public RealmModel getRealm() {
        return null;
    }

    @Override
    public AuthenticationSessionModel getAuthenticationSession() {
        return authenticationSession;
    }

    @Override
    public ClientConnection getConnection() {
        return null;
    }

    @Override
    public jakarta.ws.rs.core.UriInfo getUriInfo() {
        return httpRequest.getUri();
    }

    @Override
    public KeycloakSession getSession() {
        return session;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    @Override
    public RequiredActionConfigModel getConfig() {
        if (configModel.getConfig() == null) {
            configModel.setConfig(new java.util.HashMap<>());
        }
        return configModel;
    }

    @Override
    public String generateCode() {
        return "code";
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void challenge(Response response) {
        this.lastChallenge = response;
    }

    @Override
    public void failure(String userMessage) {
        this.status = Status.FAILURE;
    }

    @Override
    public void failure() {
        this.status = Status.FAILURE;
    }

    @Override
    public void success() {
        this.status = Status.SUCCESS;
    }

    @Override
    public void cancel() {
        this.status = Status.CANCELLED;
    }

    @Override
    public void ignore() {
        this.status = Status.IGNORE;
    }
}
