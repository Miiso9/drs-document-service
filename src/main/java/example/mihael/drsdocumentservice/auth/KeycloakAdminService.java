package example.mihael.drsdocumentservice.auth;

import example.mihael.drsdocumentservice.dto.UserDTO;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class KeycloakAdminService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    private static final String ADMIN_CLI = "admin-cli";

    public void createUserInKeycloak(UserDTO userDTO) {

        try (Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakServerUrl)
                .realm("master")
                .clientId(ADMIN_CLI)
                .username("admin")
                .password("admin")
                .build()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation user = getUserRepresentation(userDTO);

            Response response = usersResource.create(user);
            if (response.getStatus() != 201) {
                throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
            }

            String userId = extractUserIdFromResponse(response);

            assignRoleToUser(keycloak, userId, "user");

            log.info("User created and role assigned successfully.");
        } catch (Exception e) {
            throw new RuntimeException("Error creating user in Keycloak: " + e.getMessage(), e);
        }
    }

    private static UserRepresentation getUserRepresentation(UserDTO userDTO) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEnabled(true);

        CredentialRepresentation credentials = new CredentialRepresentation();
        credentials.setTemporary(false);
        credentials.setType(CredentialRepresentation.PASSWORD);
        credentials.setValue(userDTO.getPassword());
        user.setCredentials(Collections.singletonList(credentials));
        return user;
    }

    private void assignRoleToUser(Keycloak keycloak, String userId, String roleName) {
        RealmResource realmResource = keycloak.realm(realm);

        List<ClientRepresentation> clients = realmResource.clients().findByClientId(clientId);
        if (clients.isEmpty()) {
            throw new IllegalArgumentException("Client with clientId '" + clientId + "' not found.");
        }
        ClientRepresentation client = clients.get(0);

        ClientResource clientResource = realmResource.clients().get(client.getId());

        RoleRepresentation role = clientResource.roles().get(roleName).toRepresentation();
        if (role == null) {
            throw new IllegalArgumentException("Role '" + roleName + "' not found for client '" + clientId + "'.");
        }

        realmResource.users().get(userId).roles().clientLevel(client.getId()).add(Collections.singletonList(role));
    }

    private String extractUserIdFromResponse(Response response) {
        String location = response.getHeaderString("Location");
        if (location == null) {
            throw new RuntimeException("Failed to retrieve user ID from Keycloak response.");
        }
        return location.substring(location.lastIndexOf("/") + 1);
    }
}
