package com.pryme.Backend.crm;

import com.pryme.Backend.common.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        UUID userId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken(userId, "token");

        when(applicationService.listMyApplications(userId)).thenReturn(List.of());

        var response = controller.myApplications(auth);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void myApplicationsRequiresAuthenticationPrincipal() {
        var auth = new UsernamePasswordAuthenticationToken("anonymous", "token");
        assertThrows(ForbiddenException.class, () -> controller.myApplications(auth));
    }
}
