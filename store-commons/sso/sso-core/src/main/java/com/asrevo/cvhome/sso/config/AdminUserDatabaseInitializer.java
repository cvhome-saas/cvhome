package com.asrevo.cvhome.sso.config;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.s2s.model.AdminUserProperties;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(name = SeedProperties.APPLY_ON_BOOT, havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class AdminUserDatabaseInitializer {

    private static final UUID SUPER_ADMIN_ID = UUID.fromString("65D8419C-8765-4B8B-A15F-910DCE959931");

    private final AdminUserProperties adminUserProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        String password = adminUserProperties.password();
        if (password == null || password.isBlank()) {
            log.debug("Super admin password not provided in properties, skipping initialization");
            return;
        }

        log.info("Checking super admin account for password update");

        Optional<User> existingUser = userRepository.findById(SUPER_ADMIN_ID);
        if (existingUser.isEmpty()) {
            log.warn("Super admin user with ID {} not found in database", SUPER_ADMIN_ID);
            return;
        }

        User user = existingUser.get();
        log.info("Updating super admin password from configuration");
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
