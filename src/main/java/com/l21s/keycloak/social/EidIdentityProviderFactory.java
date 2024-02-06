package com.l21s.keycloak.social;

import com.l21s.keycloak.social.configuration.EidIdentityProviderModel;
import java.util.List;
import org.keycloak.Config;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class EidIdentityProviderFactory
    extends AbstractIdentityProviderFactory<EidIdentityProvider> {

  public static final String PROVIDER_ID = "eid";

  @Override
  public void init(Config.Scope config) {
    super.init(config);
  }

  @Override
  public String getName() {
    return "eID";
  }

  @Override
  public EidIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
    return new EidIdentityProvider(session, new EidIdentityProviderModel(session, model));
  }

  @Override
  public IdentityProviderModel createConfig() {
    return new IdentityProviderModel();
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return ProviderConfigurationBuilder.create()
        .property()
        .name(EidIdentityProviderModel.ID_PANSTAR_SERVER_URL)
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("ID Panstar Server URL")
        .helpText("ID Panstar Server URL that receives the SAML request for authentication.")
        .required(true)
        .add()
        .property()
        .name(EidIdentityProviderModel.SAML_ENTITY_BASE_URL)
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("SAML Request Entity Base URL")
        .helpText("URI of the entity creating the SAML request.")
        .required(true)
        .add()
        .property()
        .name(EidIdentityProviderModel.SAML_REQUEST_SIGNATURE_PRIVATE_KEY)
        .type(ProviderConfigProperty.FILE_TYPE)
        .label("SAML Request Signature Private Key")
        .helpText("Private key to sign the SAML request.")
        .required(true)
        .add()
        .property()
        .name(EidIdentityProviderModel.SAML_RESPONSE_DECRYPRION_PUBLIC_KEY)
        .type(ProviderConfigProperty.FILE_TYPE)
        .label("SAML Response Decryption Public Key")
        .helpText("Public Key for decrypting the SAML response.")
        .required(true)
        .add()
        .property()
        .name(EidIdentityProviderModel.SAML_RESPONSE_DECRYPRION_PRIVATE_KEY)
        .type(ProviderConfigProperty.FILE_TYPE)
        .label("SAML Response Decryption Private Key")
        .helpText("Private Key for decrypting the SAML response.")
        .required(true)
        .add()
        .property()
        .name(EidIdentityProviderModel.SAML_RESPONSE_VERIFICATION_CERTIFICATE)
        .type(ProviderConfigProperty.FILE_TYPE)
        .label("SAML Response Verification Certificate")
        .helpText("Certificate for verifying the SAML response.")
        .required(true)
        .add()
        .property()
        .name(EidIdentityProviderModel.SAML_REQUEST_ENCRYPTION_CERTIFICATE)
        .type(ProviderConfigProperty.FILE_TYPE)
        .label("SAML Request Encryption Certificate")
        .helpText("Certificate for SAML request encryption.")
        .required(true)
        .add()
        .build();
  }
}
