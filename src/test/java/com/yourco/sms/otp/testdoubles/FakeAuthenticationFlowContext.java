package com.yourco.sms.otp.testdoubles;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationSelectionOption;
import org.keycloak.authentication.FlowStatus;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.BruteForceProtector;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.services.messages.Messages;

/**
 * AuthenticationFlowContext stub that captures responses for assertions.
 */
public class FakeAuthenticationFlowContext implements AuthenticationFlowContext {

    private final FakeLoginFormsProvider formsProvider = new FakeLoginFormsProvider();
    private final FakeAuthenticationSessionModel authenticationSession = new FakeAuthenticationSessionModel();
    private final FakeHttpRequest httpRequest = new FakeHttpRequest();
    private final AuthenticatorConfigModel configModel = new AuthenticatorConfigModel();
    private UserModel user;
    private Response lastChallenge;
    private AuthenticationFlowError lastError;
    private boolean success;
    private boolean attempted;

    private KeycloakSession session;

    public FakeAuthenticationFlowContext user(UserModel user) {
        this.user = user;
        return this;
    }

    public FakeLoginFormsProvider forms() {
        return formsProvider;
    }

    public FakeAuthenticationFlowContext config(String key, String value) {
        if (configModel.getConfig() == null) {
            configModel.setConfig(new java.util.HashMap<>());
        }
        configModel.getConfig().put(key, value);
        return this;
    }

    public FakeAuthenticationFlowContext session(KeycloakSession session) {
        this.session = session;
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

    public AuthenticationFlowError lastError() {
        return lastError;
    }

    public boolean successCalled() {
        return success;
    }

    public boolean attemptedCalled() {
        return attempted;
    }

    @Override
    public UserModel getUser() {
        return user;
    }

    @Override
    public void setUser(UserModel user) {
        this.user = user;
    }

    @Override
    public List<AuthenticationSelectionOption> getAuthenticationSelections() {
        return List.of();
    }

    @Override
    public void setAuthenticationSelections(List<AuthenticationSelectionOption> selections) {
    }

    @Override
    public void clearUser() {
        this.user = null;
    }

    @Override
    public void attachUserSession(org.keycloak.models.UserSessionModel userSession) {
    }

    @Override
    public AuthenticationSessionModel getAuthenticationSession() {
        return authenticationSession;
    }

    @Override
    public String getFlowPath() {
        return "/auth";
    }

    @Override
    public LoginFormsProvider form() {
        return formsProvider;
    }

    @Override
    public URI getActionUrl(String code) {
        return URI.create("http://localhost/action/" + code);
    }

    @Override
    public URI getActionTokenUrl(String token) {
        return URI.create("http://localhost/action-token/" + token);
    }

    @Override
    public URI getRefreshExecutionUrl() {
        return URI.create("http://localhost/refresh");
    }

    @Override
    public URI getRefreshUrl(boolean authSessionIdParam) {
        return URI.create("http://localhost/refresh");
    }

    @Override
    public void cancelLogin() {
    }

    @Override
    public void resetFlow() {
    }

    @Override
    public void resetFlow(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void fork() {
    }

    @Override
    public void forkWithSuccessMessage(org.keycloak.models.utils.FormMessage message) {
    }

    @Override
    public void forkWithErrorMessage(org.keycloak.models.utils.FormMessage message) {
    }

    @Override
    public EventBuilder getEvent() {
        return null;
    }

    @Override
    public EventBuilder newEvent() {
        return null;
    }

    @Override
    public AuthenticationExecutionModel getExecution() {
        return null;
    }

    @Override
    public AuthenticationFlowModel getTopLevelFlow() {
        return null;
    }

    @Override
    public RealmModel getRealm() {
        return null;
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
    public BruteForceProtector getProtector() {
        return null;
    }

    @Override
    public AuthenticatorConfigModel getAuthenticatorConfig() {
        return configModel;
    }

    @Override
    public org.keycloak.models.utils.FormMessage getForwardedErrorMessage() {
        return null;
    }

    @Override
    public org.keycloak.models.utils.FormMessage getForwardedSuccessMessage() {
        return null;
    }

    @Override
    public org.keycloak.models.utils.FormMessage getForwardedInfoMessage() {
        return null;
    }

    @Override
    public void setForwardedInfoMessage(String message, Object... parameters) {
    }

    @Override
    public String generateAccessCode() {
        return "code";
    }

    @Override
    public AuthenticationExecutionModel.Requirement getCategoryRequirementFromCurrentFlow(String category) {
        return AuthenticationExecutionModel.Requirement.ALTERNATIVE;
    }

    @Override
    public void success() {
        success = true;
    }

    @Override
    public void success(String execution) {
        success();
    }

    @Override
    public void failure(AuthenticationFlowError error) {
        this.lastError = error;
    }

    @Override
    public void failure(AuthenticationFlowError error, Response response) {
        this.lastError = error;
        this.lastChallenge = response;
    }

    @Override
    public void failure(AuthenticationFlowError error, Response response, String eventError, String eventNote) {
        failure(error, response);
    }

    @Override
    public void challenge(Response response) {
        this.lastChallenge = response;
    }

    @Override
    public void forceChallenge(Response response) {
        this.lastChallenge = response;
    }

    @Override
    public void failureChallenge(AuthenticationFlowError error, Response response) {
        this.lastError = error;
        this.lastChallenge = response;
    }

    @Override
    public void attempted() {
        this.attempted = true;
    }

    @Override
    public FlowStatus getStatus() {
        return success ? FlowStatus.SUCCESS : FlowStatus.FAILED;
    }

    @Override
    public AuthenticationFlowError getError() {
        return lastError;
    }

    @Override
    public String getEventDetails() {
        return Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR;
    }

    @Override
    public String getUserErrorMessage() {
        return lastError != null ? lastError.name() : null;
    }
}
