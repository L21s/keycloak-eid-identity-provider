package com.extension.configuration;

import com.extension.EidIdentityProvider;
import de.bund.bsi.eid240.PersonalDataType;
import de.bund.bsi.eid240.RestrictedIDType;
import de.governikus.panstar.sdk.saml.response.ProcessedSamlResult;
import de.governikus.panstar.sdk.saml.configuration.SamlConfiguration;
import de.governikus.panstar.sdk.saml.response.SamlResponseHandlerWithoutTimeAssertion;
import de.governikus.panstar.sdk.utils.exception.InvalidInputException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import org.opensaml.core.config.InitializationException;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EidSamlResponseHandlerUnitTest {

    @Test
    void givenPersonalDataWithID_whenGetRestrictedId_thenReturnHexEncodedString() {
        SamlResponseReceiverEndpoint sut = new SamlResponseReceiverEndpoint(
                mock(RealmModel.class),
                mock(KeycloakSession.class),
                mock(IdentityProvider.AuthenticationCallback.class),
                mock(EventBuilder.class),
                mock(EidIdentityProvider.class),
                mock(EidIdentityProviderModel.class)
        );

        byte[] restrictedIdInput = "01234".getBytes();
        RestrictedIDType restrictedId = new RestrictedIDType();
        restrictedId.setID(restrictedIdInput);
        PersonalDataType personalData = new PersonalDataType();
        personalData.setRestrictedID(restrictedId);

        try {
            String restrictedIdOutputString = getRestrictedIdStringMethod().invoke(sut, personalData).toString();
            assertNotNull(restrictedIdOutputString);
            assertEquals(Hex.encodeHexString(restrictedIdInput), restrictedIdOutputString);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void givenPersonalDataWithNullID_whenGetRestrictedId_thenReturnNull() {
        SamlResponseReceiverEndpoint sut = new SamlResponseReceiverEndpoint(
                mock(RealmModel.class),
                mock(KeycloakSession.class),
                mock(IdentityProvider.AuthenticationCallback.class),
                mock(EventBuilder.class),
                mock(EidIdentityProvider.class),
                mock(EidIdentityProviderModel.class)
        );

        RestrictedIDType restrictedId = new RestrictedIDType();
        restrictedId.setID(null);
        PersonalDataType personalDataWithNullID = new PersonalDataType();
        personalDataWithNullID.setRestrictedID(restrictedId);

        try {
            assertNull(getRestrictedIdStringMethod().invoke(sut, personalDataWithNullID));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void givenPersonalDataWithoutID_whenGetRestrictedId_thenReturnNull() {
        SamlResponseReceiverEndpoint sut = new SamlResponseReceiverEndpoint(
                mock(RealmModel.class),
                mock(KeycloakSession.class),
                mock(IdentityProvider.AuthenticationCallback.class),
                mock(EventBuilder.class),
                mock(EidIdentityProvider.class),
                mock(EidIdentityProviderModel.class)
        );

        PersonalDataType personalDataWithoutID = new PersonalDataType();

        try {
            assertNull(getRestrictedIdStringMethod().invoke(sut, personalDataWithoutID));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void givenIdentityAndConfig_whenSetUpIdentity_thenIdentityIsSetUpWithConfig() {
        EidIdentityProvider eidIdentityProvider = mock(EidIdentityProvider.class);
        EidIdentityProviderModel eidIdentityProviderConfig = mock(EidIdentityProviderModel.class);
        AuthenticationSessionModel authSession = mock(AuthenticationSessionModel.class);
        ProcessedSamlResult samlResponse = mock(ProcessedSamlResult.class);
        PersonalDataType personalDataType = mock(PersonalDataType.class);
        SamlResponseReceiverEndpoint sut = new SamlResponseReceiverEndpoint(
                mock(RealmModel.class),
                mock(KeycloakSession.class),
                mock(IdentityProvider.AuthenticationCallback.class),
                mock(EventBuilder.class),
                eidIdentityProvider,
                eidIdentityProviderConfig
        );

        when(samlResponse.getPersonalData()).thenReturn(personalDataType);
        when(personalDataType.getGivenNames()).thenReturn("Erika");
        when(personalDataType.getFamilyNames()).thenReturn("Mustermann");

        BrokeredIdentityContext identity = new BrokeredIdentityContext(Hex.encodeHexString("restrictedId".getBytes()));
        try {
            getSetUpIdentityMethod().invoke(sut, identity, eidIdentityProvider, eidIdentityProviderConfig, authSession, samlResponse);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        assertNotNull(identity);
        assertEquals("erika", identity.getFirstName());
        assertEquals("mustermann", identity.getLastName());
        assertEquals("erika_mustermann", identity.getUsername());
        assertEquals(eidIdentityProvider, identity.getIdp());
        assertEquals(eidIdentityProviderConfig, identity.getIdpConfig());
        assertEquals(authSession, identity.getAuthenticationSession());
    }

    @Test
    void samlResponseReceiverEndpointParsesPersonalDataAndCreatesIdentityForAuthentication() throws URISyntaxException {
        RealmModel realm = mock(RealmModel.class);
        KeycloakSession session = mock(KeycloakSession.class);
        IdentityProvider.AuthenticationCallback callback = mock(IdentityProvider.AuthenticationCallback.class);
        EventBuilder event = mock(EventBuilder.class);
        EidIdentityProvider eidIdentityProvider = mock(EidIdentityProvider.class);
        EidIdentityProviderModel eidIdentityProviderConfig = mock(EidIdentityProviderModel.class);
        UriInfo uriInfo = mock(UriInfo.class);
        KeycloakContext context = mock(KeycloakContext.class);
        IdentityProviderModel model = mock(IdentityProviderModel.class);
        SamlConfigurationImpl samlConfiguration = mock(SamlConfigurationImpl.class);

        SamlKeyMaterialImpl samlKeyMaterial;
        try {
            String workDir = new File("src/test/resources").getAbsolutePath();
            samlKeyMaterial = new SamlKeyMaterialImpl(
                    new String(readAllBytes(Paths.get(workDir + "/keys/samlRequestSignaturePrivateKey.txt"))),
                    new String(readAllBytes(Paths.get(workDir + "/keys/samlResponseDecryptionPublicKey.txt"))),
                    new String(readAllBytes(Paths.get(workDir + "/keys/samlResponseDecryptionPrivateKey.txt"))),
                    new String(readAllBytes(Paths.get(workDir + "/keys/samlResponseVerificationCertificate.txt"))),
                    new String(readAllBytes(Paths.get(workDir + "/keys/samlRequestEncryptionCertificate.txt")))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SamlEidServerConfigurationImpl samlEidServerConfiguration = new SamlEidServerConfigurationImpl("https://dev.id.governikus-eid.de/gov_autent/async");
        SamlServiceProviderConfigurationImpl samlServiceProviderConfiguration = new SamlServiceProviderConfigurationImpl("master", "https://localhost:8443");
        HashMap modelConfigMap = new HashMap<>();
        modelConfigMap.put("responseReceiverRealm", "master");
        modelConfigMap.put("idPanstarSamlReceiverUri", "https://dev.id.governikus-eid.de/gov_autent/async");

        EidSamlResponseHandler sut = new EidSamlResponseHandler(realm, session, callback, event, eidIdentityProvider,
            eidIdentityProviderConfig,
            SamlResponseHandlerWithoutTimeAssertion::new);

        when(session.getContext()).thenReturn(context);
        when(context.getRealm()).thenReturn(realm);
        when(realm.getIdentityProviderByAlias("eid")).thenReturn(model);
        when(model.getConfig()).thenReturn(modelConfigMap);
        when(eidIdentityProviderConfig.getSamlConfiguration()).thenReturn(samlConfiguration);
        when(samlConfiguration.getSamlKeyMaterial()).thenReturn(samlKeyMaterial);
        when(samlConfiguration.getSamlEidServerConfiguration()).thenReturn(samlEidServerConfiguration);
        when(samlConfiguration.getSamlServiceProviderConfiguration()).thenReturn(samlServiceProviderConfiguration);
        when(uriInfo.getRequestUri())
                .thenReturn(new URI(
                        "https://localhost:8443/realms/master/broker/eid/endpoint" +
                                "?SAMLResponse=" + getEncodedSamlResponse() +
                                "&RelayState=AdVKKybhjpB4OxBW9fTbvp3Js87KIwqFd6qqVtkG9VE.SGFMINMrczA.kJynTqBsTu2S42SsqqbGIg" +
                                "&SigAlg=http%3A%2F%2Fwww.w3.org%2F2007%2F05%2Fxmldsig-more%23sha256-rsa-MGF1" +
                                "&Signature=YWpmQq%2FHrWa3elNAQzjd7xfNGCVVHFWT%2FSpVhFziTYwhvVcOgNIl8F7rEbiFmhASHWR%2F6qyTB6q9Nl%2F3lgFgvfVjPRns%2FNj1cPP0AdQwNQWOQz3YICdNLq0yeA204j8qoPdvgH7P7h6YOl2oV3EwWJcCqcLYOBtKTJ75v9Eg0zrkmOFKCs0S4s5JbDaTtp4lCJN5hjPOfXGBE6WhrzkQhuBzZRs6LASk%2B0XsQXr3oG7GLWxYjuU%2BmNFSDwkCSrm00KZy4XTS3cJ5Hki4nJ8i7wa6gN%2Fit3agvo5U5gJSrxM%2FH%2F%2F1KHJ%2FZuEiz6U2lsnXZDznu8983xlJQvQp3C23%2FMIrAVt4W%2Fge65Pp2mCIs4piSriJ0JNC5QNetfVQhFuplcJJqP05dA%2F9QRgxflVR9Rd6%2Bkt%2BODSAwDFALgwuJO9yuTVTsCNpl7cHJfGyAX9H0pOqs6NDvq2pp%2FsaFyZNcTEnO%2Faq0mNjfa%2FFrWLINNKi9nqN8Gy90XkjqvHYxyhYj4c3vMb468%2FhMHuB560DauFwBeBZpLrz8nKrAb6Mrs62EBycnLNi6FpdNkP73FkWsOIm2u2I2g29qTbuAQgh08HezIkJP%2BJLyvUm1ahp1pxKjy2fvQ7UHZy9gyozvefFugwU73B2L6C5rqedinRX4LV3lA1SM4ch27lBozqbR1o%3D"
                ));

        Response response = sut.receiveSamlResponse(uriInfo);
        assertNotNull(response);
    }

    private String getEncodedSamlResponse() {
        String samlResponseString =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<saml2p:Response xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\"\n" +
                        "    Destination=\"https://localhost:8443/realms/master/broker/eid/endpoint\"\n" +
                        "    ID=\"_f7ca7d47-a6f8-49ea-88de-60df46d3bf9e\" InResponseTo=\"_b851b730-055f-4557-be75-13eca33e6628\"\n" +
                        "    IssueInstant=\"2024-01-23T09:03:59.426Z\" Version=\"2.0\">\n" +
                        "    <saml2:Issuer xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">\n" +
                        "        https://dev.id.governikus-eid.de</saml2:Issuer>\n" +
                        "    <saml2p:Status>\n" +
                        "        <saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\" />\n" +
                        "    </saml2p:Status>\n" +
                        "    <saml2:EncryptedAssertion xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">\n" +
                        "        <xenc:EncryptedData xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\"\n" +
                        "            Id=\"_43f5ede6d5e7a48255f47b1e460770af\" Type=\"http://www.w3.org/2001/04/xmlenc#Element\">\n" +
                        "            <xenc:EncryptionMethod Algorithm=\"http://www.w3.org/2009/xmlenc11#aes256-gcm\" />\n" +
                        "            <ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                        "                <xenc:EncryptedKey Id=\"_ca39c8cdbe531dd4359f1567c4c956af\">\n" +
                        "                    <xenc:EncryptionMethod\n" +
                        "                        Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\">\n" +
                        "                        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />\n" +
                        "                    </xenc:EncryptionMethod>\n" +
                        "                    <xenc:CipherData>\n" +
                        "                        <xenc:CipherValue>\n" +
                        "                            ZxLRO0xUE8/qK20/GIs9EO/7LqvPKhyJ/q0+n66ZM0Z5d6joDo7phZVUghLeZEnfniI2Lnwck9dStmwKUhTQT6ar3g6lcthuzEGw7zVWziCplcYJsVYNgcPAGQ9X1+eMbqVdg2hUSRKUL3z/t3K6nSpG1DaJfAaV2GUEpMaXQAgMZAi8YQRCbcuTABQv9rZPcbw/R6AF9+52PXxgHMtxzGlDhCnhay1HdaupVzukiKB4fbjKA3fmLJGkhV4sv2U+v9q3RPPBmgb78z4qAcIubcKl4ecfMnenbRCKxKSTinIr4ggxkiHbGGJ2upC1q7k55gCIWwjM8eP0bMdsrZRFKkKPCZPjPn4UQ8XS25Oop3dMnN73FAJJOSN+9JnYA1Rc5ylIHgwp615aAvozHzd3sgtIFOHebAwSsv9GSw1LAhibM3vR8f6qg3kn7w7gmfA/+hXfOM7gt3Z78KNIoARdVuFdvFZsM43fNuKESioEAC9piErtiMJrH+fTRXMHaGHk494/V1Qf9aDdke0qgn8L4ri+qKj/CUyKq5eYnobtJq43nvwqmblXqppPdfMuzJgB/WSVozbkAv8JFOsNT+pdU6M8TX8o7s8IZP/LjR8CEdeUBNPcgcZWLUWOCJKMpsQPRwLGk3K4+wa+lxMWjVEV5717PrWuf/HoFWl7d/xR4PQ=</xenc:CipherValue>\n" +
                        "                    </xenc:CipherData>\n" +
                        "                </xenc:EncryptedKey>\n" +
                        "            </ds:KeyInfo>\n" +
                        "            <xenc:CipherData>\n" +
                        "                <xenc:CipherValue>\n" +
                        "                    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</xenc:CipherValue>\n" +
                        "            </xenc:CipherData>\n" +
                        "        </xenc:EncryptedData>\n" +
                        "    </saml2:EncryptedAssertion>\n" +
                        "</saml2p:Response>";
        return Base64.getEncoder().encodeToString(samlResponseString.replaceAll("\\s", "").getBytes());
    }

    private Method getRestrictedIdStringMethod() {
        Method method;
        try {
            method = SamlResponseReceiverEndpoint.class.getDeclaredMethod("getRestrictedIdString", PersonalDataType.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        method.setAccessible(true);
        return method;
    }

    private Method getSetUpIdentityMethod() throws NoSuchMethodException {
        Method method = SamlResponseReceiverEndpoint.class.getDeclaredMethod(
                "setUpIdentity",
                BrokeredIdentityContext.class,
                EidIdentityProvider.class,
                EidIdentityProviderModel.class,
                AuthenticationSessionModel.class,
                ProcessedSamlResult.class
        );
    private Method getSetUpIdentityMethod() {
        Method method;
        try {
            method = SamlResponseReceiverEndpoint.class.getDeclaredMethod(
                    "setUpIdentity",
                    BrokeredIdentityContext.class,
                    EidIdentityProvider.class,
                    EidIdentityProviderModel.class,
                    AuthenticationSessionModel.class,
                    ProcessedSamlResult.class
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        method.setAccessible(true);
        return method;
    }
}
