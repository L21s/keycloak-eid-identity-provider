# eID Identity Provider for Keycloak
![Uploading demo.gif…]()

## Run Keycloak with eID Identity Provider Extension
`git clone git@github.com:L21s/keycloak-extension.git`  
`cd keycloak-extension`  
`mvn clean package -P dev`  
`docker-compose up`  

## Configure eID Identity Provider in Keycloak Admin UI
- Admin UI: `https://localhost:8443`, Username: `admin`, Password: `admin`
- Specific Client Id and Client Secret are not necessary for the eID IDP but the current Keycloak implementation requires dummy values: [GitHub Issue](https://github.com/keycloak/keycloak/issues/21891)
- ID Panstar Server URL: `https://dev.id.governikus-eid.de/gov_autent/async`
- SAML Request Entity Base URL: `https://localhost:8443`
- Keys are stored at `src/main/resources/keys`.
  - SAML Request Signature Private Key: `01samlRequestSignaturePrivateKey.txt`
  - SAML Response Decryption Public Key: `02samlResponseDecryptionPublicKey.txt`
  - SAML Response Decryption Private Key: `03samlResponseDecryptionPrivateKey.txt`
  - SAML Response Verification Certificate: `04samlResponseVerificationCertificate.txt`
  - SAML Request Encryption Certificate: `05samlRequestEncryptionCertificate.txt`
