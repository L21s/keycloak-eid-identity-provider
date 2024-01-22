package com.extension.configuration;

import com.extension.EidIdentityProvider;
import de.bund.bsi.eid240.PersonalDataType;
import de.bund.bsi.eid240.RestrictedIDType;
import de.governikus.panstar.sdk.saml.response.ProcessedSamlResult;
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
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlResponseReceiverEndpointUnitTest {

    private static final String WORK_DIR = new File("src/test/resources").getAbsolutePath();
    private static final String requestSignaturePrivateKeyString;
    private static final String responseDecryptionPublicKeyString;
    private static final String responseDecryptionPrivateKeyString;
    private static final String responseVerificationCertificateString;
    private static final String requestEncryptionCertificateString;

    static {
        try {
            requestSignaturePrivateKeyString = new String(readAllBytes(Paths.get(WORK_DIR + "/keys/samlRequestSignaturePrivateKey.txt")));
            responseDecryptionPublicKeyString = new String(readAllBytes(Paths.get(WORK_DIR + "/keys/samlResponseDecryptionPublicKey.txt")));
            responseDecryptionPrivateKeyString = new String(readAllBytes(Paths.get(WORK_DIR + "/keys/samlResponseDecryptionPrivateKey.txt")));
            responseVerificationCertificateString = new String(readAllBytes(Paths.get(WORK_DIR + "/keys/samlResponseVerificationCertificate.txt")));
            requestEncryptionCertificateString = new String(readAllBytes(Paths.get(WORK_DIR + "/keys/samlRequestEncryptionCertificate.txt")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void givenPersonalDataWithID_whenGetRestrictedId_thenReturnHexEncodedString() {
        try {
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

            String restrictedIdOutputString = getRestrictedIdStringMethod().invoke(sut, personalData).toString();
            assertEquals(Hex.encodeHexString(restrictedIdInput), restrictedIdOutputString);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void givenPersonalDataWithNullID_whenGetRestrictedId_thenReturnNull() {
        try {
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

            assertNull(getRestrictedIdStringMethod().invoke(sut, personalDataWithNullID));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void givenPersonalDataWithoutID_whenGetRestrictedId_thenReturnNull() {
        try {
            SamlResponseReceiverEndpoint sut = new SamlResponseReceiverEndpoint(
                    mock(RealmModel.class),
                    mock(KeycloakSession.class),
                    mock(IdentityProvider.AuthenticationCallback.class),
                    mock(EventBuilder.class),
                    mock(EidIdentityProvider.class),
                    mock(EidIdentityProviderModel.class)
            );

            PersonalDataType personalDataWithoutID = new PersonalDataType();

            assertNull(getRestrictedIdStringMethod().invoke(sut, personalDataWithoutID));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
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
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
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
    @Disabled
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
        SamlKeyMaterialImpl samlKeyMaterial = new SamlKeyMaterialImpl(
                requestSignaturePrivateKeyString,
                responseDecryptionPublicKeyString,
                responseDecryptionPrivateKeyString,
                responseVerificationCertificateString,
                requestEncryptionCertificateString
        );
        SamlEidServerConfigurationImpl samlEidServerConfiguration = new SamlEidServerConfigurationImpl("https://dev.id.governikus-eid.de/gov_autent/async");
        SamlServiceProviderConfigurationImpl samlServiceProviderConfiguration = new SamlServiceProviderConfigurationImpl("master", "https://localhost:8443");
        HashMap modelConfigMap = new HashMap<>();
        modelConfigMap.put("responseReceiverRealm", "master");
        modelConfigMap.put("idPanstarSamlReceiverUri", "https://dev.id.governikus-eid.de/gov_autent/async");
        SamlResponseReceiverEndpoint sut = new SamlResponseReceiverEndpoint(realm, session, callback, event, eidIdentityProvider, eidIdentityProviderConfig);

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
                                "&RelayState=R_gZtpEw6jeDgSHdvTjzmO-xXCcR1Bq2KElfj_8iWXw.6C4d_8kuWts.LeAW0Jb1Q0WtTDro7e3RuQ" +
                                "&SigAlg=http%3A%2F%2Fwww.w3.org%2F2007%2F05%2Fxmldsig-more%23sha256-rsa-MGF1" +
                                "&Signature=G2tNAnNZtBUnQw6i3vNlq5tZ0TCRQ3atmzJnKD0QgRSSyxEOE2OQA4u47covLi48rJmf4lm5Sw1nNQ3gEYW4ToBj6198Asej88hjIAgByzvIPtfBHa0Zqqs69bUfNsE3oqUIZfjGsGW2EvaTyTjYucQfoIdNy8fMDJIQbcoRsW8O3n%2FJmWO9EsrLgL4Ull6Pcht3zzV9WjS7EKNU%2BhYSXQ%2B1J46lwAsg7%2BlWNm4k6THyCPHaZDZ56LKkbxeGuwv9854TgEqOEN554jmxPJsiiS%2FN5VSGzS0BZQc1YoOYOBUaelpJTJ5Tgz21KHdRMNGxbblOTq8n5XqZRpmTqENlpK9yfbZ4UKp8uDJAkaOcwMXd7WMIEluqy7%2FwW%2FvuPAeR2WBOBkm%2F87cGHT4XiWCAR9vRg5s8B9Zm6f34OST1dCDu%2Fa%2Bc5Do4C%2FqR39HUA5Wg5osjqJ21ITQBzoGVS1yXSZ7C4I8ujYjjzlPPY1Zej5NORGrI6inNWO7kAdwRSYUQFMADQqPTHWtIgsuXhJQLMnoMN44x4x0wuKcGMTrxQxc2OUULucvgmyg9%2Bj5y1T8DKikqrUxqX0IhqSNz5%2BhFc4l%2BZelFhWk5hDXcaCo44mO5KzhsaVWuKekqbCTs32DsC%2BMSnrSKRtvjb8bjK3h1UaZ3EdI8bRZit2qWWZXCWQo%3D"
                ));

        Response response = sut.receiveSamlResponse(uriInfo);
        assertNotNull(response);
    }

    private String getEncodedSamlResponse() {
        String samlResponseString = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<saml2p:Response xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\"\n" +
                        "    Destination=\"https://localhost:8443/realms/master/broker/eid/endpoint\"\n" +
                        "    ID=\"_9ddae633-c804-42b5-bd94-d8488af89802\" InResponseTo=\"_7dc9b779-9670-4810-bee7-4628c0e6fd8f\"\n" +
                        "    IssueInstant=\"%s\" Version=\"2.0\">\n" +
                        "    <saml2:Issuer xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">\n" +
                        "        https://dev.id.governikus-eid.de</saml2:Issuer>\n" +
                        "    <saml2p:Status>\n" +
                        "        <saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\" />\n" +
                        "    </saml2p:Status>\n" +
                        "    <saml2:EncryptedAssertion xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\">\n" +
                        "        <xenc:EncryptedData xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\"\n" +
                        "            Id=\"_f7d2c3e05812a37cd2deceee9f753c7c\" Type=\"http://www.w3.org/2001/04/xmlenc#Element\">\n" +
                        "            <xenc:EncryptionMethod Algorithm=\"http://www.w3.org/2009/xmlenc11#aes256-gcm\" />\n" +
                        "            <ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                        "                <xenc:EncryptedKey Id=\"_58e8d740d651b324035e29f06327d07b\">\n" +
                        "                    <xenc:EncryptionMethod\n" +
                        "                        Algorithm=\"http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p\">\n" +
                        "                        <ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" />\n" +
                        "                    </xenc:EncryptionMethod>\n" +
                        "                    <xenc:CipherData>\n" +
                        "                        <xenc:CipherValue>\n" +
                        "                            KXxOabZBsS70RcLLhWuItj/DdTiYn86cHbegopUp0OFGJLTzDIFT21kLO9xYmGwWmyd5bHwOfN1L6oWN+sRwo7X2m5PkNjHQ2vOeX7TUyRbflAWrdyR1UtA4nWa0YuODAyaNtAqFxXX6v9u2mgi4jJ4WPALil9qhBS6gCsgueOXK9RmV1xGqGjZYeZSXCIPO9OU3STa29eVdGEeTJ6DaQA+nd7uLKN0wAVdS+PQqBC0B9BntKKaJCJLkBP9EM485ie6DPzp9dRhs8oYeTZPwzmWvSpihC/txhueVAJBLrmUx47rlKIfRbYXxj/MZmdQEmfKOG6bYNSc0j4ORJ6Skova4Q7FKEjFXnexSeH5Z81sg8tNbOyXMUBSWKV3Hy2wE4rjTquOFqNMLwqT0iiP5erq8swKatwbZrW32K8i4tRmiqDdZxu0gXVv50cDn4aJfiu/US7lsH8SD1E8Jn6XaPtawSbOdjssab3e+TZgDQul05MmAF9fFXNnDHIoFhcfdzJKRMSgwLytZ83t+1wGFmsu1VVP1bxkASzft5mlCT5dBAuqxO6jpWwqi3arbhAT8rWMTib/DpgBD0K2ZA2Y2DWXchHojEJmr+DQm8ydwQUP3JSdKm6kpt61+jNHYIJxjY79W0W50RXPt3o6wYIcSCTKRiYN4FRiKyR3Rh0LANeg=</xenc:CipherValue>\n" +
                        "                    </xenc:CipherData>\n" +
                        "                </xenc:EncryptedKey>\n" +
                        "            </ds:KeyInfo>\n" +
                        "            <xenc:CipherData>\n" +
                        "                <xenc:CipherValue>\n" +
                        "                    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</xenc:CipherValue>\n" +
                        "            </xenc:CipherData>\n" +
                        "        </xenc:EncryptedData>\n" +
                        "    </saml2:EncryptedAssertion>\n" +
                        "</saml2p:Response>",
                LocalDateTime.now()
        );
        return Base64.getEncoder().encodeToString(samlResponseString.replaceAll("\\s", "").getBytes());
    }

    private Method getRestrictedIdStringMethod() throws NoSuchMethodException {
        Method method = SamlResponseReceiverEndpoint.class.getDeclaredMethod("getRestrictedIdString", PersonalDataType.class);
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
        method.setAccessible(true);
        return method;
    }
}
