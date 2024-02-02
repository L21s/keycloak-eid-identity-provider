package com.extension;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

public class EidClientAvailabilityEndpointFactory implements RealmResourceProviderFactory {
    public static final String PROVIDER_ID = "eid-client-availability-endpoint";

    @Override
    public RealmResourceProvider create(KeycloakSession keycloakSession) {
        return new EidClientAvailabilityEndpoint(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
