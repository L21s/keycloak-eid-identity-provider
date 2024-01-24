package com.extension;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TcTokenEndpointUnitTest {

    @Test
    void createsSamlRequestAndRedirectToIdPanstarServer() {
        // given a TcTokenEndPoint object
        KeycloakSession session = mock(KeycloakSession.class);
        UriInfo uriInfo = mock(UriInfo.class);
        KeycloakContext context = mock(KeycloakContext.class);
        RealmModel realm = mock(RealmModel.class);
        IdentityProviderModel model = mock(IdentityProviderModel.class);
        MultivaluedHashMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.put("RelayState", List.of("relayState"));
        queryParameters.put("authSessionId", List.of("authSessionId"));
        String workingDir = new File("src/test/resources").getAbsolutePath();
        Map<String, String> modelConfigMap = new HashMap<>();
        modelConfigMap.put("idPanstarServerUrl", "https://dev.id.governikus-eid.de/gov_autent/async");
        modelConfigMap.put("samlEntityBaseUrl", "https://localhost:8443");

        try {
            modelConfigMap.put(
                    "samlRequestSignaturePrivateKey",
                    new String(Files.readAllBytes(Paths.get(workingDir + "/keys/samlRequestSignaturePrivateKey.txt")))
            );
            modelConfigMap.put(
                    "samlResponseDecryptionPublicKey",
                    new String(Files.readAllBytes(Paths.get(workingDir + "/keys/samlResponseDecryptionPublicKey.txt")))
            );
            modelConfigMap.put(
                    "samlResponseDecryptionPrivateKey",
                    new String(Files.readAllBytes(Paths.get(workingDir + "/keys/samlResponseDecryptionPrivateKey.txt")))
            );
            modelConfigMap.put(
                    "samlResponseVerificationCertificate",
                    new String(Files.readAllBytes(Paths.get(workingDir + "/keys/samlResponseVerificationCertificate.txt")))
            );
            modelConfigMap.put(
                    "samlRequestEncryptionCertificate",
                    new String(Files.readAllBytes(Paths.get(workingDir + "/keys/samlRequestEncryptionCertificate.txt")))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TcTokenEndpoint sut = new TcTokenEndpoint(session);

        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(session.getContext()).thenReturn(context);
        when(context.getRealm()).thenReturn(realm);
        when(realm.getName()).thenReturn("master");
        when(realm.getIdentityProviderByAlias("eid")).thenReturn(model);
        when(model.getConfig()).thenReturn(modelConfigMap);

        // when SAML request generation is requested
        Response response = sut.eIdClientEntrance(uriInfo);

        // then generate SAML request and redirect to ID Panstar Server
        assertNotNull(response);
        assertEquals(303, response.getStatus());
        assertTrue(response.getHeaders().getFirst("Location").toString().contains("https://dev.id.governikus-eid.de/gov_autent/async?SAMLRequest="));
    }
}
