package com.l21s.keycloak.social.configuration;

import de.governikus.panstar.sdk.saml.configuration.SamlServiceProviderConfiguration;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamlServiceProviderConfigurationImpl implements SamlServiceProviderConfiguration {

  private static final Logger logger =
      LoggerFactory.getLogger(SamlServiceProviderConfigurationImpl.class);
  private final String responseReceiverRealm;
  private final String samlEntityBaseUrl;

  public SamlServiceProviderConfigurationImpl(
      String responseReceiverRealm, String samlEntityBaseUrl) {
    this.responseReceiverRealm = responseReceiverRealm;
    this.samlEntityBaseUrl = samlEntityBaseUrl;
  }

  @Override
  public Optional<URL> getSamlResponseReceiverUrl() {
    try {
      URL responseReceiverUrl =
          new URL(
              samlEntityBaseUrl
                  + String.format("/realms/%s/broker/eid/endpoint", responseReceiverRealm));
      return Optional.of(responseReceiverUrl);
    } catch (MalformedURLException e) {
      logger.debug("Cannot create URL", e);
      throw new IllegalStateException(
          "Cannot create response receiver URL for SAML Sample Demo. Without a response receiver URL the ID PANSTAR cannot send the SAML response to this server",
          e);
    }
  }

  @Override
  public String getSamlEntityId() {
    return samlEntityBaseUrl;
  }
}
