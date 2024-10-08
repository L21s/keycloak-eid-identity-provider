package de.l21s.keycloak.eid.configuration;

import de.governikus.panstar.sdk.saml.configuration.SamlConfiguration;
import de.governikus.panstar.sdk.saml.response.SamlResponseHandler;
import de.governikus.panstar.sdk.utils.exception.InvalidInputException;
import org.opensaml.core.config.InitializationException;

public class SamlResponseHandlerFactoryImpl implements SamlResponseHandlerFactory {

  @Override
  public SamlResponseHandler create(SamlConfiguration samlConfiguration)
      throws InvalidInputException, InitializationException {
    return new SamlResponseHandler(samlConfiguration);
  }
}
