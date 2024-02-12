package com.l21s.keycloak.authentication;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.ClientAuthenticator;
import org.keycloak.authentication.ClientAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class ThirdPartyClientAvailabilityAuthenticatorFactory
    implements ClientAuthenticatorFactory {
  String PROVIDER_ID = "auth-client-third-party-availability";
  ThirdPartyClientAvailabilityAuthenticator SINGLETON =
      new ThirdPartyClientAvailabilityAuthenticator();

  @Override
  public ClientAuthenticator create() {
    return SINGLETON;
  }

  @Override
  public String getDisplayType() {
    return "Third Party Client Availability";
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
  public List<ProviderConfigProperty> getConfigPropertiesPerClient() {
    return null;
  }

  @Override
  public Map<String, Object> getAdapterConfiguration(ClientModel clientModel) {
    return null;
  }

  @Override
  public Set<String> getProtocolAuthenticatorMethods(String s) {
    return null;
  }

  @Override
  public String getHelpText() {
    return "Check if a third party client is available";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return null;
  }

  @Override
  public ClientAuthenticator create(KeycloakSession keycloakSession) {
    return null;
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
