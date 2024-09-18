package de.l21s.keycloak.eid.configuration;

import de.governikus.panstar.sdk.saml.configuration.SamlConfiguration;
import de.governikus.panstar.sdk.saml.configuration.SamlEidServerConfiguration;
import de.governikus.panstar.sdk.saml.configuration.SamlKeyMaterial;
import de.governikus.panstar.sdk.saml.configuration.SamlServiceProviderConfiguration;
import java.util.Map;

public class SamlConfigurationImpl implements SamlConfiguration {

  private final SamlKeyMaterial samlKeyMaterial;

  private final SamlEidServerConfiguration samlEidServerConfiguration;

  private final SamlServiceProviderConfiguration samlServiceProviderConfiguration;

  public SamlConfigurationImpl(Map<String, String> config, String responseReceiverRealm) {
    this.samlKeyMaterial =
        new SamlKeyMaterialImpl(
            config.get(EidIdentityProviderModel.SAML_REQUEST_SIGNATURE_PRIVATE_KEY),
            config.get(EidIdentityProviderModel.SAML_RESPONSE_DECRYPRION_PUBLIC_KEY),
            config.get(EidIdentityProviderModel.SAML_RESPONSE_DECRYPRION_PRIVATE_KEY),
            config.get(EidIdentityProviderModel.SAML_RESPONSE_VERIFICATION_CERTIFICATE),
            config.get(EidIdentityProviderModel.SAML_REQUEST_ENCRYPTION_CERTIFICATE));
    this.samlEidServerConfiguration =
        new SamlEidServerConfigurationImpl(
            config.get(EidIdentityProviderModel.ID_PANSTAR_SERVER_URL));
    this.samlServiceProviderConfiguration =
        new SamlServiceProviderConfigurationImpl(
            responseReceiverRealm, config.get(EidIdentityProviderModel.SAML_ENTITY_BASE_URL));
  }

  @Override
  public SamlKeyMaterial getSamlKeyMaterial() {
    return samlKeyMaterial;
  }

  @Override
  public SamlEidServerConfiguration getSamlEidServerConfiguration() {
    return samlEidServerConfiguration;
  }

  @Override
  public SamlServiceProviderConfiguration getSamlServiceProviderConfiguration() {
    return samlServiceProviderConfiguration;
  }
}
