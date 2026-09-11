package com.asrevo.cvhome.sso.config;

import org.springframework.core.env.Environment;

/**
 * The switch behind the boot-time seed writers.
 *
 * <p>
 * {@code AdminUserDatabaseInitializer} and {@code OAuth2ClientDatabaseInitializer} overwrite the super admin's
 * password and every registered client's secret from configuration on <em>every</em> start. That is what a local stack
 * wants — a fresh database and a known login — and what a deployment must never do: a password changed by an
 * operator would silently revert at the next restart. So the writers run only where
 * {@value #APPLY_ON_BOOT} is {@code true}, which the {@code lcl} and {@code test-stores} slices set and nothing else does.
 * </p>
 *
 * <p>
 * Read when the writer runs, not as a bean condition. A native image fixes its beans when it is built, once, for
 * every deployment; a condition on this switch would have frozen whatever the build saw — seeding on every start
 * everywhere, or never, including for the operator who sets it for exactly one start.
 * </p>
 */
public final class SeedProperties {

    public static final String APPLY_ON_BOOT = "com.asrevo.cvhome.uaa.seed.apply-on-boot";

    private SeedProperties() {
    }

    /**
     * Whether this start should write the configured secrets over the seeded ones. Off unless set.
     */
    public static boolean appliesOnBoot(Environment environment) {
        return environment.getProperty(APPLY_ON_BOOT, Boolean.class, false);
    }

}
