package com.pryme.Backend.iam;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@pryme.com");
            admin.setPasswordHash(passwordEncoder.encode("password123"));
            admin.setRole(Role.SUPER_ADMIN);
            admin.setFullName("Aadesh SuperAdmin");
            admin.setPhone("+91-8144426440");

            userRepository.save(admin);
        }
    }
}
