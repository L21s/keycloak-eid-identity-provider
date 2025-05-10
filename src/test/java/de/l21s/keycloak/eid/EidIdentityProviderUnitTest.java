package de.l21s.keycloak.eid;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.l21s.keycloak.eid.configuration.EidIdentityProviderModel;
import de.l21s.keycloak.eid.configuration.EidSamlResponseHandler;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

public class EidIdentityProviderUnitTest {

  private static final String REQUEST_STATE =
      "AWDYHOhB3Dy1FcD1rrfLh0eRFAC-t_CSd7G3KpHlb0o.erobji-kr7o.xrUVKpHiRA281FOHHr4wVw";

  @Test
  void startAuthenticationWithDesktopClient() {
    try {
      // given an authentication request with a User-Agent header containing "Macintosh"
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
      when(request.getState().getEncoded()).thenReturn(REQUEST_STATE);
      when(request.getHttpRequest()).thenReturn(httpRequest);
      when(httpRequest.getHttpHeaders()).thenReturn(httpHeaders);
      when(httpHeaders.getRequestHeader("User-Agent"))
          .thenReturn(
              List.of(
                  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"));

      // when the authentication is started
      Response response = sut.performLogin(request);

      // then use the desktop client
      String expectedLocationHeaderValue =
          "http://127.0.0.1:24727/eID-Client?tcTokenURL="
              + URLEncoder.encode(
                  "https://localhost:8443/realms/master/tc-token-endpoint/tc-token?RelayState=erobji-kr7o.c6b5152a-91e2-440d-bcd4-53871ebe3057&authSessionId=sessionId",
                  StandardCharsets.UTF_8);
      assertNotNull(response);
      assertEquals(303, response.getStatus());
      assertEquals(
          expectedLocationHeaderValue, response.getHeaders().getFirst("Location").toString());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void startAuthenticationWithMobileClient() {
    try {
      // given an authentication request with a User-Agent header containing "iPhone"
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
      when(request.getState().getEncoded()).thenReturn(REQUEST_STATE);
      when(request.getHttpRequest()).thenReturn(httpRequest);
      when(httpRequest.getHttpHeaders()).thenReturn(httpHeaders);
      when(httpHeaders.getRequestHeader("User-Agent"))
          .thenReturn(
              List.of(
                  "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/114.0.5735.99 Mobile/15E148 Safari/604.1"));

      // when the authentication is started
      Response response = sut.performLogin(request);

      // then use the mobile client
      String expectedLocationHeaderValue =
          "eid://127.0.0.1:24727/eID-Client?tcTokenURL="
              + URLEncoder.encode(
                  "https://localhost:8443/realms/master/tc-token-endpoint/tc-token?RelayState=erobji-kr7o.c6b5152a-91e2-440d-bcd4-53871ebe3057&authSessionId=sessionId",
                  StandardCharsets.UTF_8);
      assertNotNull(response);
      assertEquals(303, response.getStatus());
      assertEquals(
          expectedLocationHeaderValue, response.getHeaders().getFirst("Location").toString());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void callbackReturnsEidSamlResponseHandler() {
    // given an EidIdentityProvider object
    KeycloakSession session = mock(KeycloakSession.class);
    EidIdentityProviderModel config = mock(EidIdentityProviderModel.class);
    RealmModel realm = mock(RealmModel.class);
    IdentityProvider.AuthenticationCallback callback =
        mock(IdentityProvider.AuthenticationCallback.class);
    EventBuilder event = mock(EventBuilder.class);
    EidIdentityProvider sut = new EidIdentityProvider(session, config);

    // when the callback method is called
    Object result = sut.callback(realm, callback, event);

    // then a EidSamlResponseHandler object is returned
    assertInstanceOf(EidSamlResponseHandler.class, result);
  }
}
