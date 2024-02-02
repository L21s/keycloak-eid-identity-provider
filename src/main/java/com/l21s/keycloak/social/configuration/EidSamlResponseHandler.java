package com.l21s.keycloak.social.configuration;

import static org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;

import com.l21s.keycloak.social.EidIdentityProvider;
import de.bund.bsi.eid240.PersonalDataType;
import de.governikus.panstar.sdk.saml.exception.SamlAuthenticationException;
import de.governikus.panstar.sdk.saml.exception.UnsuccessfulSamlAuthenticationProcessException;
import de.governikus.panstar.sdk.saml.response.ProcessedSamlResult;
import de.governikus.panstar.sdk.utils.exception.InvalidInputException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;
import org.opensaml.core.config.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EidSamlResponseHandler {

  private static final Logger logger = LoggerFactory.getLogger(EidSamlResponseHandler.class);
  private final KeycloakSession session;
  private final AuthenticationCallback callback;
  private final EventBuilder event;
  private final EidIdentityProvider eidIdentityProvider;
  private final EidIdentityProviderModel eidIdentityProviderConfig;
  private final RealmModel realm;
  private final SamlResponseHandlerFactory samlResponseHandlerFactory;

  public EidSamlResponseHandler(
      RealmModel realm,
      KeycloakSession session,
      AuthenticationCallback callback,
      EventBuilder event,
      EidIdentityProvider eidIdentityProvider,
      EidIdentityProviderModel eidIdentityProviderConfig,
      SamlResponseHandlerFactory samlResponseHandlerFactory) {
    this.realm = realm;
    this.session = session;
    this.callback = callback;
    this.event = event;
    this.eidIdentityProvider = eidIdentityProvider;
    this.eidIdentityProviderConfig = eidIdentityProviderConfig;
    this.samlResponseHandlerFactory = samlResponseHandlerFactory;
  }

  @GET
  public Response receiveSamlResponse(@Context UriInfo uriInfo) {
    try {
      logger.info(
          "Received a request on SAML response-receiver endpoint. Try to parse a SAML response, set up an identity, and initiate authentication callback.");
      logger.debug("SAML response is {}", uriInfo.getRequestUri().getRawQuery());

      ProcessedSamlResult samlResponse =
          samlResponseHandlerFactory
              .create(eidIdentityProviderConfig.getSamlConfiguration())
              .parseSamlResponse(uriInfo.getRequestUri().getRawQuery());

      logger.info("Successfully parsed SAML response. Try to set up an identity.");

      BrokeredIdentityContext identity =
          new BrokeredIdentityContext(getRestrictedIdString(samlResponse.getPersonalData()));
      AuthenticationSessionModel authSession = getAuthSession(uriInfo, samlResponse);
      setUpIdentity(
          identity, eidIdentityProvider, eidIdentityProviderConfig, authSession, samlResponse);

      logger.info("Successfully set up identity. Initiate authentication callback.");

      return callback.authenticated(identity);
    } catch (InitializationException
        | InvalidInputException
        | SamlAuthenticationException
        | UnsuccessfulSamlAuthenticationProcessException e) {
      throw new RuntimeException(e);
    }
  }

  private AuthenticationSessionModel getAuthSession(
      UriInfo uriInfo, ProcessedSamlResult samlResponse) {
    // we retrieve the root authentication session via the id
    // this is needed as we can't use the "normal" mechanisms as the client that calls this
    // endpoint is the AusweisApp, which is NOT running in the browser and thus, does not
    // have the necessary KC_SESSION_ID cookies etc
    String authSessionId = samlResponse.getInResponseTo().substring(1);
    RootAuthenticationSessionModel rootAuthSession =
        session.authenticationSessions().getRootAuthenticationSession(realm, authSessionId);
    // then we can retrieve the initiating authSession via the tabId
    String relayState = uriInfo.getQueryParameters().getFirst("RelayState");
    IdentityBrokerState identityBrokerState = IdentityBrokerState.encoded(relayState, realm);
    ClientModel client = realm.getClientByClientId(identityBrokerState.getClientId());
    return rootAuthSession.getAuthenticationSession(client, identityBrokerState.getTabId());
  }

  private String getRestrictedIdString(PersonalDataType personalDataType) {
    if (personalDataType == null
        || personalDataType.getRestrictedID() == null
        || ArrayUtils.isEmpty(personalDataType.getRestrictedID().getID())) {
      return null;
    }
    return Hex.encodeHexString(personalDataType.getRestrictedID().getID());
  }

  private void setUpIdentity(
      BrokeredIdentityContext identity,
      EidIdentityProvider eidIdentityProvider,
      EidIdentityProviderModel eidIdentityProviderConfig,
      AuthenticationSessionModel authSession,
      ProcessedSamlResult samlResponse) {
    identity.setIdp(eidIdentityProvider);
    identity.setIdpConfig(eidIdentityProviderConfig);
    identity.setAuthenticationSession(authSession);
    String givenName = samlResponse.getPersonalData().getGivenNames().toLowerCase();
    String familyName = samlResponse.getPersonalData().getFamilyNames().toLowerCase();
    identity.setUsername(givenName + "_" + familyName);
    identity.setFirstName(givenName);
    identity.setLastName(familyName);
  }
}
