package example.mihael.drsdocumentservice.services;

import example.mihael.drsdocumentservice.auth.KeycloakAdminService;
import example.mihael.drsdocumentservice.dto.UserDTO;
import example.mihael.drsdocumentservice.models.User;
import example.mihael.drsdocumentservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    // TODO: Improve
    public User registerUser(UserDTO userDTO) {
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setUsername(userDTO.getUsername());
        user.setRole("user");

        keycloakAdminService.createUserInKeycloak(userDTO);

        return userRepository.save(user);
    }
}
