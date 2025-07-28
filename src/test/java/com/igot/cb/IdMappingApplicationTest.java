package com.igot.cb;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = IdMappingApplication.class,
        // Disable web server so the test will start and then shut down immediately:
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class IdMappingApplicationTest {

    @Test
    void contextLoads() {
        // Verify application context loads without errors
    }

    @Test
    void mainShouldNotThrow() {
        // Tell Spring Boot not to start any web server
        String[] args = { "--spring.main.web-application-type=none" };

        // simply invoke main and assert that no exception bubbles up
        assertDoesNotThrow(() -> IdMappingApplication.main(args));
    }
}