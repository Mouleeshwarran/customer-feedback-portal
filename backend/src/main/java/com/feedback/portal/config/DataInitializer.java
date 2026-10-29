
package com.feedback.portal.config;

import com.feedback.portal.entity.User;
import com.feedback.portal.entity.UserRole;
import com.feedback.portal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createDemoUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            createUser(userRepository, passwordEncoder,
                    "customer@example.com",
                    "CustomerDemo123!", UserRole.CUSTOMER);

            createUser(userRepository, passwordEncoder,
                    "moderator@example.com",
                    "ModeratorDemo123!", UserRole.MODERATOR);

            createUser(userRepository, passwordEncoder,
                    "admin@example.com",
                    "AdminDemo123!", UserRole.ADMINISTRATOR);
        };
    }

    private void createUser(
            UserRepository repository,
            PasswordEncoder encoder,
            String email,
            String password,
            UserRole role) {

        User user = repository.findByEmailIgnoreCase(email)
                .orElseGet(User::new);

        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);
        user.setActive(true);

        repository.save(user);
    }
}
