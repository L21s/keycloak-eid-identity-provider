package com.l21s.keycloak.authentication;

import org.keycloak.authentication.ClientAuthenticationFlowContext;
import org.keycloak.authentication.ClientAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThirdPartyClientAvailabilityAuthenticator implements ClientAuthenticator {
  private static final Logger logger =
      LoggerFactory.getLogger(ThirdPartyClientAvailabilityAuthenticator.class);

  @Override
  public void authenticateClient(ClientAuthenticationFlowContext clientAuthenticationFlowContext) {
    logger.info("Check third party client availability");
  }

  @Override
  public void close() {}
}
