package com.pryme.Backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
