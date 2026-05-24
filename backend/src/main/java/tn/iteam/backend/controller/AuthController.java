package tn.iteam.backend.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.entity.User;
import tn.iteam.backend.repository.UserRepository;
import tn.iteam.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username != null) {
            username = username.trim();
        }

        String token = authService.login(username, password);
        User user = userRepository.findByUsername(username).orElseThrow();

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("userId", user.getId());
        resp.put("username", user.getUsername());
        resp.put("fullName", user.getFullName());
        resp.put("role", user.getRole().getName().name());
        return ResponseEntity.ok(resp);
    }
}

