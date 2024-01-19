package com.extension;

import com.extension.configuration.EidIdentityProviderModel;
import com.extension.configuration.SamlResponseReceiverEndpoint;
import de.governikus.panstar.sdk.utils.TcTokenUtils;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.keycloak.broker.provider.*;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.stream.Stream;

public class EidIdentityProvider extends AbstractIdentityProvider<EidIdentityProviderModel> {

  private final EidIdentityProviderModel config;

  public EidIdentityProvider(KeycloakSession session, EidIdentityProviderModel config) {
    super(session, config);
    this.config = config;
  }

  private static final Logger logger = LoggerFactory.getLogger(EidIdentityProvider.class);

  @Override
  public Object callback(RealmModel realm, IdentityProvider.AuthenticationCallback callback, EventBuilder event) {
    return new SamlResponseReceiverEndpoint(realm, session, callback, event, this, config);
  }

  @Override
  public Response performLogin(AuthenticationRequest request) {
    String authSessionId = request.getAuthenticationSession().getParentSession().getId();
    String relayState = request.getState().getEncoded();

    String tcTokenUrl =
        UriBuilder.fromUri(request.getUriInfo().getBaseUri())
            .path("realms")
            .path(request.getRealm().getName())
            .path("tc-token-endpoint")
            .path("tc-token")
            .queryParam("RelayState", relayState)
            .queryParam("authSessionId", authSessionId)
            .build()
            .toString();
    logger.debug("tcTokenUrl is {}", tcTokenUrl);

    try {
      logger.debug("Request AusweisApp to initialize SAML authentication flow.");
      String userAgentHeader = request.getHttpRequest().getHttpHeaders().getRequestHeader("User-Agent").toString();
      boolean isMobileClient = Stream.of("iPhone", "Android", "Windows Phone").anyMatch(userAgentHeader::contains);

      URI tcTokenRedirectUri = null;
      if (isMobileClient) {
        tcTokenRedirectUri = new URI(TcTokenUtils.getMobileEidClientUrl(tcTokenUrl));
      }
      if (!isMobileClient) {
        tcTokenRedirectUri = new URI(TcTokenUtils.getStationaryEidClientUrl(tcTokenUrl));
      }
      logger.debug("TcTokenRedirectUrl is {}", tcTokenRedirectUri);

      return Response.seeOther(tcTokenRedirectUri).build();
    } catch (Exception e) {
      throw new IdentityBrokerException("Could not create authentication request.", e);
    }
  }

  @Override
  public Response retrieveToken(KeycloakSession keycloakSession, FederatedIdentityModel federatedIdentityModel) {
    return Response.ok(federatedIdentityModel.getToken()).type(MediaType.APPLICATION_JSON).build();
  }

  @Override
  public boolean isMapperSupported(IdentityProviderMapper mapper) {
    return super.isMapperSupported(mapper);
  }
}
