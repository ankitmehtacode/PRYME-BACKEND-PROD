package com.pryme.Backend.eligibility;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;

import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
public class DebugFoirTest {

    @MockBean
    private ProxyManager<String> proxyManager;

    @MockBean
    private RedisClient redisClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void dumpFoir() {
        System.out.println("=== DUMPING FOIR FOR TATA & JIO ===");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT lp.product_code, pf.* FROM product_foir_matrix pf " +
            "JOIN loan_products lp ON pf.product_id = lp.id " +
            "WHERE lp.product_code LIKE 'TATA%' OR lp.product_code LIKE 'JIO%' " +
            "ORDER BY lp.product_code, pf.employment_type"
        );
        for (Map<String, Object> row : rows) {
            System.out.println(row);
        }
        System.out.println("=== END DUMP ===");
    }
}
