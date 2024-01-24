package com.extension;

import com.extension.configuration.EidIdentityProviderModel;
import com.extension.configuration.EidSamlResponseHandler;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EidIdentityProviderUnitTest {

    @Test
    void startAuthenticationWithDesktopClient() {
        try {
            AuthenticationRequest request = mock(AuthenticationRequest.class);
            KeycloakSession session = mock(KeycloakSession.class);
            EidIdentityProviderModel config = mock(EidIdentityProviderModel.class);
            AuthenticationSessionModel authSession = mock(AuthenticationSessionModel.class);
            RootAuthenticationSessionModel rootSession = mock(RootAuthenticationSessionModel.class);
            IdentityBrokerState identityBrokerState = mock(IdentityBrokerState.class);
            UriInfo uriInfo = mock(UriInfo.class);
            RealmModel realmModel = mock(RealmModel.class);
            HttpRequest httpRequest = mock(HttpRequest.class);
            HttpHeaders httpHeaders = mock(HttpHeaders.class);
            EidIdentityProvider sut = new EidIdentityProvider(session, config);

            when(request.getUriInfo()).thenReturn(uriInfo);
            when(uriInfo.getBaseUri()).thenReturn(new URI("https://localhost:8443"));
            when(request.getRealm()).thenReturn(realmModel);
            when(realmModel.getName()).thenReturn("master");
            when(request.getAuthenticationSession()).thenReturn(authSession);
            when(authSession.getParentSession()).thenReturn(rootSession);
            when(request.getAuthenticationSession().getParentSession().getId()).thenReturn("sessionId");
            when(request.getState()).thenReturn(identityBrokerState);
            when(request.getState().getEncoded()).thenReturn("state");
            when(request.getHttpRequest()).thenReturn(httpRequest);
            when(httpRequest.getHttpHeaders()).thenReturn(httpHeaders);
            when(httpHeaders.getRequestHeader("User-Agent")).thenReturn(Arrays.asList("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"));

            Response response = sut.performLogin(request);
            String expectedLocationHeaderValue = "http://127.0.0.1:24727/eID-Client?tcTokenURL="
                    + URLEncoder.encode("https://localhost:8443/realms/master/tc-token-endpoint/tc-token?RelayState=state&authSessionId=sessionId", StandardCharsets.UTF_8);
            assertNotNull(response);
            assertEquals(303, response.getStatus());
            assertEquals(expectedLocationHeaderValue, response.getHeaders().getFirst("Location").toString());
        } catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void startAuthenticationWithMobileClient() {
        try {
            AuthenticationRequest request = mock(AuthenticationRequest.class);
            KeycloakSession session = mock(KeycloakSession.class);
            EidIdentityProviderModel config = mock(EidIdentityProviderModel.class);
            AuthenticationSessionModel authSession = mock(AuthenticationSessionModel.class);
            RootAuthenticationSessionModel rootSession = mock(RootAuthenticationSessionModel.class);
            IdentityBrokerState identityBrokerState = mock(IdentityBrokerState.class);
            UriInfo uriInfo = mock(UriInfo.class);
            RealmModel realmModel = mock(RealmModel.class);
            HttpRequest httpRequest = mock(HttpRequest.class);
            HttpHeaders httpHeaders = mock(HttpHeaders.class);
            EidIdentityProvider sut = new EidIdentityProvider(session, config);

            when(request.getUriInfo()).thenReturn(uriInfo);
            when(uriInfo.getBaseUri()).thenReturn(new URI("https://localhost:8443"));
            when(request.getRealm()).thenReturn(realmModel);
            when(realmModel.getName()).thenReturn("master");
            when(request.getAuthenticationSession()).thenReturn(authSession);
            when(authSession.getParentSession()).thenReturn(rootSession);
            when(request.getAuthenticationSession().getParentSession().getId()).thenReturn("sessionId");
            when(request.getState()).thenReturn(identityBrokerState);
            when(request.getState().getEncoded()).thenReturn("state");
            when(request.getHttpRequest()).thenReturn(httpRequest);
            when(httpRequest.getHttpHeaders()).thenReturn(httpHeaders);
            when(httpHeaders.getRequestHeader("User-Agent")).thenReturn(Arrays.asList("Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/114.0.5735.99 Mobile/15E148 Safari/604.1"));

            Response response = sut.performLogin(request);
            String expectedLocationHeaderValue = "eid://127.0.0.1:24727/eID-Client?tcTokenURL="
                    + URLEncoder.encode("https://localhost:8443/realms/master/tc-token-endpoint/tc-token?RelayState=state&authSessionId=sessionId", StandardCharsets.UTF_8);
            assertNotNull(response);
            assertEquals(303, response.getStatus());
            assertEquals(expectedLocationHeaderValue, response.getHeaders().getFirst("Location").toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void callbackReturnsEidSamlResponseHandler() {
        // given: all arguments for the callback method are available
        KeycloakSession session = mock(KeycloakSession.class);
        EidIdentityProviderModel config = mock(EidIdentityProviderModel.class);
        RealmModel realm = mock(RealmModel.class);
        IdentityProvider.AuthenticationCallback callback = mock(IdentityProvider.AuthenticationCallback.class);
        EventBuilder event = mock(EventBuilder.class);
        EidIdentityProvider sut = new EidIdentityProvider(session, config);

        // when: the callback method is called
        Object result = sut.callback(realm, callback, event);

        // then: a EidSamlResponseHandler object is returned
        assertInstanceOf(EidSamlResponseHandler.class, result);
    }
}
