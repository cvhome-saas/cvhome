package com.asrevo.cvhome.sso.config;

import java.util.Optional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.s2s.model.TestStoreProperties;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("test-stores")
public class TestUserDatabaseInitializer {

    private final TestStoreProperties testStoreProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        if (testStoreProperties.users() == null || testStoreProperties.users().isEmpty()) {
            log.info("No test users found in properties to sync");
            return;
        }

        log.info("Syncing {} test users from properties to database", testStoreProperties.users().size());

        for (TestStoreProperties.TestUser testUser : testStoreProperties.users()) {
            Optional<User> existingUser = userRepository.findById(testUser.id());
            if (existingUser.isEmpty()) {
                return;
            }
            User user = existingUser.get();

            if (testUser.password() != null && !testUser.password().isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(testUser.password()));
            }

            userRepository.save(user);
        }
    }
}
