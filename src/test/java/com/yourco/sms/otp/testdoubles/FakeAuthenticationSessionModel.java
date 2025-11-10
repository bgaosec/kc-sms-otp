package com.yourco.sms.otp.testdoubles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.CommonClientSessionModel.ExecutionStatus;
import org.keycloak.sessions.RootAuthenticationSessionModel;

/**
 * Simple in-memory AuthenticationSessionModel for unit tests.
 */
public class FakeAuthenticationSessionModel implements AuthenticationSessionModel {

    private final Map<String, String> authNotes = new HashMap<>();
    private final Map<String, String> clientNotes = new HashMap<>();
    private final Map<String, String> userSessionNotes = new HashMap<>();
    private final Map<String, ExecutionStatus> executionStatus = new HashMap<>();
    private final Set<String> requiredActions = new HashSet<>();
    private final Set<String> clientScopes = new HashSet<>();

    private final RootAuthenticationSessionModel rootSession = new RootAuthenticationSessionModel() {
        private String id = "root-session";

        @Override
        public String getId() {
            return id;
        }

        @Override
        public RealmModel getRealm() {
            return realm;
        }

        @Override
        public int getTimestamp() {
            return (int) (System.currentTimeMillis() / 1000);
        }

        @Override
        public void setTimestamp(int timestamp) {
        }

        @Override
        public Map<String, AuthenticationSessionModel> getAuthenticationSessions() {
            return Map.of(id, FakeAuthenticationSessionModel.this);
        }

        @Override
        public AuthenticationSessionModel getAuthenticationSession(ClientModel client, String tabId) {
            return FakeAuthenticationSessionModel.this;
        }

        @Override
        public AuthenticationSessionModel createAuthenticationSession(ClientModel client) {
            return FakeAuthenticationSessionModel.this;
        }

        @Override
        public void removeAuthenticationSessionByTabId(String tabId) {
        }

        @Override
        public void restartSession(RealmModel realm) {
        }
    };

    private RealmModel realm;
    private ClientModel client;
    private String action;
    private String protocol = "openid-connect";
    private String redirectUri;

    public FakeAuthenticationSessionModel realm(RealmModel realm) {
        this.realm = realm;
        return this;
    }

    public FakeAuthenticationSessionModel client(ClientModel client) {
        this.client = client;
        return this;
    }

    @Override
    public String getTabId() {
        return "tab";
    }

    @Override
    public RootAuthenticationSessionModel getParentSession() {
        return rootSession;
    }

    @Override
    public Map<String, ExecutionStatus> getExecutionStatus() {
        return executionStatus;
    }

    @Override
    public void setExecutionStatus(String authenticator, ExecutionStatus status) {
        executionStatus.put(authenticator, status);
    }

    @Override
    public void clearExecutionStatus() {
        executionStatus.clear();
    }

    @Override
    public org.keycloak.models.UserModel getAuthenticatedUser() {
        return null;
    }

    @Override
    public void setAuthenticatedUser(org.keycloak.models.UserModel user) {
    }

    @Override
    public Set<String> getRequiredActions() {
        return requiredActions;
    }

    @Override
    public void addRequiredAction(String action) {
        requiredActions.add(action);
    }

    @Override
    public void removeRequiredAction(String action) {
        requiredActions.remove(action);
    }

    @Override
    public void addRequiredAction(org.keycloak.models.UserModel.RequiredAction action) {
        requiredActions.add(action.name());
    }

    @Override
    public void removeRequiredAction(org.keycloak.models.UserModel.RequiredAction action) {
        requiredActions.remove(action.name());
    }

    @Override
    public void setUserSessionNote(String name, String value) {
        userSessionNotes.put(name, value);
    }

    @Override
    public Map<String, String> getUserSessionNotes() {
        return userSessionNotes;
    }

    @Override
    public void clearUserSessionNotes() {
        userSessionNotes.clear();
    }

    @Override
    public String getAuthNote(String name) {
        return authNotes.get(name);
    }

    @Override
    public void setAuthNote(String name, String value) {
        authNotes.put(name, value);
    }

    @Override
    public void removeAuthNote(String name) {
        authNotes.remove(name);
    }

    @Override
    public void clearAuthNotes() {
        authNotes.clear();
    }

    @Override
    public String getClientNote(String name) {
        return clientNotes.get(name);
    }

    @Override
    public void setClientNote(String name, String value) {
        clientNotes.put(name, value);
    }

    @Override
    public void removeClientNote(String name) {
        clientNotes.remove(name);
    }

    @Override
    public Map<String, String> getClientNotes() {
        return clientNotes;
    }

    @Override
    public void clearClientNotes() {
        clientNotes.clear();
    }

    @Override
    public Set<String> getClientScopes() {
        return clientScopes;
    }

    @Override
    public void setClientScopes(Set<String> scopes) {
        clientScopes.clear();
        if (scopes != null) {
            clientScopes.addAll(scopes);
        }
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public void setRedirectUri(String uri) {
        this.redirectUri = uri;
    }

    @Override
    public RealmModel getRealm() {
        return realm;
    }

    @Override
    public ClientModel getClient() {
        return client;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
