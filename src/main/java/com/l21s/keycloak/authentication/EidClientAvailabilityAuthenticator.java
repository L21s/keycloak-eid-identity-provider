package com.l21s.keycloak.authentication;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EidClientAvailabilityAuthenticator implements Authenticator {
  private static final Logger logger =
      LoggerFactory.getLogger(EidClientAvailabilityAuthenticator.class);

  @Override
  public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
    logger.info("Check eID client availability");

    if (isAvailable(authenticationFlowContext)) {
      authenticationFlowContext.success();
      return;
    }

    Response challenge =
        authenticationFlowContext
            .form()
            .addError(new FormMessage("eID client is not available."))
            .createErrorPage(Status.SERVICE_UNAVAILABLE);

    authenticationFlowContext.challenge(challenge);
  }

  protected boolean isAvailable(AuthenticationFlowContext authenticationFlowContext) {
    // TODO implement functionality for checking eID client availability
    return false;
  }

  @Override
  public void action(AuthenticationFlowContext authenticationFlowContext) {}

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(
      KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    return true;
  }

  @Override
  public void setRequiredActions(
      KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {}

  @Override
  public void close() {}
}
