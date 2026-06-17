package com.pryme.Backend.iam;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    long countByRole(Role role);

    /**
     * 🧠 TEAM MEMBER DROPDOWN ENGINE:
     * Fetches only internal staff (EMPLOYEE, ADMIN, SUPER_ADMIN) for the lead assignment dropdown.
     * Spring Data JPA auto-generates: SELECT * FROM users WHERE role IN (...)
     */
    List<User> findByRoleIn(List<Role> roles);

    @org.springframework.data.jpa.repository.Query("SELECT u.customerId FROM User u WHERE u.customerId LIKE :pattern")
    List<String> findCustomerIdsByPattern(String pattern);

    @org.springframework.data.jpa.repository.Query("SELECT u.employeeId FROM User u WHERE u.employeeId LIKE :pattern")
    List<String> findEmployeeIdsByPattern(String pattern);
}