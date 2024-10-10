# Configuration of Keycloak
## 1) Configure Keycloak to enable the eID provider
Follow these steps to configure the eID identity provider for using the Proof-of-Concept. 
1. Go to `https://localhost:8443` and log in to the Keycloak Admin UI with `admin` as Username and Password.
2. Go to identity providers and select eID.
3. Set dummy values for Client Id and Client Secret. They are not necessary for a functioning eID identity provider but are required by the current [Keycloak implementation](https://github.com/keycloak/keycloak/issues/21891).  
4. Set the ID Panstar Server URL to `https://dev.id.governikus-eid.de/gov_autent/async`. 
5. Set the SAML Request Entity Base URL to `https://localhost:8443`.
6. Set the keys and certificates stored at `src/main/resources/keys` in the order specified by their names.   

The final configuration looks like this.

![screencapture-localhost-8443-admin-master-console-2024-01-29-11_07_58](https://github.com/L21s/keycloak-eid-identity-provider/assets/85928453/4a24f3e9-9dc7-4238-89a0-4db38819a166)

Of course, in real world scenarios you'd need different eID servers and thus also different keys/certificates.

## 2) Create the client to work with the test frontend
1. Go to `https://localhost:8443` and log in to the Keycloak Admin UI with `admin` as Username and Password.
2. Go to Clients
3. Click "Create Client"  
    3.1 *General Settings*: Set `eid-test-frontend` as the client id  
    3.2 *Capability config*: no config needed, just click next  
    3.3 *Login Settings* Set the Root URL to `http://localhost:4200` and the redirect URIs and web origins to `*`  
4. Click save