package de.l21s.keycloak.eid;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class TcTokenEndpointFactory implements RealmResourceProviderFactory {
  public static final String PROVIDER_ID = "tc-token-endpoint";

  @Override
  public RealmResourceProvider create(KeycloakSession keycloakSession) {
    return new TcTokenEndpoint(keycloakSession);
  }

  @Override
  public void init(Config.Scope scope) {}

  @Override
  public void postInit(KeycloakSessionFactory keycloakSessionFactory) {}

  @Override
  public void close() {}

  @Override
  public String getId() {
    return PROVIDER_ID;
  }
}
