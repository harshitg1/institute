package com.institute.Institue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Disable Flyway during tests to avoid unsupported DB issues in the test environment
@SpringBootTest(properties = {"spring.flyway.enabled=false"})
class InstituteApplicationTests {

    @Test
    void contextLoads() {
    }

}
