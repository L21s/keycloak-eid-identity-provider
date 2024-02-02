package com.l21s.keycloak.social.configuration;

import de.governikus.panstar.sdk.saml.configuration.SamlEidServerConfiguration;

public class SamlEidServerConfigurationImpl implements SamlEidServerConfiguration {

  public String idPanstarServerUrl;

  public SamlEidServerConfigurationImpl(String idPanstarServerUrl) {
    this.idPanstarServerUrl = idPanstarServerUrl;
  }

  @Override
  public String getSamlRequestReceiverUrl() {
    return idPanstarServerUrl;
  }
}
