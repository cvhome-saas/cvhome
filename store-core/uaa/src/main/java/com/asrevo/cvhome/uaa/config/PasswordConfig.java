package com.asrevo.cvhome.uaa.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * bcrypt at strength 12 for every new hash; the delegating encoder still verifies the seeded {@code $2a$10$} ones.
 */
@Configuration
public class PasswordConfig {

    static final int BCRYPT_STRENGTH = 12;

    private static final String BCRYPT = "bcrypt";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new DelegatingPasswordEncoder(BCRYPT, Map.of(BCRYPT, new BCryptPasswordEncoder(BCRYPT_STRENGTH)));
    }

}
