package com.pryme.Backend.eligibility;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration,com.pryme.Backend.config.RateLimitConfig"
})
@ActiveProfiles("test")
public class PrintTATA {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void dumpFoir() {
        System.out.println("=== DUMPING LOAN PRODUCTS FOR TATA ===");
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, lender_name, loan_type FROM loan_products WHERE id LIKE 'TATA%'"
        );
        for (Map<String, Object> row : rows) {
            System.out.println(row);
        }
        System.out.println("=== END DUMP ===");
    }
}
