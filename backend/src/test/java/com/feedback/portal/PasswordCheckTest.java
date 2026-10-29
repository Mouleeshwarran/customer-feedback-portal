package com.feedback.portal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class PasswordCheckTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void checkDemoPassword() {
        String storedHash =
            "$2a$12$yg5qN6fZyHOvYa8xEKMlmeLcXZyOCxn6.SAk6qJcmSFrgvFofa3VS";

        boolean matches = passwordEncoder.matches(
            "CustomerDemo123!",
            storedHash
        );

        System.out.println("DEMO PASSWORD MATCHES: " + matches);
    }
}