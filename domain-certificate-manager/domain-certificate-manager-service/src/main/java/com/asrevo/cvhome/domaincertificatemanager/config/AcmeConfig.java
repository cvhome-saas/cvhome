package com.asrevo.cvhome.domaincertificatemanager.config;

import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyPair;

@Configuration
public class AcmeConfig {

    @Bean
    public Session session() {
        return new Session("acme://letsencrypt.org");
    }

    @Bean
    public KeyPair keyPair(@Value("classpath:/account.key") Resource accountKey) throws IOException {
        try (var in = new InputStreamReader(accountKey.getInputStream())) {
            return KeyPairUtils.readKeyPair(in);
        }
    }

    @Bean
    @Lazy
    public Account account(Session session, KeyPair keyPair) throws AcmeException {
        return new AccountBuilder()
                .onlyExisting()
                .agreeToTermsOfService()
                .useKeyPair(keyPair)
                .create(session);
    }

    @Bean
    @Lazy
    public Login login(Account account, Session session, KeyPair keyPair) {
        return session.login(account.getLocation(), keyPair);
    }
}
