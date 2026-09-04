package com.asrevo.cvhome.cua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * cua: the SSO server deployed with one realm per store.
 *
 * <p>
 * The server is {@code store-commons/sso/sso-core}, shared with store-core/uaa, which is the same code with a
 * single realm. What is cua's own is the deployment's identity: the issuer pinned to this pod, how a request maps
 * to the store whose shopper made it, and the hand-off to the storefront that renders the sign-in page — cua
 * itself renders no HTML.
 * </p>
 *
 * <p>
 * The scans name both packages because the beans, entities and repositories live under
 * {@code com.asrevo.cvhome.sso}. {@code io.namastack.outbox} is listed too: declaring an entity scan replaces
 * Boot's default auto-configuration package, which would otherwise unmap the outbox tables.
 * </p>
 */
@SpringBootApplication(scanBasePackages = {"com.asrevo.cvhome.cua", "com.asrevo.cvhome.sso"})
@EntityScan(basePackages = {"com.asrevo.cvhome.cua", "com.asrevo.cvhome.sso", "io.namastack.outbox"})
@EnableJpaRepositories(basePackages = {"com.asrevo.cvhome.cua", "com.asrevo.cvhome.sso", "io.namastack.outbox"})
public final class CuaApplication {

    private CuaApplication() {

    }

    public static void main(String[] args) {
        SpringApplication.run(CuaApplication.class, args);
    }

}
