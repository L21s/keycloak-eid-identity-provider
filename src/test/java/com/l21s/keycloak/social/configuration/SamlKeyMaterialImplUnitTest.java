package com.l21s.keycloak.social.configuration;

import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.Test;

public class SamlKeyMaterialImplUnitTest {
  private static final String WORK_DIR = new File("src/test/resources").getAbsolutePath();
  private static final String requestSignaturePrivateKeyString;
  private static final String responseDecryptionPublicKeyString;
  private static final String responseDecryptionPrivateKeyString;
  private static final String responseVerificationCertificateString;
  private static final String requestEncryptionCertificateString;

  static {
    try {
      requestSignaturePrivateKeyString =
          new String(
              readAllBytes(Paths.get(WORK_DIR + "/keys/01samlRequestSignaturePrivateKey.txt")));
      responseDecryptionPublicKeyString =
          new String(
              readAllBytes(Paths.get(WORK_DIR + "/keys/02samlResponseDecryptionPublicKey.txt")));
      responseDecryptionPrivateKeyString =
          new String(
              readAllBytes(Paths.get(WORK_DIR + "/keys/03samlResponseDecryptionPrivateKey.txt")));
      responseVerificationCertificateString =
          new String(
              readAllBytes(
                  Paths.get(WORK_DIR + "/keys/04samlResponseVerificationCertificate.txt")));
      requestEncryptionCertificateString =
          new String(
              readAllBytes(Paths.get(WORK_DIR + "/keys/05samlRequestEncryptionCertificate.txt")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void
      givenRequestSigningPrivateKeyString_whenGetSamlRequestSigningPrivateKey_thenReturnPrivateKeyObject() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(requestSignaturePrivateKeyString, "", "", "", "");

    PrivateKey privateKey = sut.getSamlRequestSigningPrivateKey();

    assertNotNull(privateKey);
    assertInstanceOf(RSAPrivateKey.class, privateKey);
    assertEquals("PKCS#8", privateKey.getFormat());
  }

  @Test
  void
      givenResponseDecryptionPublicAndPrivateKeyString_whenGetSamlResponseDecryptionKeyPair_thenReturnKeyPairObject() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            "", responseDecryptionPublicKeyString, responseDecryptionPrivateKeyString, "", "");

    KeyPair keyPair = sut.getSamlResponseDecryptionKeyPair();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();

    assertNotNull(keyPair);
    assertNotNull(publicKey);
    assertNotNull(privateKey);
    assertInstanceOf(RSAPublicKey.class, publicKey);
    assertInstanceOf(RSAPrivateKey.class, privateKey);
    assertEquals("X.509", publicKey.getFormat());
    assertEquals("PKCS#8", privateKey.getFormat());
  }

  @Test
  void
      givenResponseVerificationCertificateString_whenGetResponseVerificationCertificate_thenReturnCertificateObject() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl("", "", "", responseVerificationCertificateString, "");

    Certificate certificate = sut.getSamlResponseSignatureValidatingCertificate();

    assertNotNull(certificate);
    assertInstanceOf(X509Certificate.class, certificate);
  }

  @Test
  void
      givenRequestEncryptionCertificateString_whenGetSamlRequestEncryptionCertificate_thenReturnCertificateObject() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl("", "", "", "", requestEncryptionCertificateString);

    Certificate certificate = sut.getSamlRequestEncryptionCertificate();

