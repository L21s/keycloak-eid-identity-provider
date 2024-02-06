# eID Identity Provider for Keycloak  
![Keycloak Version](https://img.shields.io/badge/Keycloak_Version-23-blue) ![License: MIT](https://img.shields.io/badge/License-MIT-yellow)  ![Build](https://github.com/L21s/keycloak-eid-identity-provider/actions/workflows/build.yaml/badge.svg)  

![demo](https://github.com/L21s/keycloak-eid-identity-provider/assets/85928453/6e00db3a-99c3-4fe7-8475-77ec7c90ec34)  

This plugin delivers the eID identity provider which enables user creation and authentication with the German ID card.

## Known limitations
Users are authenticated with the `restrictedID` which is assigned to exactly one ID card. 
The `restrictedID` changes when a user gets a new ID card. Currently, there is no solution implemented to update a user account in this case.

## Installation guide
### Requirements
For the authentication process, Keycloak communicates with the [Governikus ID Panstar](https://www.governikus.de/en/loesungen/produkte/id-panstar/) server and the locally running [AusweisApp](https://www.ausweisapp.bund.de/download).
It is necessary to have access to both, the respective Governikus ID Panstar Server URL and the keys and certificates that secure the communication with the server.
To get you started as fast as possible, we deliver an out-of-the-box solution that uses publicly available keys and certificates provided by the Governikus ID Panstar SDK.
This must be understood as a Proof-of-Concept and is NOT production ready. Feel free to reach out to us if you have any questions: jan.schmitz-hermes@l21s.de.

### Quickstart with Docker
Use the following commands to set up Keycloak with the eID identity provider plugin in a Docker container.  
  
`git clone git@github.com:L21s/keycloak-eid-identity-provider.git`  
`cd keycloak-eid-identity-provider`  
`mvn clean package -P dev`  
`docker-compose up`

### Configuration
#### Keycloak
Follow these steps to configure the eID identity provider for using the Proof-of-Concept. 
1. Go to `https://localhost:8443` and log in to the Keycloak Admin UI with `admin` as Username and Password.
2. Go to identity providers and select eID.
3. Set dummy values for Client Id and Client Secret. They are not necessary for a functioning eID identity provider but are required by the current [Keycloak implementation](https://github.com/keycloak/keycloak/issues/21891).  
4. Set the ID Panstar Server URL to `https://dev.id.governikus-eid.de/gov_autent/async`. 
5. Set the SAML Request Entity Base URL to `https://localhost:8443`.
6. Set the keys and certificates stored at `src/main/resources/keys` in the order specified by their names.   

The final configuration looks like this.

![screencapture-localhost-8443-admin-master-console-2024-01-29-11_07_58](https://github.com/L21s/keycloak-eid-identity-provider/assets/85928453/4a24f3e9-9dc7-4238-89a0-4db38819a166)

#### AusweisApp
For using the Proof-of-Concept, the AusweisApp must be running on the same machine as Keycloak. In addition, it must be configured to mock an ID card.
Therefore, the developer mode must be activated as described [here](https://www.ausweisapp.bund.de/ausweisapp2/help/1.20/en/Windows/settings-developer.html#aktivieren-des-entwicklermodus).
Afterward, go to Settings -> Developer options and activate the internal card simulator.
Now, everything is set to go to `https://localhost:8443`, log out of the admin console, and click `eid` to start the authentication process.  

### Setup without Docker
Follow these steps to run Keycloak including the eID identity provider without Docker:
1. Download Keycloak [here](https://github.com/keycloak/keycloak/releases). 
2. Go to this project's directory and run `mvn clean package -P dev`.
3. Copy the content of this project's `target/deploy` directory to the downloaded Keycloak's `providers` directory as described [here](https://www.keycloak.org/docs/latest/server_development/index.html#registering-provider-implementations).
4. Open the keycloak directory and use `bin/kc.sh build` to build the project including the eID identity provider.
5. Run `bin/kc.sh start-dev --https-key-store-file=<path-to-keycloak-eid-identity-provider-repository>/src/main/resources/keys/tls-ssl-commcert.p12 --https-key-store-password=123456` to start Keycloak.
6. Go to `https://localhost:8443` and manually create a user to add and configure an eID identity provider.

If the eID identity provider plugin was registered successfully, `TcTokenEndpointFactory` and `EidIdentityProviderFactory` are detected and logged throughout the execution of the build and the run command.  

<img width="1730" alt="Bildschirmfoto 2024-01-29 um 12 06 30" src="https://github.com/L21s/keycloak-eid-identity-provider/assets/85928453/1844fcfd-b863-4db6-944c-a383e56a3906">

## Development
### Code style
The [Google Java Style](https://google.github.io/styleguide/javaguide.html) is used for this project. There are two ways to format your code accordingly:
1. Download the respective XML file from the [Google Style Guide](https://github.com/google/styleguide) repository and follow [these](https://github.com/google/google-java-format?tab=readme-ov-file#using-the-formatter) instructions to format the code with your IDE.
2. Run `mvn com.spotify.fmt:fmt-maven-plugin:format` from the terminal in the repository's directory.  

If the code is not formatted correctly, the build will fail.

### Authentication flow
```mermaid
sequenceDiagram
    participant Browser
    participant Keycloak
    participant AusweisApp
    participant ID Panstar Server
    Browser->>Keycloak: Request authentication with eID
    Keycloak->>Browser: Generate Tc Token URL
    Browser->>AusweisApp: Redirect with Tc Token URL
    AusweisApp->>Keycloak: Request Tc Token Endpoint
    Keycloak->>Keycloak: Generate SAML request
    Keycloak->>ID Panstar Server: Send SAML request
    ID Panstar Server->>AusweisApp: Request ID card information
    AusweisApp->>AusweisApp: Read ID card information
    AusweisApp->>ID Panstar Server: Send ID card information
    ID Panstar Server->>ID Panstar Server: Process information and create SAML response 
    ID Panstar Server->>Keycloak: Send SAML authentication response
    Keycloak->>Keycloak: Parse SAML response and authenticate or create user
    Keycloak->>Browser: Retrieve JSON Web Token
```

More technical details can be found [here](https://www.bsi.bund.de/DE/Themen/Unternehmen-und-Organisationen/Standards-und-Zertifizierung/Technische-Richtlinien/TR-nach-Thema-sortiert/tr03130/tr-03130.html).
