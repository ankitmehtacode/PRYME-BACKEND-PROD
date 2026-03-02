package com.pryme.Backend.iam;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        // 🧠 160 IQ: Log exactly what is arriving from the frontend
        System.out.println("DEBUG: Login Attempt for Email: [" + request.email() + "]");

        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> {
                    System.out.println("DEBUG: User not found in database.");
                    return new RuntimeException("Invalid email or password");
                });

        // 🧠 160 IQ: Trim whitespaces to prevent hidden space errors
        String receivedPassword = request.password().trim();
        String storedPassword = user.getPasswordHash().trim();

        System.out.println("DEBUG: Comparing Received: [" + receivedPassword + "] vs Stored: [" + storedPassword + "]");

        if (!storedPassword.equals(receivedPassword)) {
            System.out.println("DEBUG: Password Mismatch!");
            throw new RuntimeException("Invalid email or password");
        }

        String token = "PRYME-" + user.getId() + "-" + System.currentTimeMillis();

        return Map.of(
                "token", token,
                "role", user.getRole(),
                "name", user.getFullName(),
                "message", "Login Successful"
        );
    }
}

record LoginRequest(String email, String password) {}