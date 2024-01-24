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
        HashMap<String, String> modelConfigMap = new HashMap<>();
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
