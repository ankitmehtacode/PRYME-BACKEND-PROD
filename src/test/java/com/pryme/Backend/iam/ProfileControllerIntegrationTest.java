package com.pryme.Backend.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryme.Backend.config.SecurityConfig;
import com.pryme.Backend.document.S3PresignedUrlService;
import com.pryme.Backend.document.S3PresignedUrlService.PresignedUrlResponse;
import com.pryme.Backend.iam.dto.ProfileUpdateRequest;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisClient redisClient;

    @MockBean
    private ProxyManager<byte[]> proxyManager;

    @MockBean
    private S3PresignedUrlService s3PresignedUrlService;

    @MockBean
    private SessionManager sessionManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("testprofile@pryme.com");
        testUser.setFullName("Test User");
        testUser.setPhone("9876543210");
        testUser.setRole(Role.USER);
        testUser.setPasswordHash("dummyHash");
        testUser = userRepository.save(testUser);

        // Mock S3
        PresignedUrlResponse dummyResponse = new PresignedUrlResponse("http://dummy-s3-url", "dummy-key", Instant.now());
        Mockito.when(s3PresignedUrlService.generateDownloadUrl(Mockito.anyString())).thenReturn(dummyResponse);

        // Mock ProxyManager for rate limiting filter
        var mockBuilder = Mockito.mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        var mockBucket = Mockito.mock(io.github.bucket4j.distributed.BucketProxy.class);
        Mockito.when(proxyManager.builder()).thenReturn(mockBuilder);
        Mockito.when(mockBuilder.build(Mockito.any(), Mockito.any(java.util.function.Supplier.class))).thenReturn(mockBucket);
        Mockito.when(mockBucket.tryConsume(Mockito.anyLong())).thenReturn(true);

        // Mock SessionManager
        SessionRecord sessionRecord = SessionRecord.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .user(testUser)
                .expiresAt(Instant.now().plusSeconds(3600))
                .isActive(true)
                .build();
        Mockito.when(sessionManager.validate(Mockito.anyString())).thenReturn(sessionRecord);

        // Authenticate as testUser
        var auth = new UsernamePasswordAuthenticationToken(
                testUser.getId(),
                "11111111-2222-3333-4444-555555555555",
                SecurityContextHolder.getContext().getAuthentication() != null
                        ? SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                        : java.util.List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile")
                        .cookie(new jakarta.servlet.http.Cookie("PRYME_SID", "11111111-2222-3333-4444-555555555555")))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateAndRetrieveProfile() throws Exception {
        Map<String, Object> newMetadata = new HashMap<>();
        newMetadata.put("panCard", "ABCDE1234F");
        newMetadata.put("monthlyIncome", "60000");

        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
                "Updated Name",
                "9999999999",
                "Mumbai",
                "Maharashtra",
                null,
                newMetadata
        );

        mockMvc.perform(put("/api/v1/users/profile")
                        .cookie(new jakarta.servlet.http.Cookie("PRYME_SID", "11111111-2222-3333-4444-555555555555"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Reload user from database and assert persistence
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("Updated Name", updatedUser.getFullName());
        assertEquals("9999999999", updatedUser.getPhone());
        assertEquals("Mumbai", updatedUser.getCity());
        assertEquals("Maharashtra", updatedUser.getState());
        assertNotNull(updatedUser.getMetadata());
        assertEquals("ABCDE1234F", updatedUser.getMetadata().get("panCard"));
        assertEquals("60000", updatedUser.getMetadata().get("monthlyIncome"));

        // Call GET and verify JSON output contains the same values
        String responseContent = mockMvc.perform(get("/api/v1/users/profile")
                        .cookie(new jakarta.servlet.http.Cookie("PRYME_SID", "11111111-2222-3333-4444-555555555555")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(responseContent.contains("Mumbai"));
        assertTrue(responseContent.contains("Maharashtra"));
        assertTrue(responseContent.contains("ABCDE1234F"));
        assertTrue(responseContent.contains("60000"));
    }
}
