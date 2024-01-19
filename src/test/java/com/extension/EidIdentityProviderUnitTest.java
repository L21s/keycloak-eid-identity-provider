package com.extension;

import com.extension.configuration.EidIdentityProviderModel;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EidIdentityProviderUnitTest {

    @Test
    void performLoginRedirectsToAusweisAppAndIncludesTcTokenUrl() throws URISyntaxException {
        AuthenticationRequest request = mock(AuthenticationRequest.class);
        KeycloakSession session = mock(KeycloakSession.class);
        EidIdentityProviderModel config = mock(EidIdentityProviderModel.class);
        AuthenticationSessionModel authSession = mock(AuthenticationSessionModel.class);
        RootAuthenticationSessionModel rootSession = mock(RootAuthenticationSessionModel.class);
        IdentityBrokerState identityBrokerState = mock(IdentityBrokerState.class);
        UriInfo uriInfo = mock(UriInfo.class);
        RealmModel realmModel = mock(RealmModel.class);
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

        Response response = sut.performLogin(request);
        String expectedLocationHeaderValue = "http://127.0.0.1:24727/eID-Client?tcTokenURL="
                + URLEncoder.encode("https://localhost:8443/realms/master/tc-token-endpoint/tc-token?RelayState=state&authSessionId=sessionId", StandardCharsets.UTF_8);
        assertNotNull(response);
        assertEquals(303, response.getStatus());
        assertEquals(expectedLocationHeaderValue, response.getHeaders().getFirst("Location").toString());
    }
}
