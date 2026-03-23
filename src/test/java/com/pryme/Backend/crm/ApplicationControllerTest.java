package com.pryme.Backend.crm;

import com.pryme.Backend.common.UnauthorizedException; // 🧠 FIXED: Matches your actual architectural exception
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    private ApplicationController controller;

    @BeforeEach
    void setup() {
        controller = new ApplicationController(applicationService);
    }

    @Test
    void myApplicationsReturnsOwnedApplications() {
        // 🧠 SETUP: Simulating a strictly-typed UUID from the JWT Context
        UUID userId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken(userId, "token");

        when(applicationService.listMyApplications(userId, Pageable.unpaged())).thenReturn(List.of());

        // ⚡ EXECUTE
        ResponseEntity<List<ApplicationResponse>> response = controller.getMyApplications(auth);

        // 🛡️ ASSERT
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());

        verify(applicationService).listMyApplications(userId, Pageable.unpaged());
    }

    @Test
    void myApplicationsRequiresAuthenticationPrincipal() {
        // 🧠 SETUP: Simulating an invalid string principal instead of a secure UUID
        var auth = new UsernamePasswordAuthenticationToken("anonymous_string_user", "token");

        // 🛡️ ASSERT: The Code X Architecture dictates this must throw an UnauthorizedException
        assertThrows(UnauthorizedException.class, () -> controller.getMyApplications(auth));
    }

    @Test
    void myApplicationsRequiresNonNullAuthentication() {
        // 🛡️ ASSERT: Completely null authentication should also instantly bounce
        assertThrows(UnauthorizedException.class, () -> controller.getMyApplications(null));
    }
}
