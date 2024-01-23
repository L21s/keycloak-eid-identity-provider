package com.extension.configuration;

import de.governikus.panstar.sdk.saml.configuration.SamlConfiguration;
import de.governikus.panstar.sdk.saml.response.SamlResponseHandler;
import de.governikus.panstar.sdk.utils.exception.InvalidInputException;
import org.opensaml.core.config.InitializationException;

public interface EidSamlResponseHandlerFactory {
  SamlResponseHandler create(SamlConfiguration samlConfiguration)
      throws InvalidInputException, InitializationException;
}
