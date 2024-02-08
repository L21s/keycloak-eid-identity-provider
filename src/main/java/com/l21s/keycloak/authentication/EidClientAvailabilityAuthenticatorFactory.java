package com.l21s.keycloak.authentication;

import java.util.List;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class EidClientAvailabilityAuthenticatorFactory implements AuthenticatorFactory {
  public static final String PROVIDER_ID = "auth-eid-availability";
  static EidClientAvailabilityAuthenticator SINGLETON = new EidClientAvailabilityAuthenticator();

  @Override
  public String getDisplayType() {
    return "eID Client Availability";
  }

  @Override
  public String getReferenceCategory() {
    return null;
  }

  @Override
  public boolean isConfigurable() {
    return false;
  }

  @Override
  public Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return "Checks if the eID client is available";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return null;
  }

  @Override
  public Authenticator create(KeycloakSession keycloakSession) {
    return SINGLETON;
  }

  @Override
  public void init(Scope scope) {}

  @Override
  public void postInit(KeycloakSessionFactory keycloakSessionFactory) {}

  @Override
  public void close() {}

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
