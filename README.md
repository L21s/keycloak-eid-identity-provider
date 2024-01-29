# eID Identity Provider for Keycloak
![demo](https://github.com/L21s/keycloak-eid-identity-provider/assets/85928453/6e00db3a-99c3-4fe7-8475-77ec7c90ec34)
This plugin delivers the eID identity provider that enables user creation and authentication with the German ID card.

## Known limitations
Users are authenticated with the `restrictedID` which is assigned to exactly one ID card. 
The `restrictedID` changes when a user gets a new ID card. Currently, there is no solution implemented to update a user account in this case.

## Installation guide
### Requirements
For the authentication process, Keycloak communicates with the [AusweisApp](https://github.com/Governikus/AusweisApp) and the [Governikus ID Panstar](https://www.governikus.de/en/loesungen/produkte/id-panstar/) server.
It is necessary to have access to both, the respective Governikus ID Panstar Server URL and the keys and certificates that secure the communication with the server.
To get you started as fast as possible, we deliver an out-of-the-box solution that uses publicly available keys and certificates.
This must be understood as a Proof-of-Concept and is NOT production ready.

### Quickstart
Use the following commands to set up Keycloak with the eID identity provider plugin in a Docker container that runs on `https://localhost:8443`.  
  
`git clone git@github.com:L21s/keycloak-eid-identity-provider.git`  
`cd keycloak-eid-identity-provider`  
`mvn clean package -P dev`  
`docker-compose up`

### Configuration
Log in to the Keycloak Admin UI with `admin` as Username and Password, go to identity providers, and select eID.
For a working eID identity provider, Client Id and Client Secret are not necessary but the current Keycloak implementation requires dummy values. More information are provided [here](https://github.com/keycloak/keycloak/issues/21891).
ID Panstar Server URL `https://dev.id.governikus-eid.de/gov_autent/async` and SAML Request Entity Base URL `https://localhost:8443` must be configured.
In addition, the required keys and certificates are stored at `src/main/resources/keys`.
They are named after their respective configuration purposes and the order in which they must be selected.  

![screencapture-localhost-8443-admin-master-console-2024-01-29-11_07_58](https://github.com/L21s/keycloak-eid-identity-provider/assets/85928453/4a24f3e9-9dc7-4238-89a0-4db38819a166)

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

