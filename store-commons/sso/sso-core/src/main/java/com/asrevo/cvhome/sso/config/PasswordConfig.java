package com.asrevo.cvhome.sso.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.asrevo.cvhome.sso.client.ClientSecretEncoder;

/**
 * Two hashes for two kinds of secret.
 *
 * <p>
 * A person's password: bcrypt at strength 12 for every new hash; the delegating encoder still verifies the seeded
 * {@code $2a$10$} ones. A client secret: {@link ClientSecretEncoder}, a salted SHA-256, because it is 256 random bits
 * nobody can guess and bcrypt only cost every token request 227 ms of CPU. The client-secret encoder is its own type,
 * so a by-type {@link PasswordEncoder} still means the password one.
 * </p>
 */
@Configuration
public class PasswordConfig {

    static final int BCRYPT_STRENGTH = 12;

    private static final String BCRYPT = "bcrypt";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new DelegatingPasswordEncoder(BCRYPT, Map.of(BCRYPT, new BCryptPasswordEncoder(BCRYPT_STRENGTH)));
    }

    @Bean
    public ClientSecretEncoder clientSecretEncoder() {
        return new ClientSecretEncoder();
    }

}
