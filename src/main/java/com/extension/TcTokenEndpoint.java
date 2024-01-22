package com.extension;

import com.extension.configuration.SamlConfigurationImpl;
import de.governikus.panstar.sdk.saml.exception.SamlRequestException;
import de.governikus.panstar.sdk.saml.request.SamlRequestGenerator;
import de.governikus.panstar.sdk.utils.RequestData;
import de.governikus.panstar.sdk.utils.constant.LevelOfAssuranceType;
import de.governikus.panstar.sdk.utils.exception.InvalidInputException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import org.opensaml.core.config.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class TcTokenEndpoint implements RealmResourceProvider {

    KeycloakSession session;

    TcTokenEndpoint(KeycloakSession session) {
        this.session = session;
    }

    private static final Logger logger = LoggerFactory.getLogger(TcTokenEndpoint.class);

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
    }

    @GET
    @Path("tc-token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response eIdClientEntrance(@Context UriInfo uriInfo) {
        try {
            logger.info("Received a request on tc-token endpoint. Try to generate a SAML request and redirect to ID PANSTAR.");

            RequestData requestData = new RequestData()
                    .restrictedID(true)
                    .givenNames(false)
                    .familyNames(true)
                    .levelOfAssurance(LevelOfAssuranceType.BUND_NORMAL)
                    .seCertified(true)
                    .seEndorsed(false)
                    .hwKeyStore(true)
                    .cardCertified(true);

            String relayState = getQueryParameterOfKey(uriInfo, "RelayState");
            String authSessionId = "_" + getQueryParameterOfKey(uriInfo, "authSessionId"); // has to start with _ because xml ids can't start with numbers

            logger.debug("RelayState is {}", relayState);
            logger.debug("authSessionId is {}", authSessionId);
            logger.debug("Realm is {}", session.getContext().getRealm().getName());
            logger.debug("SAML configuration is {}", session.getContext().getRealm().getIdentityProviderByAlias("eid").getConfig().toString());

            String completeUrlWithRequest = new SamlRequestGenerator(
                new SamlConfigurationImpl(
                        session.getContext().getRealm().getIdentityProviderByAlias("eid").getConfig(),
                        session.getContext().getRealm().getName()
                )
            ).createSamlRequestUrl(requestData, relayState, authSessionId);

            logger.info("Successfully generated SAML request. eID client will be redirected to ID PANSTAR.");
            logger.debug("Redirect URI is {}", redirectUri);

            URI uri = new URI(completeUrlWithRequest);
            return Response.seeOther(uri).build();
        } catch (InvalidInputException | SamlRequestException | InitializationException e) {
            logger.debug("Could not create SAML request: " + e);
            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    private String getQueryParameterOfKey(UriInfo uriInfo, String key) {
        return uriInfo.getQueryParameters().getFirst(key);
    }
}
