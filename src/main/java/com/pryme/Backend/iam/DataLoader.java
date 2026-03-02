package com.pryme.Backend.iam;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Only create the admin if the table is completely empty
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@pryme.com");
            admin.setPasswordHash("password123"); // In the security phase, we will BCrypt this
            admin.setRole(Role.SUPER_ADMIN);
            admin.setFullName("Aadesh SuperAdmin");
            admin.setPhone("+91-8144426440");

            userRepository.save(admin);
            System.out.println("✅ SUPER_ADMIN account created: admin@pryme.com");
        }
    }
}