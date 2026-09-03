package com.asrevo.cvhome.uaa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * uaa: the SSO server deployed as the platform's single-realm identity provider.
 *
 * <p>
 * Almost nothing lives in this module. The server itself — users, roles, audit, keys, identity providers, lockout,
 * sessions — is {@code store-commons/sso/sso-core}, shared with cua, which is the same server deployed with one
 * realm per store. What is uaa's own is the deployment's identity: the issuer pinned to uaa's service host, the
 * seeds, and the embedded admin SPA.
 * </p>
 *
 * <p>
 * The three scans are explicit because the beans, entities and repositories now live under
 * {@code com.asrevo.cvhome.sso} rather than beneath this class. {@code io.namastack.outbox} is named alongside
 * them so that declaring an entity scan — which replaces Boot's default auto-configuration package for entities —
 * cannot quietly unmap the outbox tables this service publishes its events through.
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"com.asrevo.cvhome.uaa", "com.asrevo.cvhome.sso"})
@EntityScan(basePackages = {"com.asrevo.cvhome.sso", "io.namastack.outbox"})
@EnableJpaRepositories(basePackages = {"com.asrevo.cvhome.sso", "io.namastack.outbox"})
public final class UaaApplication {

    private UaaApplication() {

    }

    public static void main(String[] args) {
        SpringApplication.run(UaaApplication.class, args);
    }

}
