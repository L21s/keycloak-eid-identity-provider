package de.l21s.keycloak.eid;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class EidIdentityProviderIntegratedTest {

  @Container
  KeycloakContainer keycloak =
      new KeycloakContainer()
          .withProviderClassesFrom("target/deploy")
          .withRealmImportFile("/realm.json");

  @Test
  @Disabled
  void keycloakIsRunning() {
    assertTrue(keycloak.isRunning());
  }
}
