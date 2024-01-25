# eID Identity Provider for Keycloak
![demo](https://github.com/L21s/keycloak-eid-identity-provider/assets/85928453/6e00db3a-99c3-4fe7-8475-77ec7c90ec34)
Use this plugin to create and authenticate users with the German ID card and the [AusweisApp](https://github.com/Governikus/AusweisApp).

## Known limitations
Users are authenticated with the `restrictedID` which is assigned to exactly one ID card. 
This ID changes when a user gets a new card. Currently, there is no solution implemented to update a user account in this case.

## Installation guide
### Requirements
For the authentication process Keycloak communicates with the Governikus ID Panstar Server and the AusweisApp.
It is necessary to have access to both, the respective ID Panstar Server URL and the certificates that secure the communication with the server.
Further details are not described, but we are happy to give you more insights. Feel free to reach out: fabian.kuenzer@l21s.de.  

To get you started as fast as possible, we deliver an out-of-the-box solution that uses publicly available keys and certificates to secure the communication.
This must be understood as a Proof-of-Concept and is NOT production ready.

### Quickstart
Use the following commands to set up Keycloak with the eID plugin in a Docker container that runs on `https://localhost:8443`.  
`git clone git@github.com:L21s/keycloak-extension.git`  
`cd keycloak-extension`  
`mvn clean package -P dev`  
`docker-compose up`

### Configuration
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

### Setup
- Build project
- Copy to `providers` directory
- Run with kc shell
- Check log for success
- More details: [Keycloak Server Developer Guide](https://www.keycloak.org/docs/latest/server_development/index.html#_providers)
- add pictures

## Development
- Build with Maven
- Code style for IntelliJ
- Swimlanes with Mermaid

