package de.l21s.keycloak.eid.configuration;

import de.governikus.panstar.sdk.saml.configuration.SamlKeyMaterial;
import de.governikus.panstar.sdk.utils.constant.Common;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamlKeyMaterialImpl implements SamlKeyMaterial {

  private static final Logger logger = LoggerFactory.getLogger(SamlKeyMaterialImpl.class);
  String samlRequestSignaturePrivateKey;
  String samlResponseDecryptionPublicKey;
  String samlResponseDecryptionPrivateKey;
  String samlResponseVerificationCertificate;
  String samlRequestEncryptionCertificate;

  SamlKeyMaterialImpl(
      String samlRequestSignaturePrivateKey,
      String samlResponseDecryptionPublicKey,
      String samlResponseDecryptionPrivateKey,
      String samlResponseVerificationCertificate,
      String samlRequestEncryptionCertificate) {
    this.samlRequestSignaturePrivateKey = samlRequestSignaturePrivateKey;
    this.samlResponseDecryptionPublicKey = samlResponseDecryptionPublicKey;
    this.samlResponseDecryptionPrivateKey = samlResponseDecryptionPrivateKey;
    this.samlResponseVerificationCertificate = samlResponseVerificationCertificate;
    this.samlRequestEncryptionCertificate = samlRequestEncryptionCertificate;
  }

  @Override
  public PrivateKey getSamlRequestSigningPrivateKey() {
    try {
      PemReader privateKeyReader = new PemReader(new StringReader(samlRequestSignaturePrivateKey));
      byte[] privateKeyContent = privateKeyReader.readPemObject().getContent();
      PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyContent);
      KeyFactory fact = KeyFactory.getInstance("RSA");

      return fact.generatePrivate(privateKeySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public KeyPair getSamlResponseDecryptionKeyPair() {
    try {
      PemReader publicKeyReader = new PemReader(new StringReader(samlResponseDecryptionPublicKey));
      byte[] publicKeyContent = publicKeyReader.readPemObject().getContent();
      X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyContent);

      PemReader privateKeyReader =
          new PemReader(new StringReader(samlResponseDecryptionPrivateKey));
      byte[] privateKeyContent = privateKeyReader.readPemObject().getContent();
      PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyContent);

      KeyFactory fact = KeyFactory.getInstance("RSA");

      return new KeyPair(fact.generatePublic(publicKeySpec), fact.generatePrivate(privateKeySpec));
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public X509Certificate getSamlResponseSignatureValidatingCertificate() {
    try {
      PemReader certPem = new PemReader(new StringReader(samlResponseVerificationCertificate));
      byte[] content = certPem.readPemObject().getContent();
      CertificateFactory cf = CertificateFactory.getInstance("X509", Common.BOUNCY_PROVIDER);
      Optional<X509Certificate> cert =
          Optional.of((X509Certificate) cf.generateCertificate(new ByteArrayInputStream(content)));

      return cert.orElseThrow(IllegalStateException::new);
    } catch (Exception e) {
      logger.warn("No certificate present to verify signature in SAML response");
      throw new IllegalStateException(
          "No certificate present to verify signature in SAML response. Without a certificate to validate the signature the SAML response cannot be parsed",
          e);
    }
  }

  @Override
  public X509Certificate getSamlRequestEncryptionCertificate() {
    try {
      PemReader certPem = new PemReader(new StringReader(samlRequestEncryptionCertificate));
      byte[] content = certPem.readPemObject().getContent();
      CertificateFactory cf = CertificateFactory.getInstance("X509", Common.BOUNCY_PROVIDER);
      Optional<X509Certificate> cert =
          Optional.of((X509Certificate) cf.generateCertificate(new ByteArrayInputStream(content)));

      return cert.orElseThrow(IllegalStateException::new);
    } catch (Exception e) {
      logger.warn("No certificate present to verify signature in SAML response");
      throw new IllegalStateException(
          "No certificate present to encrypt SAML request. Without an encryption certificate no SAML request can be created",
          e);
    }
  }
}
