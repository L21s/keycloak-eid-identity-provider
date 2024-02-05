package com.extension;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class EidClientAvailabilityEndpoint implements RealmResourceProvider {

    private static final Logger logger = LoggerFactory.getLogger(EidClientAvailabilityEndpoint.class);
    KeycloakSession session;

    EidClientAvailabilityEndpoint(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
    }

    //@Inject
    //Template availability;

    @GET
    @Path("availability")
    @Produces(MediaType.TEXT_HTML)
    public Object eIdClientAvailability(@Context UriInfo uriInfo) {
        logger.info("Received a request on availability endpoint. Browser checks eID client availability and calls available endpoint.");
        logger.info("TcTokenRedirectUri is {}", uriInfo.getQueryParameters().getFirst("TcTokenRedirectUri"));

        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Service Availability Check</title>
                </head>
                <body>

                <script>
                    async function checkServiceAvailability() {
                        try {
                            const response = await fetch('http://127.0.0.1:24727/eID-Client?Status');

                            if (response.ok) {
                                window.location.href = 'https://localhost:8443/realms/master/eid-client-availability-endpoint/available?TcTokenRedirectUri='+'%s';
                            } else {
                                renderStatus('Service is unavailable');
                            }
                        } catch (error) {
                            renderStatus('Service is unavailable');
                        }
                    }

                    function renderStatus(status) {
                        const statusElement = document.createElement('p');
                        statusElement.textContent = status;
                        document.body.appendChild(statusElement);
                    }

                    checkServiceAvailability();
                </script>

                </body>
                </html>""", uriInfo.getQueryParameters().getFirst("TcTokenRedirectUri"));
    }

    @GET
    @Path("available")
    @Produces(MediaType.TEXT_HTML)
    public Response available(@Context UriInfo uriInfo) {
        logger.info("Received a request on available endpoint. Try to redirect to tcTokenRedirectUrl.");

        String redirectUri = uriInfo.getRequestUri().getRawQuery().substring(19);
        String tcTokenUri = redirectUri.substring(45);
        String urlEncodedRedirectUri = "http://127.0.0.1:24727/eID-Client?tcTokenURL=" + URLEncoder.encode(tcTokenUri, StandardCharsets.UTF_8);

        logger.info("TcTokenRedirectUrl is {}", urlEncodedRedirectUri);

        try {
            return Response.seeOther(new URI(urlEncodedRedirectUri)).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
