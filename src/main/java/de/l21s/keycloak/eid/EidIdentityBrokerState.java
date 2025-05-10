package de.l21s.keycloak.eid;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.regex.Pattern;
import org.keycloak.common.util.Base64Url;

public class EidIdentityBrokerState {

  private static final Pattern DOT = Pattern.compile("\\.");

  private final String clientId;
  private final String tabId;
  private final String encoded;

  private EidIdentityBrokerState(String clientId, String tabId, String encoded) {
    this.clientId = clientId;
    this.tabId = tabId;
    this.encoded = encoded;
  }

  public static EidIdentityBrokerState fromState(String state) {
    String[] parts = DOT.split(state, 4);
    if (parts.length < 3) {
      throw new IllegalArgumentException("Invalid state: " + state);
    }
    String tabId = parts[1];
    String encodedClientId = parts[2];

    byte[] decodedClientId = Base64Url.decode(encodedClientId);
    ByteBuffer bb = ByteBuffer.wrap(decodedClientId);
    long first = bb.getLong();
    long second = bb.getLong();
    UUID clientDbUuid = new UUID(first, second);
    String clientIdInDb = clientDbUuid.toString();

    String encodedState = tabId + "." + clientIdInDb;
    return new EidIdentityBrokerState(clientIdInDb, tabId, encodedState);
  }

  public static EidIdentityBrokerState fromRelayState(String relayState) {
    String[] parts = DOT.split(relayState, 2);
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid RelayState: " + relayState);
    }
    String tabId = parts[0];
    String clientId = parts[1];

    String encodedState = tabId + "." + clientId;
    return new EidIdentityBrokerState(clientId, tabId, encodedState);
  }

  public String getClientId() {
    return clientId;
  }

  public String getTabId() {
    return tabId;
  }

  public String getEncoded() {
    return encoded;
  }
}
