package com.extension.configuration;

import de.governikus.panstar.sdk.saml.configuration.SamlConfiguration;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class EidIdentityProviderModel extends IdentityProviderModel {

  public static final String ID_PANSTAR_SERVER_URL = "idPanstarServerUrl";
  public static final String SAML_ENTITY_BASE_URL = "samlEntityBaseUrl";
  public static final String SAML_REQUEST_SIGNATURE_PRIVATE_KEY = "samlRequestSignaturePrivateKey";
  public static final String SAML_RESPONSE_DECRYPRION_PUBLIC_KEY = "samlResponseDecryptionPublicKey";
  public static final String SAML_RESPONSE_DECRYPRION_PRIVATE_KEY = "samlResponseDecryptionPrivateKey";
  public static final String SAML_RESPONSE_VERIFICATION_CERTIFICATE = "samlResponseVerificationCertificate";
  public static final String SAML_REQUEST_ENCRYPTION_CERTIFICATE = "samlRequestEncryptionCertificate";

  private final KeycloakSession session;

  public EidIdentityProviderModel(KeycloakSession session, IdentityProviderModel model) {
    super(model);
    this.session = session;
  }

  public SamlConfiguration getSamlConfiguration() {
    return new SamlConfigurationImpl(getConfig(), session.getContext().getRealm().getName());
  }
}
