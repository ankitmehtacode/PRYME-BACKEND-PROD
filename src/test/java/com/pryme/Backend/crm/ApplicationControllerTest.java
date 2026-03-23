package com.pryme.Backend.crm;

import com.pryme.Backend.common.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

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
        UUID userId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken(userId, "token");

        when(applicationService.listMyApplications(userId, Pageable.unpaged())).thenReturn(Page.empty());

        ResponseEntity<Page<ApplicationResponse>> response = controller.getMyApplications(auth);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());

        verify(applicationService).listMyApplications(userId, Pageable.unpaged());
    }

    @Test
    void myApplicationsRequiresAuthenticationPrincipal() {
        var auth = new UsernamePasswordAuthenticationToken("anonymous_string_user", "token");

        assertThrows(ForbiddenException.class, () -> controller.getMyApplications(auth));
    }

    @Test
    void myApplicationsRequiresNonNullAuthentication() {
        assertThrows(ForbiddenException.class, () -> controller.getMyApplications(null));
    }
}