    assertNotNull(certificate);
    assertInstanceOf(X509Certificate.class, certificate);
  }

  @Test
  void
      givenNoRequestSigningPrivateKeyString_whenGetSamlRequestSigningPrivateKey_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            "",
            responseDecryptionPublicKeyString,
            responseDecryptionPrivateKeyString,
            responseVerificationCertificateString,
            requestEncryptionCertificateString);

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlRequestSigningPrivateKey);

    assertNotNull(exception);
    assertInstanceOf(NullPointerException.class, exception);
  }

  @Test
  void
      givenNoResponseDecryptionPublicKeyString_whenGetSamlResponseDecryptionKeyPair_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            requestSignaturePrivateKeyString,
            "",
            responseDecryptionPrivateKeyString,
            responseVerificationCertificateString,
            requestEncryptionCertificateString);

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlResponseDecryptionKeyPair);

    assertNotNull(exception);
    assertInstanceOf(NullPointerException.class, exception);
  }

  @Test
  void
      givenNoResponseDecryptionPrivateKeyString_whenGetSamlResponseDecryptionKeyPair_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            requestSignaturePrivateKeyString,
            responseDecryptionPublicKeyString,
            "",
            responseVerificationCertificateString,
            requestEncryptionCertificateString);

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlResponseDecryptionKeyPair);

    assertNotNull(exception);
    assertInstanceOf(NullPointerException.class, exception);
  }

  @Test
  void
      givenNoResponseVerificationCertificateString_whenGetSamlResponseSignatureValidatingCertificate_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            requestSignaturePrivateKeyString,
            responseDecryptionPublicKeyString,
            responseDecryptionPrivateKeyString,
            "",
            requestEncryptionCertificateString);

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlResponseSignatureValidatingCertificate);

    assertNotNull(exception);
    assertInstanceOf(IllegalStateException.class, exception);
    assertTrue(
        exception
            .toString()
            .contains("No certificate present to verify signature in SAML response."));
  }

  @Test
  void
      givenNoRequestEncryptionCertificateString_whenGetSamlRequestEncryptionCertificate_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            requestSignaturePrivateKeyString,
            responseDecryptionPublicKeyString,
            responseDecryptionPrivateKeyString,
            responseVerificationCertificateString,
            "");

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlRequestEncryptionCertificate);

    assertNotNull(exception);
    assertInstanceOf(IllegalStateException.class, exception);
    assertTrue(exception.toString().contains("No certificate present to encrypt SAML request."));
  }

  @Test
  void
      givenInvalidRequestSigningPrivateKeyString_whenGetSamlRequestSigningPrivateKey_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            "invalidKeyString",
            responseDecryptionPublicKeyString,
            responseDecryptionPrivateKeyString,
            responseVerificationCertificateString,
            requestEncryptionCertificateString);

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlRequestSigningPrivateKey);

    assertNotNull(exception);
    assertInstanceOf(NullPointerException.class, exception);
  }

  @Test
  void
      givenInvalidResponseDecryptionPublicKeyString_whenGetSamlResponseDecryptionKeyPair_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            requestSignaturePrivateKeyString,
            "invalidKeyString",
            responseDecryptionPrivateKeyString,
            responseVerificationCertificateString,
            requestEncryptionCertificateString);

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlResponseDecryptionKeyPair);

    assertNotNull(exception);
    assertInstanceOf(NullPointerException.class, exception);
  }

  @Test
  void
      givenInvalidResponseDecryptionPrivateKeyString_whenGetSamlResponseDecryptionKeyPair_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            requestSignaturePrivateKeyString,
            responseDecryptionPublicKeyString,
            "invalidKeyString",
            responseVerificationCertificateString,
            requestEncryptionCertificateString);

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlResponseDecryptionKeyPair);

    assertNotNull(exception);
    assertInstanceOf(NullPointerException.class, exception);
  }

  @Test
  void
      givenInvalidResponseVerificationCertificateString_whenGetSamlResponseSignatureValidatingCertificate_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            requestSignaturePrivateKeyString,
            responseDecryptionPublicKeyString,
            responseDecryptionPrivateKeyString,
            "invalidKeyString",
            requestEncryptionCertificateString);

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlResponseSignatureValidatingCertificate);

    assertNotNull(exception);
    assertInstanceOf(IllegalStateException.class, exception);
    assertTrue(
        exception
            .toString()
            .contains("No certificate present to verify signature in SAML response."));
  }

  @Test
  void
      givenInvalidRequestEncryptionCertificateString_whenGetSamlRequestEncryptionCertificate_thenThrowException() {
    SamlKeyMaterialImpl sut =
        new SamlKeyMaterialImpl(
            requestSignaturePrivateKeyString,
            responseDecryptionPublicKeyString,
            responseDecryptionPrivateKeyString,
            responseVerificationCertificateString,
            "invalidKeyString");

    Exception exception =
        assertThrows(RuntimeException.class, sut::getSamlRequestEncryptionCertificate);

    assertNotNull(exception);
    assertInstanceOf(IllegalStateException.class, exception);
    assertTrue(exception.toString().contains("No certificate present to encrypt SAML request."));
  }
}
