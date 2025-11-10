package com.yourco.sms.otp.testdoubles;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.MessageType;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserModel.RequiredAction;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.rar.AuthorizationDetails;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Simplified LoginFormsProvider that records attributes and last template.
 */
public class FakeLoginFormsProvider implements LoginFormsProvider {

    private final Map<String, Object> attributes = new HashMap<>();
    private String lastTemplate;
    private String lastError;
    private String lastInfo;

    public Map<String, Object> attributes() {
        return attributes;
    }

    public String lastTemplate() {
        return lastTemplate;
    }

    public String lastError() {
        return lastError;
    }

    public String lastInfo() {
        return lastInfo;
    }

    @Override
    public Response createResponse(RequiredAction requiredAction) {
        return Response.ok("required-action:" + requiredAction).build();
    }

    @Override
    public Response createForm(String template) {
        this.lastTemplate = template;
        return Response.ok(template).build();
    }

    @Override
    public String getMessage(String message, Object... parameters) {
        return message;
    }

    @Override
    public LoginFormsProvider setError(String message, Object... parameters) {
        this.lastError = message;
        return this;
    }

    @Override
    public LoginFormsProvider setInfo(String message, Object... parameters) {
        this.lastInfo = message;
        return this;
    }

    @Override
    public LoginFormsProvider setAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    public LoginFormsProvider setSuccess(String message, Object... parameters) {
        return this;
    }

    @Override
    public LoginFormsProvider addError(FormMessage error) {
        this.lastError = error.getMessage();
        return this;
    }

    @Override
    public LoginFormsProvider addSuccess(FormMessage message) {
        this.lastInfo = message.getMessage();
        return this;
    }

    @Override
    public LoginFormsProvider setMessage(MessageType type, String message, Object... parameters) {
        return this;
    }

    @Override
    public LoginFormsProvider setDetachedAuthSession() {
        return this;
    }

    @Override
    public LoginFormsProvider setUser(UserModel user) {
        return this;
    }

    @Override
    public LoginFormsProvider setResponseHeader(String name, String value) {
        return this;
    }

    @Override
    public LoginFormsProvider setFormData(MultivaluedMap<String, String> formData) {
        return this;
    }

    @Override
    public LoginFormsProvider setStatus(Response.Status status) {
        return this;
    }

    @Override
    public LoginFormsProvider setActionUri(URI actionUri) {
        return this;
    }

    @Override
    public LoginFormsProvider setExecution(String execution) {
        return this;
    }

    @Override
    public LoginFormsProvider setAuthContext(org.keycloak.authentication.AuthenticationFlowContext context) {
        return this;
    }

    @Override
    public LoginFormsProvider setAttributeMapper(Function<Map<String, Object>, Map<String, Object>> mapper) {
        Map<String, Object> mapped = mapper.apply(new HashMap<>(attributes));
        attributes.clear();
        attributes.putAll(mapped);
        return this;
    }

    // Remaining methods default to no-op or simple defaults -----------------

    @Override
    public void addScript(String script) {
    }

    @Override
    public Response createLoginUsernamePassword() {
        return Response.ok("login").build();
    }

    @Override
    public Response createLoginUsername() {
        return Response.ok("login-username").build();
    }

    @Override
    public Response createLoginPassword() {
        return Response.ok("login-password").build();
    }

    @Override
    public Response createOtpReset() {
        return Response.ok("otp-reset").build();
    }

    @Override
    public Response createPasswordReset() {
        return Response.ok("password-reset").build();
    }

    @Override
    public Response createLoginTotp() {
        return Response.ok("totp").build();
    }

    @Override
    public Response createLoginRecoveryAuthnCode() {
        return Response.ok("recovery").build();
    }

    @Override
    public Response createLoginWebAuthn() {
        return Response.ok("webauthn").build();
    }

    @Override
    public Response createRegistration() {
        return Response.ok("registration").build();
    }

    @Override
    public Response createInfoPage() {
        return Response.ok("info").build();
    }

    @Override
    public Response createUpdateProfilePage() {
        return Response.ok("update-profile").build();
    }

    @Override
    public Response createIdpLinkConfirmLinkPage() {
        return Response.ok("link-confirm").build();
    }

    @Override
    public Response createIdpLinkConfirmOverrideLinkPage() {
        return Response.ok("link-override").build();
    }

    @Override
    public Response createIdpLinkEmailPage() {
        return Response.ok("link-email").build();
    }

    @Override
    public Response createLoginExpiredPage() {
        return Response.ok("expired").build();
    }

    @Override
    public Response createErrorPage(Response.Status status) {
        this.lastError = status.toString();
        return Response.status(status).build();
    }

    @Override
    public Response createWebAuthnErrorPage() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Override
    public Response createOAuthGrant() {
        return Response.ok("oauth-grant").build();
    }

    @Override
    public Response createSelectAuthenticator() {
        return Response.ok("select-auth").build();
    }

    @Override
    public Response createOAuth2DeviceVerifyUserCodePage() {
        return Response.ok("device").build();
    }

    @Override
    public Response createCode() {
        return Response.ok("code").build();
    }

    @Override
    public Response createX509ConfirmPage() {
        return Response.ok("x509").build();
    }

    @Override
    public Response createSamlPostForm() {
        return Response.ok("saml").build();
    }

    @Override
    public Response createFrontChannelLogoutPage() {
        return Response.ok("front-channel-logout").build();
    }

    @Override
    public Response createLogoutConfirmPage() {
        return Response.ok("logout-confirm").build();
    }

    @Override
    public LoginFormsProvider setAuthenticationSession(AuthenticationSessionModel authenticationSession) {
        return this;
    }

    @Override
    public LoginFormsProvider setClientSessionCode(String code) {
        return this;
    }

    @Override
    public LoginFormsProvider setAccessRequest(List<AuthorizationDetails> accessRequest) {
        return this;
    }

    @Override
    public LoginFormsProvider setErrors(List<FormMessage> errors) {
        return this;
    }

    @Override
    public void close() {
    }
}
