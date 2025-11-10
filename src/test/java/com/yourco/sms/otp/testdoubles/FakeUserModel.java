package com.yourco.sms.otp.testdoubles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.MembershipMetadata;
import org.keycloak.models.RoleModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;

/**
 * Minimal UserModel test double that tracks attributes and required actions.
 * Methods not needed by the tests deliberately throw UnsupportedOperationException
 * to surface accidental usage.
 */
public class FakeUserModel implements UserModel {

    private final String username;
    private final Map<String, List<String>> attributes = new HashMap<>();
    private final Set<String> requiredActions = new LinkedHashSet<>();
    private boolean enabled = true;

    public FakeUserModel(String username) {
        this.username = username;
    }

    public FakeUserModel attribute(String name, String value) {
        setSingleAttribute(name, value);
        return this;
    }

    @Override
    public String getId() {
        return username;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public Long getCreatedTimestamp() {
        return null;
    }

    @Override
    public void setCreatedTimestamp(Long timestamp) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, new ArrayList<>(List.of(value)));
        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        attributes.put(name, new ArrayList<>(values));
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String getFirstAttribute(String name) {
        List<String> values = attributes.get(name);
        return (values == null || values.isEmpty()) ? null : values.get(0);
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        List<String> values = attributes.get(name);
        return values == null ? Stream.empty() : values.stream();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        return requiredActions.stream();
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
    public void addRequiredAction(RequiredAction action) {
        requiredActions.add(action.name());
    }

    @Override
    public void removeRequiredAction(RequiredAction action) {
        requiredActions.remove(action.name());
    }

    @Override
    public String getFirstName() {
        return null;
    }

    @Override
    public void setFirstName(String firstName) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public String getLastName() {
        return null;
    }

    @Override
    public void setLastName(String lastName) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public void setEmail(String email) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public boolean isEmailVerified() {
        return false;
    }

    @Override
    public void setEmailVerified(boolean verified) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public Stream<GroupModel> getGroupsStream() {
        return Stream.empty();
    }

    @Override
    public Stream<GroupModel> getGroupsStream(String search, Integer first, Integer max) {
        return Stream.empty();
    }

    @Override
    public long getGroupsCount() {
        return 0;
    }

    @Override
    public long getGroupsCountByNameContaining(String search) {
        return 0;
    }

    @Override
    public void joinGroup(GroupModel group) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public void joinGroup(GroupModel group, MembershipMetadata metadata) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public void leaveGroup(GroupModel group) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public boolean isMemberOf(GroupModel group) {
        return false;
    }

    @Override
    public String getFederationLink() {
        return null;
    }

    @Override
    public void setFederationLink(String link) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public String getServiceAccountClientLink() {
        return null;
    }

    @Override
    public void setServiceAccountClientLink(String link) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public boolean isFederated() {
        return false;
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        throw new UnsupportedOperationException("not needed");
    }

    // RoleMapperModel methods
    @Override
    public void grantRole(RoleModel role) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        throw new UnsupportedOperationException("not needed");
    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return Stream.empty();
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
        return Stream.empty();
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return false;
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return Stream.empty();
    }
}
