package example.mihael.drsdocumentservice.controllers;

import example.mihael.drsdocumentservice.auth.AuthService;
import example.mihael.drsdocumentservice.dto.LoginDTO;
import example.mihael.drsdocumentservice.dto.UserDTO;
import example.mihael.drsdocumentservice.models.User;
import example.mihael.drsdocumentservice.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.registerUser(userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginDTO loginDTO, HttpSession session) {
        AccessTokenResponse tokenResponse = authService.login(loginDTO.getUsername(), loginDTO.getPassword());
        session.setAttribute("accessToken", tokenResponse.getToken());
        return ResponseEntity.ok(tokenResponse.getToken());
    }
}