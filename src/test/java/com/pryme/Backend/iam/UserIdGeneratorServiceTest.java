package com.pryme.Backend.iam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserIdGeneratorServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserIdGeneratorService generatorService;

    @BeforeEach
    void setUp() {
        generatorService = new UserIdGeneratorService(userRepository);
    }

    @Test
    void mapStateToCode_mapsKnownStates() {
        assertEquals("MH", generatorService.mapStateToCode("Maharashtra"));
        assertEquals("MP", generatorService.mapStateToCode("Madhya Pradesh"));
        assertEquals("DL", generatorService.mapStateToCode("Delhi"));
        assertEquals("XX", generatorService.mapStateToCode(null));
        assertEquals("XX", generatorService.mapStateToCode(""));
        assertEquals("KA", generatorService.mapStateToCode("KA"));
        assertEquals("UT", generatorService.mapStateToCode("Uttarakhand"));
    }

    @Test
    void ensureUserIds_assignsNewCustomerId() {
        User user = new User();
        user.setRole(Role.USER);
        user.setState("Madhya Pradesh");

        when(userRepository.findCustomerIdsByPattern("PRY-CUS-MP%"))
                .thenReturn(Collections.emptyList());

        generatorService.ensureUserIds(user);

        assertEquals("PRY-CUS-MP0000001", user.getCustomerId());
        assertNull(user.getEmployeeId());
    }

    @Test
    void ensureUserIds_assignsNewEmployeeId() {
        User user = new User();
        user.setRole(Role.ADMIN);
        user.setState("Gujarat");

        when(userRepository.findEmployeeIdsByPattern("PRY-EMP-GJ%"))
                .thenReturn(Collections.emptyList());

        generatorService.ensureUserIds(user);

        assertEquals("PRY-EMP-GJ000001", user.getEmployeeId());
        assertNull(user.getCustomerId());
    }

    @Test
    void ensureUserIds_incrementsExistingCustomerIds() {
        User user = new User();
        user.setRole(Role.USER);
        user.setState("Maharashtra");

        List<String> existingIds = Arrays.asList(
                "PRY-CUS-MH0000001",
                "PRY-CUS-MH0000015",
                "PRY-CUS-MH0000002"
        );

        when(userRepository.findCustomerIdsByPattern("PRY-CUS-MH%"))
                .thenReturn(existingIds);

        generatorService.ensureUserIds(user);

        assertEquals("PRY-CUS-MH0000016", user.getCustomerId());
    }

    @Test
    void ensureUserIds_incrementsExistingEmployeeIds() {
        User user = new User();
        user.setRole(Role.EMPLOYEE);
        user.setState("Rajasthan");

        List<String> existingIds = Arrays.asList(
                "PRY-CUS-RJ0000001", // Should ignore customer ids
                "PRY-EMP-RJ000001",
                "PRY-EMP-RJ000005"
        );

        when(userRepository.findEmployeeIdsByPattern("PRY-EMP-RJ%"))
                .thenReturn(existingIds);

        generatorService.ensureUserIds(user);

        assertEquals("PRY-EMP-RJ000006", user.getEmployeeId());
    }
}
