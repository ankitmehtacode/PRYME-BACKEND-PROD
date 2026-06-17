package com.pryme.Backend.iam;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserIdGeneratorService userIdGeneratorService;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder, UserIdGeneratorService userIdGeneratorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userIdGeneratorService = userIdGeneratorService;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@pryme.com");
            admin.setPasswordHash(passwordEncoder.encode("password123"));
            admin.setRole(Role.SUPER_ADMIN);
            admin.setFullName("Aadesh SuperAdmin");
            admin.setPhone("+91-8144426440");
            admin.setState("MP");
            userIdGeneratorService.ensureUserIds(admin);

            userRepository.save(admin);
        }

        // Backfill existing users who do not have a customer or employee ID
        java.util.List<User> allUsers = userRepository.findAll();
        allUsers.sort((u1, u2) -> {
            if (u1.getCreatedAt() != null && u2.getCreatedAt() != null) {
                return u1.getCreatedAt().compareTo(u2.getCreatedAt());
            }
            if (u1.getId() != null && u2.getId() != null) {
                return u1.getId().compareTo(u2.getId());
            }
            return 0;
        });

        for (User user : allUsers) {
            if (user.getRole() == Role.USER) {
                if (user.getCustomerId() == null) {
                    userIdGeneratorService.ensureUserIds(user);
                    userRepository.save(user);
                }
            } else {
                if (user.getEmployeeId() == null) {
                    userIdGeneratorService.ensureUserIds(user);
                    userRepository.save(user);
                }
            }
        }
    }
}
