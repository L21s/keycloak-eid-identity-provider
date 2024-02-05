package com.extension;

import com.extension.configuration.EidIdentityProviderModel;
import com.extension.configuration.EidSamlResponseHandler;
import com.extension.configuration.SamlResponseHandlerFactoryImpl;
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
    return new EidSamlResponseHandler(realm, session, callback, event, this, config, new SamlResponseHandlerFactoryImpl());
  }

  @Override
  public Response performLogin(AuthenticationRequest request) {
    logger.info("Requested login with eID. Try to generate TcTokenUri and redirect to AusweisApp.");

    String authSessionId = request.getAuthenticationSession().getParentSession().getId();
    String relayState = request.getState().getEncoded();

    logger.debug("authSessionId is {}", authSessionId);
    logger.debug("RelayState is {}", relayState);

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

    logger.debug("TcTokenUrl is {}", tcTokenUrl);

    try {
      String userAgentHeader = request.getHttpRequest().getHttpHeaders().getRequestHeader("User-Agent").toString();
      boolean isMobileClient = Stream.of("iPhone", "Android", "Windows Phone").anyMatch(userAgentHeader::contains);

      String tcTokenRedirectUri = null;
      if (isMobileClient) {
        tcTokenRedirectUri = TcTokenUtils.getMobileEidClientUrl(tcTokenUrl);
      }
      if (!isMobileClient) {
        tcTokenRedirectUri = TcTokenUtils.getStationaryEidClientUrl(tcTokenUrl);
      }

      logger.debug("TcTokenRedirectUri is {}", tcTokenRedirectUri);
      logger.info("Successfully generated TcTokenUri. Redirect to AusweisApp.");

      String redirectUriString = String.format("https://localhost:8443/realms/master/eid-client-availability/check/?TcTokenRedirectUri=%s", tcTokenRedirectUri);

      logger.info("Browser checks availability of AusweisApp with {}", redirectUriString);

      return Response.seeOther(new URI(redirectUriString)).build();
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
