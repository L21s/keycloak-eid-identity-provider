package com.extension.configuration;

import com.extension.EidIdentityProvider;
import de.bund.bsi.eid240.PersonalDataType;
import de.bund.bsi.eid240.RestrictedIDType;
import de.governikus.panstar.sdk.saml.response.ProcessedSamlResult;
import de.governikus.panstar.sdk.saml.response.SamlResponseHandlerWithoutTimeAssertion;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.sessions.RootAuthenticationSessionModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EidSamlResponseHandlerUnitTest {

    @Test
    void givenPersonalDataWithID_whenGetRestrictedId_thenReturnHexEncodedString() {
        EidSamlResponseHandler sut = new EidSamlResponseHandler(
                mock(RealmModel.class),
                mock(KeycloakSession.class),
                mock(IdentityProvider.AuthenticationCallback.class),
                mock(EventBuilder.class),
                mock(EidIdentityProvider.class),
                mock(EidIdentityProviderModel.class),
                mock(SamlResponseHandlerFactory.class)
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
        EidSamlResponseHandler sut = new EidSamlResponseHandler(
                mock(RealmModel.class),
                mock(KeycloakSession.class),
                mock(IdentityProvider.AuthenticationCallback.class),
                mock(EventBuilder.class),
                mock(EidIdentityProvider.class),
                mock(EidIdentityProviderModel.class),
                mock(SamlResponseHandlerFactory.class)
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
        EidSamlResponseHandler sut = new EidSamlResponseHandler(
                mock(RealmModel.class),
                mock(KeycloakSession.class),
                mock(IdentityProvider.AuthenticationCallback.class),
                mock(EventBuilder.class),
                mock(EidIdentityProvider.class),
                mock(EidIdentityProviderModel.class),
                mock(SamlResponseHandlerFactory.class)
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
        EidSamlResponseHandler sut = new EidSamlResponseHandler(
                mock(RealmModel.class),
                mock(KeycloakSession.class),
                mock(IdentityProvider.AuthenticationCallback.class),
                mock(EventBuilder.class),
                eidIdentityProvider,
                eidIdentityProviderConfig,
                mock(SamlResponseHandlerFactory.class)
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
    void samlResponseReceiverEndpointParsesPersonalDataAndCreatesIdentityForAuthentication() {
        RealmModel realm = mock(RealmModel.class);
        KeycloakSession session = mock(KeycloakSession.class);
        IdentityProvider.AuthenticationCallback callback = mock(IdentityProvider.AuthenticationCallback.class);
        EventBuilder event = mock(EventBuilder.class);
        EidIdentityProvider eidIdentityProvider = mock(EidIdentityProvider.class);
        EidIdentityProviderModel eidIdentityProviderConfig = mock(EidIdentityProviderModel.class);
        UriInfo uriInfo = mock(UriInfo.class);
        URI uri = mock(URI.class);
        AuthenticationSessionProvider authSessionprovider = mock(AuthenticationSessionProvider.class);
        RootAuthenticationSessionModel rootAuthSessionModel = mock(RootAuthenticationSessionModel.class);

        MultivaluedHashMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.put("RelayState", List.of("AWDYHOhB3Dy1FcD1rrfLh0eRFAC-t_CSd7G3KpHlb0o.erobji-kr7o.xrUVKpHiRA281FOHHr4wVw"));
        queryParameters.put("authSessionId", List.of("7d04d3c0-660e-475b-8679-c84772987d33"));

        String workDir = new File("src/test/resources").getAbsolutePath();
        HashMap modelConfigMap = new HashMap<>();
        modelConfigMap.put("responseReceiverRealm", "master");
        modelConfigMap.put("samlEntityBaseUrl", "https://localhost:8443");
        modelConfigMap.put("idPanstarServerUrl", "https://dev.id.governikus-eid.de/gov_autent/async");

        try {
            modelConfigMap.put("samlRequestSignaturePrivateKey", new String(readAllBytes(Paths.get(workDir + "/keys/samlRequestSignaturePrivateKey.txt"))));
            modelConfigMap.put("samlResponseDecryptionPublicKey", new String(readAllBytes(Paths.get(workDir + "/keys/samlResponseDecryptionPublicKey.txt"))));
            modelConfigMap.put("samlResponseDecryptionPrivateKey", new String(readAllBytes(Paths.get(workDir + "/keys/samlResponseDecryptionPrivateKey.txt"))));
            modelConfigMap.put("samlResponseVerificationCertificate", new String(readAllBytes(Paths.get(workDir + "/keys/samlResponseVerificationCertificate.txt"))));
            modelConfigMap.put("samlRequestEncryptionCertificate", new String(readAllBytes(Paths.get(workDir + "/keys/samlRequestEncryptionCertificate.txt"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SamlConfigurationImpl samlConfiguration = new SamlConfigurationImpl(modelConfigMap, "master");
        EidSamlResponseHandler sut = new EidSamlResponseHandler(realm, session, callback, event, eidIdentityProvider, eidIdentityProviderConfig, SamlResponseHandlerWithoutTimeAssertion::new);

        when(eidIdentityProviderConfig.getConfig()).thenReturn(modelConfigMap);
        when(eidIdentityProviderConfig.getSamlConfiguration()).thenReturn(samlConfiguration);
        when(uriInfo.getRequestUri()).thenReturn(uri);
        when(uri.getRawQuery()).thenReturn("SAMLResponse=nXhX0%2BNYjuVfych%2BZGTRuy%2B6aoJG9E704ssGPSla0ZO%2FftlVWdNdszGzE%2F2k0JWACxwc4AL4%2B38cXftty6e5Hvpfv8O%2FQN%2B%2F5X06ZHVf%2Fvrdc4Uf1Pf%2F%2BO3vc9y1yPhl5%2FM49HP%2B7Rbq568%2FTn%2F9vk791xDP9fzVx10%2Bfy3pl8Po2hfyC%2FQ1TsMypEP7%2FRufz0vdx8vvF1XLMs5fINgOadxWw7x8URiGglMet90MdvG85BOYTENzf%2BR1BuZ9Ng51v3z%2FJvO%2Ffv8%2FKZkRdAHFP3IioX9gBBb%2FSGIM%2FYGnFF0gCJkkWXb%2Ftf%2FTYHe4hcgMwjI0hX4QBJT%2FwEg8%2BUERJP0jpTCSRGiKzFD0FprnNZf7eYn75dfvCIRgPyD4B4K5EP2FIV8Q%2BQuBI9H3b%2F6fmN1efv%2BJ0NfvwtO%2FwvM%2FoxPPcz79A5Hvv%2F2JSJZvv9TZL%2BVwB6Wvm3X%2Bcfv%2FS5b%2FHfwd7p9X%2FGdInCVe1vm%2FfOWGLP%2Fmx%2B2a%2F8%2FX307ewl%2FOmqb5PH8Hf%2Ft5yfj1F7Vfjz6dznHJM%2BZPe%2F8tF%2F9%2B3NT6pzI%2BXuKfev7xwx%2BkuDmx7%2FsvO%2FrLMJUgAkEwCGHgzbf7H3%2B7o5PdgaRyIqYhPItxCqdyDM4TLE4oHKXjDMFSAv7%2BzT3H2%2FX%2Fr75Hm3f5Tarf%2FmLZHQ49X6oh%2B8a05TDVS9X9N7ron4bB8N%2FifEZw4keZdv%2BAMZu%2F1PyU%2B2L46WA2%2FzcqIBD6XUs21%2BXf%2FosheXYr%2BcPlGIcyPMGSm%2BlkhhApRKMIVeAJUsQkktDpv%2B3Cv8I7zfGPIc7HH11ZwOOt8naDr8s7b%2F%2BXePyrsrmKb0B%2B59S%2Fhv0%2Fwf2JOVePVT79gwp%2FOfidvL9xT7oCk9dCzeIKeb2PAU9Viz65WQKX0VgCgCLsZtcpYEqFdgTeGIJPd3b8ox%2Bjp38BgXAhfRwMDV%2FyniL6nHqZBjap81EVYvJ2EIrtkKc1ck7WibHgtasZ5KWicygHqKaTwxjCj%2FBiWTlseOG7qLKxFhKiQZ9RduTUpJORKZgXYcC6psScXj1ox9ASp5nVU02LF2btePdMC1bw7dOPuVDa6nbuJ63dQjEk0KbiJkMOzWd5AS2BmJDdI%2B4ZaMC8SCRHIls3%2Btzo7kecNs8wQuKXnUET4xbrUx09k8rN6yGsj%2B2lwvETG8wOG1MqqgEgy1so0AR9DYN41CSEVB79Ezoo4aXsjYrQI5DLbsQDitGF1A59EOBAjK1nQdQrSiGmmr39lGvEBeyG1eiVgSEGXwlbcmICPcCQx6XV53MMlMfAqoEtJ3Faf4IbzToxXo7ZpL%2BoNSbXEa921XIbsavdUE9BGu%2B62Uo641WNJjvx5koqw9jqb6nwkM6KF%2BvV0FhxehmaQLzoLwH1rpvzZUAMYoYpG6kc0ewoYM2D90CH7k2YSa4Vgl1ySk%2F19sDwr9MjUMKKbI8ZFVKgWC0oNotfRxAFBB47HnWtY4WyEagdgcLIO58H3BCRLhvlIXUXn8D9ox0j4E0AKQoiej84gCXgQwNTPHCVu1Kij4wuBgrHD7thBw56ChHcMb%2F%2BpPkffP6Dvn85%2BoPif8mF3%2FP7PvtntfjfJAVdApvoQGWbMSjfi6BMjFW9oqhoGyJ9ESCfeZhqpb3Yqgbklbo6gr7X7erH2%2FiUQyh9j403j0XX5rfYQqDFjtQ0IUAjiLAfeRlNc9c34awAm%2BvlrTMMKDiIVbJlhSDbFmMiZIZrwA8c2DoBy2PToEySB8u6Hve64KKFLQEsCxd%2FnZBqcMO6Mmh50Z1b7CvBXtx8B0lpjVULN9ivLfJKFN3my2B3u56HH4b35uq0a8BPqXWQrbCdwJykMdH1NKLrXAPrIDfxi9ERQWapK5nRUdD2mOrp7oGpcEY%2FXSqVFd9QzzMFg%2FbtTFpzWdvK%2BJtiGkeaPFAD5kheYN4ihoeCTxRUQc6AAhQ3QJaRWNbs%2B3booxFx9C%2FEvVgx3uFiWHb%2FeJEfOdptICoNL%2BsJTFTrGJGHrSNqGyHo%2BnSVD7vWiPgyJC%2Brw6GcW1b4hP37qvaJePULDXePGTzOSSUKAC%2BjnWIR2Rf4ijccF26NOsQETyfffAxTTUvFNi%2FjOFUSZDACn%2BkpyE5ZoMuhRQq%2FMBS7IGBvIx2uQSAc6CE251vYeAGx09NZ0hW%2FZyT6TkTerPIgEiMHf40cUHcZ1LfvYqSfcC7a5ijrBZTZVzfAev5%2BC%2FG0hDEAC0d%2BOntI8y9T4BPj9enl0z1Gm%2FPZXDW0QzwqrIkXgKkSXUDxFmJwL5c%2BlB%2FR15xsjkBqm5RnhLKN%2FOMB1CMOhwAU%2B6CbbjwegsSsNnYvMW9sgomuM%2B%2F%2BYox1HPI6dspxTw8sVxpNmxQSTn5APoQvpccDhGLrbf%2BucjJiS2WEkJ17ea7sce27ix9HlIReFFRjXjmt%2BCg%2Fz3JG457zVIuuQUsIOVDL15n6CK0X0C8PTzy8Va7Q3KymmYjHOcFKJ%2Fova33IDh9Xj8bdJizY3nH1HvHcOBg0KHP3kzugRKX%2Bc0k83jk%2F7ABlGba7nqpeT42Sy4djyZD80AgnT6IQ6u3a11dEijofOip98WrQzExNBRS6HMmUdBg87JtBtyy8sh4fgXt8iiiqyLw3G74heBuo86SGUA8RhdHTGv8jvCfsWKVjGO0wS%2FmVLAWxx3B%2FIKYrzSgLYRx5oEgNa86KksF3X0Rp%2BPKnTfIILXcKT66UulK9qLldvEbO6%2BQ7de%2F3zbYt6rCoJhUz3Z39XifsJKQTO8dHcv7wgZg2NIlt7xzL6Lx%2FvmFRwimz9ExNx0MuK8xT8IWnz8cpfC7EJVY4OydeVpWZ0msCOkMXhdAIJ6giEtR5ll%2FIJfWBmddP1BVBycbRlxsUmPnJPhKPnHn%2BQt300NPVZ06KK1Yw1cvdGhg1Ltt8T47HUntijFfrtfodq1N0OjziesVwmnxSwcjCtv2kbhktbR4yeh2ahjnPc8UufkniNRIk3740InmtpekZIFKAuaQaqVi2owUzg%2BBR5%2FOhwHrFPt8oMUtSFfGgBXMQVxigYGpaADcf75mtS1aO3hvsLH%2BxFg%2BZNE8r%2B%2FGJby9bu540yCZGeyTVPDpKpHtWQb6eKuPXk4gjnCFncjCELwLD074%2BCNNp%2FMxykknc2CQSWNndGZV%2BhiEqH3XiLn4U5nS%2BcaxuDKYs7Ixyc88q%2BLoBEyiMp5j2cpNH6r11LQPiHfxa180LwscM08C5703YoFarSHWkcbvM5WZ1lsUJP58pccLhB7NxZSfUMCN5NPzgdI4YxbNd4XkRKptR6ucovlPWgJ14xzNUApj7KR18NhUnLUFw89DetobszMRgEjbgFQM6rPkWCNAhPje1jwG14bgz5ylid%2FqEPp36%2BYgB%2FiRKVRDhUWR5Q%2F%2BMBBYecnYU7PzMuPrVVI%2F3II9gZNUEbCl1EYOG1GDVLNPjpYoR2Xzcc4n01K6hdyeEh1GkzjAd5MS8S%2BgyTPxidPu0%2BXYqHZkDiKTdwyaywUcy3TkogKcXhew8UIPMs48Dwd%2BOKXIPtkKVZo0G2TMBt2MC%2B7HCSGfILyCkUSd3ZUGuPcUI7hwgl2L3KqkiCBqdUCwqJNln0VmNJ%2BmljuUesc844mXyZI0IwR4UrFwLrVw59HGb3p%2BaQLKbMi3G3SXEd%2FLSbEFrHlqPYavPkig7cX2AU2JLLRQw5Q3RxREtsKpWvhyhsx8OUD1CMy9bRX6YZY56VV0ElhEt2%2BfuYCcEt%2BYQVvOUbEvZqrnGP7IJUsPiNWs2%2Br78d3%2FLG1eL3BVLajRA8hNOG870%2Fc6el8tb87QVVjs2jfBi3srTWwwjBk0SgaVKRpzDj71MMgwHa2sUPVWQKE1hp0d2UtnVa7eZPurBT4%2Fcn5uxSvUBsxIaDpezaDWlvuefj6yd3LsQOA4wesKcmZ7El5P9FLNTkxawBxeb0JzkK6fJquvLFjq1XSAq9pbKGmyOZBvp7sgdhSWCXjiVc%2B2wd6XufhXIPPXgPghE4UxcXxqUXfD5wuuAjAf4%2BTg3NIMhQeYVksTDdxeNTFUs4ayXthcv%2ByfaRGU7Xtw7sNhA5Q62doEpmRfxbTG2XjgwjKulvqHhzn4uO9IAkNudIuwlRO3rSCjUzNLkYKtSZ%2BeLXIDJl2CeFv1hp0iCoRQNNFufMLpvzUduGJwdMpliRYfrjgfE8d4Ck5HL%2BT7tIAxxXl3Mm7Yg5w%2BDK99KTTSK9SCfDkSUoxisu3VZI%2BvoDZ9rw3XNNVbqPX%2BnKaERlWpBuHJMV9DXkhI%2FWEobFR%2F4MHQs7POjuquuySap4bW8ou2JWJu9NAAkCAEKJt4zReRvdxmuMsxvIsc%2BpvTjCjXPGuuT%2FEha7Ckp5FDh0oSYfD8ARLzRUsqC98jUMTJI%2BJ9yf85QAdacAlomZSwJAT17YQ%2F4rA1bpQz4AvTch92ovqiZ53ryWPdUN15ATcObthI1Qm8sJAXh%2B3Ij3i0MdEue5jqzyKkkm4QIM6M7qDn1uERBa0vVwS2Pc6E0qM5qKXgXY3wubN9Zm0cE1yjKs7VVu%2FceEz8DdAwSpjldnxD2PqOaMX3KBTygcdS4VHcdbx3HqdpetWocmw46sglsB8E7mEuOS6Uxy%2FKbYcaPa79li0NXbdK7BC1NUEgkljHwRatUt4dGvU2etenXjSvo6om9LqUWirJ37vR4igE2vAvzudOtX%2Fd37Qq9S%2FCHAWpyWU%2BgFI5GlnOLmIPkVYthGJaO8nrkwLDcKeA2YOjvxEEIzpkk6Y0lN9Ve2KPameQoXEo1j%2BoTGH4%2BWoTUE5NnWNCj60Cz%2BpulMtUEihMAgYMlci6l%2BQXBs31MnxmUsG854lQ8YC5XfFeUfjAAyprwsW1c4TXkEr8nW4fE4WnB5knLnDpmn88nAq3ksyezoohSBevXJKNvcR0DQkyzpy3pd4Po3%2F38KghG4O%2FP8GzkYtr6%2FRJzHQ2PrdKNxU3Gxt%2B1Hqm7lkNh9eJg%2BSMyXhhGYfsMHQNnpG4Yd%2BRwqfpCBOeN1zLhwvNYON3sAnLiFLx1yuH8QD4pqLj7EgLxgY178Qnm0p0qRiCJtpU%2BmJiT8XXuaxnmEukhyGPMZ5sTtux19xb9mpWXjq1UTi6kLwJsQi7uHKRxYmwp1nkmxvRMsKPmEHSNluIeSDlQkdVYLz1AtUQ%2Bmrr0RCIMr%2BUasKlxxQ9VJIvbdlRDlv5yvxwHgW5NkS%2F1ypkWPGyzzL3nNA0k1INv7np4Ppk0oIB7eVv4iMLQZ4mXCxLDdtH%2Bhx8KONAMn6SAjU2fHK%2F20djUPX4%2BEIy96nA%2BqM4DcRN9yhDH0aBH7mUIrykZ4lvuvHoE7c87L19cukuarI7HixbKYLinBPjBoUT2MEQIzoTtVcVno2hhreg0P0pXZuwHuEe2XL5WG1MyAeJ5h9bFkdB0UkYEY7UTqAf7NM7v%2FUacgLEBv%2FTqlfTURq1VJFUMlrrL9koUQzURaGq6o8tXK35mQQYq%2FmayveU923Rn4lHdW%2FgeeNl7dPLvaZFtE39VwI85aUd9xmK%2BIzz1UqwnYgee%2BBnMAijUneivHU93mHBmgHrVJw3mtM96MpHyrveK0SgttK4sVV16HTD9vPdCJw4EsnkQbC4NzofMliocH90l2RqvDnYb2TiZXzS18c2%2BdyOcE6uHQUQ%2BG0yiEEJaofpa39sU7N7yNAFQoGIc42BGu%2BtVqrjM%2FPrvLQl%2Bbg7%2BWP%2F%2Bv6vYfy5t%2F9x2%2F%2FZ%2FAQ%3D%3D&RelayState=AWDYHOhB3Dy1FcD1rrfLh0eRFAC-t_CSd7G3KpHlb0o.erobji-kr7o.xrUVKpHiRA281FOHHr4wVw&SigAlg=http%3A%2F%2Fwww.w3.org%2F2007%2F05%2Fxmldsig-more%23sha256-rsa-MGF1&Signature=YP7qsZJW71siIUPWPuPEv46GfdENZ7%2F%2B1sgRydWSNlDkCP46WfY2OZ%2FRUuQk27bJA0%2FGU5zdXCG%2FSN3AJf4v6bX0AalvGzTQeuweigjgMyuOCQVnxRaOIuoZuTn72kYxJHEj3VeqaFJyr6EHaEcrSUNUCU8h0YGaMvZnqMz1LPfzqwwX0uyEur32Hlms0K2NWzMpuSIaTxw05W%2BhW5U07juJLLCcZrE2FJGgxfDi7XViIsq5D83mZ38uFuWndQt4%2Fewq0jlZLr0i3RWorN7W1az8KPFO7h9JGA%2FgRtNDn9z6kc3StfElRmbvuEzSEvBBhWD9p1BS3o3UNnxMVQn3nnuYdGg5HMsgUHz9Nu%2FFoiTfpO3YfRxa2P0wDZ5NxXAm8mPxg3eduMoiG6qFeSp8GH9lT5HqX1vHik5YiSq9w3FfipNBiBCvyr7WdHfvj7Kz4iM2R7ap%2BpKjFr%2BEt3Tjogk4dA824yvaAyyUk8UESg6z1Fw9GI0ffyFWYOYyMWyAxZ89WyjktBx%2FvFbIxXlqAG1FSW6rFg5eozXYsiCfvdeinr2%2BC0Ufg07k2fKMaSmJdsOjKT0Slwhwdj6yPSMCsTkvHUhaHM2zkvT91PxzkziCpokBMfuoOADKavxzF1oPF9SHTCcWUC2Th6dKqG4Q%2F%2F1fQBr0Lxo7rap0HKE0By0%3D");
        when(session.authenticationSessions()).thenReturn(authSessionprovider);
        when(authSessionprovider.getRootAuthenticationSession(realm, "7d04d3c0-660e-475b-8679-c84772987d33")).thenReturn(rootAuthSessionModel);
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);

        sut.receiveSamlResponse(uriInfo);
        verify(callback).authenticated(any(BrokeredIdentityContext.class));
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
            method = EidSamlResponseHandler.class.getDeclaredMethod("getRestrictedIdString", PersonalDataType.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        method.setAccessible(true);
        return method;
    }

    private Method getSetUpIdentityMethod() {
        Method method;
        try {
            method = EidSamlResponseHandler.class.getDeclaredMethod(
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
