package com.pryme.Backend.eligibility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryme.Backend.eligibility.audit.*;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.iam.User;
import com.pryme.Backend.iam.Role;
import com.pryme.Backend.iam.UserRepository;
import com.pryme.Backend.iam.SessionManager;
import com.pryme.Backend.iam.SessionRecord;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://ep-empty-boat-a1abgqec-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require",
        "spring.datasource.username=neondb_owner",
        "spring.datasource.password=npg_VbzCd0Anf8oZ",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditComparisonTest {

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
    private SessionManager sessionManager;

    private User adminUser;
    private final String sessionToken = "11111111-2222-3333-4444-555555555555";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create Super Admin test user
        adminUser = User.builder()
                .email("superadmin@pryme.com")
                .fullName("Super Admin User")
                .phone("9876543211")
                .role(Role.SUPER_ADMIN)
                .passwordHash("dummyAdminHash")
                .build();
        adminUser = userRepository.save(adminUser);

        // Mock ProxyManager
        var mockBuilder = Mockito.mock(io.github.bucket4j.distributed.proxy.RemoteBucketBuilder.class);
        var mockBucket = Mockito.mock(io.github.bucket4j.distributed.BucketProxy.class);
        Mockito.when(proxyManager.builder()).thenReturn(mockBuilder);
        Mockito.when(mockBuilder.build(Mockito.any(), Mockito.any(java.util.function.Supplier.class))).thenReturn(mockBucket);
        Mockito.when(mockBucket.tryConsume(Mockito.anyLong())).thenReturn(true);

        // Mock SessionManager
        SessionRecord sessionRecord = SessionRecord.builder()
                .id(UUID.fromString(sessionToken))
                .user(adminUser)
                .expiresAt(Instant.now().plusSeconds(3600))
                .isActive(true)
                .build();
        Mockito.when(sessionManager.validate(Mockito.anyString())).thenReturn(sessionRecord);

        // Authenticate the request context
        var auth = new UsernamePasswordAuthenticationToken(
                adminUser.getId(),
                sessionToken,
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testGetMasterVersion() throws Exception {
        mockMvc.perform(get("/api/v1/admin/eligibility/master-version")
                        .cookie(new jakarta.servlet.http.Cookie("PRYME_SID", sessionToken)))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminEvaluateEndpoint() throws Exception {
        EligibilityRequest request = createSampleRequest();

        mockMvc.perform(post("/api/v1/admin/eligibility/evaluate")
                        .cookie(new jakarta.servlet.http.Cookie("PRYME_SID", sessionToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].decisionTrace").exists());
    }

    @Test
    void testAdminAuditEndpoint() throws Exception {
        EligibilityRequest request = createSampleRequest();

        mockMvc.perform(post("/api/v1/admin/eligibility/audit")
                        .cookie(new jakarta.servlet.http.Cookie("PRYME_SID", sessionToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.traces").isArray())
                .andExpect(jsonPath("$.masterDataVersion").exists())
                .andExpect(jsonPath("$.requestHash").exists());
    }

    @Test
    void testAdminCompareEndpoint() throws Exception {
        EligibilityRequest request = createSampleRequest();
        AuditComparisonRequest.ExpectedResult expected = new AuditComparisonRequest.ExpectedResult(
                "NIP",
                new BigDecimal("2500000"),
                BigDecimal.ZERO,
                new BigDecimal("0.75"),
                new BigDecimal("0.095"),
                new BigDecimal("50000"),
                true
        );
        AuditComparisonRequest compareRequest = new AuditComparisonRequest(request, expected);

        mockMvc.perform(post("/api/v1/admin/eligibility/compare")
                        .cookie(new jakarta.servlet.http.Cookie("PRYME_SID", sessionToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compareRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expected").exists())
                .andExpect(jsonPath("$.actual").exists())
                .andExpect(jsonPath("$.mismatches").isArray());
    }

    private EligibilityRequest createSampleRequest() {
        IncomeComputationInput incomeInput = new IncomeComputationInput(
                "NIP",
                new BigDecimal("600000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of(),
                BigDecimal.ZERO,
                "",
                BigDecimal.ZERO,
                "",
                "",
                ""
        );

        return new EligibilityRequest(
                1L, // HDFC
                "HL",
                750,
                35,
                "Salaried",
                "Flat/Apartment/House",
                "Tier 1",
                new BigDecimal("2500000"),
                new BigDecimal("4000000"),
                180,
                new BigDecimal("50000"),
                BigDecimal.ZERO,
                3,
                5,
                incomeInput,
                "idempotency-test-compare-endpoint",
                3,
                new BigDecimal("50000"),
                "452001",
                "Flat/Apartment/House",
                null
        );
    }
}
