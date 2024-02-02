package de.governikus.panstar.sdk.saml.response;

import de.governikus.panstar.sdk.saml.configuration.SamlConfiguration;
import de.governikus.panstar.sdk.utils.exception.InvalidInputException;
import org.jetbrains.annotations.NotNull;
import org.opensaml.core.config.InitializationException;

public class SamlResponseHandlerWithoutTimeAssertion extends SamlResponseHandler {

  /**
   * Create an instance of {@link SamlResponseHandler}.
   *
   * @param samlConfiguration instance of {@link SamlConfiguration}.
   * @throws InitializationException if OpenSAML could not be initialized
   */
  public SamlResponseHandlerWithoutTimeAssertion(@NotNull SamlConfiguration samlConfiguration)
      throws InitializationException, InvalidInputException {
    super(samlConfiguration);
    setCheckAssertionTime(false);
  }
}
