package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.policy.provider.ActiveBundlePolicyProvider;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import java.util.stream.Collectors;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb?sslmode=require&channelBinding=require",
        "spring.datasource.username=neondb_owner",
        "spring.datasource.password=npg_VbzCd0Anf8oZ",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
public class DebugCertificationDataTest {

    @MockBean private RedisClient redisClient;
    @MockBean private ProxyManager<byte[]> proxyManager;

    @Autowired
    private ActiveBundlePolicyProvider activeBundlePolicyProvider;

    @Test
    public void dumpActiveBundle() {
        PolicyBundle active = activeBundlePolicyProvider.getActiveBundle();
        System.out.println("Active Bundle: " + (active != null));
        if (active != null) {
            System.out.println("Rules count: " + active.eligibilityRules().size());
            System.out.println("First few rows:");
            active.eligibilityRules().stream().limit(5).forEach(System.out::println);
            
            System.out.println("\nDistinct Lenders:");
            System.out.println(active.eligibilityRules().stream().map(r -> r.lenderName()).distinct().collect(Collectors.toList()));
            
            System.out.println("\nDistinct Product Names:");
            System.out.println(active.eligibilityRules().stream().map(r -> r.productName()).distinct().collect(Collectors.toList()));
            
            System.out.println("\nDistinct Loan Types:");
            System.out.println(active.eligibilityRules().stream().map(r -> r.loanType()).distinct().collect(Collectors.toList()));
        }
    }
}
