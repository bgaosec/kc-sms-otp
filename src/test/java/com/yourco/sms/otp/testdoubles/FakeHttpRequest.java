package com.yourco.sms.otp.testdoubles;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.http.FormPartValue;
import org.keycloak.http.HttpRequest;

/**
 * Simple HttpRequest implementation backed by maps.
 */
public class FakeHttpRequest implements HttpRequest {

    private final MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
    private String method = "POST";

    public FakeHttpRequest put(String key, String value) {
        form.putSingle(key, value);
        return this;
    }

    public MultivaluedMap<String, String> formParameters() {
        return form;
    }

    @Override
    public String getHttpMethod() {
        return method;
    }

    public FakeHttpRequest method(String method) {
        this.method = method;
        return this;
    }

    @Override
    public MultivaluedMap<String, String> getDecodedFormParameters() {
        return form;
    }

    @Override
    public MultivaluedMap<String, FormPartValue> getMultiPartFormParameters() {
        return new MultivaluedHashMap<>();
    }

    @Override
    public HttpHeaders getHttpHeaders() {
        return null;
    }

    @Override
    public java.security.cert.X509Certificate[] getClientCertificateChain() {
        return new java.security.cert.X509Certificate[0];
    }

    @Override
    public UriInfo getUri() {
        return new UriInfo() {
            @Override
            public String getPath() {
                return "/";
            }

            @Override
            public String getPath(boolean decode) {
                return "/";
            }

            @Override
            public List<PathSegment> getPathSegments() {
                return List.of();
            }

            @Override
            public List<PathSegment> getPathSegments(boolean decode) {
                return List.of();
            }

            @Override
            public URI getRequestUri() {
                return URI.create("http://localhost");
            }

            @Override
            public UriBuilder getRequestUriBuilder() {
                return UriBuilder.fromUri(getRequestUri());
            }

            @Override
            public URI getAbsolutePath() {
                return URI.create("http://localhost");
            }

            @Override
            public UriBuilder getAbsolutePathBuilder() {
                return UriBuilder.fromUri(getAbsolutePath());
            }

            @Override
            public URI getBaseUri() {
                return URI.create("http://localhost");
            }

            @Override
            public UriBuilder getBaseUriBuilder() {
                return UriBuilder.fromUri(getBaseUri());
            }

            @Override
            public MultivaluedMap<String, String> getPathParameters() {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedMap<String, String> getPathParameters(boolean decode) {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedMap<String, String> getQueryParameters() {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
                return new MultivaluedHashMap<>();
            }

            @Override
            public List<String> getMatchedURIs() {
                return List.of();
            }

            @Override
            public List<String> getMatchedURIs(boolean decode) {
                return List.of();
            }

            @Override
            public List<Object> getMatchedResources() {
                return List.of();
            }

            @Override
            public URI resolve(URI uri) {
                return uri;
            }

            @Override
            public URI relativize(URI uri) {
                return uri;
            }
        };
    }

    @Override
    public boolean isProxyTrusted() {
        return false;
    }
}
