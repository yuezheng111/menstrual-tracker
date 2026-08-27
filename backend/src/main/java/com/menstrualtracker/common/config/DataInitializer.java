package com.menstrualtracker.common.config;

import com.menstrualtracker.user.entity.User;
import com.menstrualtracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the initial admin account from ADMIN_USERNAME / ADMIN_PASSWORD
 * environment variables. It never modifies an existing user and only runs
 * when no admin account exists yet.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.username:}")
    private String adminUsername;

    @Value("${admin.bootstrap.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_USERNAME / ADMIN_PASSWORD not configured, skip admin bootstrap");
            return;
        }

        long adminCount = userRepository.countByRole("ADMIN");
        if (adminCount > 0) {
            log.info("Admin account already exists, skip admin bootstrap");
            return;
        }

        String username = adminUsername.trim();
        if (userRepository.existsByUsername(username)) {
            log.warn("Bootstrap username '{}' already exists, skip without modifying the existing user", username);
            return;
        }

        User admin = User.builder()
                .username(username)
                .password(passwordEncoder.encode(adminPassword))
                .role("ADMIN")
                .loginType("PASSWORD")
                .enabled(true)
                .passwordChangeRequired(true)
                .build();
        userRepository.save(admin);
        log.info("Created bootstrap admin account '{}'; password change is required on first login", username);
    }
}
