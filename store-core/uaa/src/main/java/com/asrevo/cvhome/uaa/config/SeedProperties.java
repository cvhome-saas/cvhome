package com.asrevo.cvhome.uaa.config;

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
 */
public final class SeedProperties {

    public static final String APPLY_ON_BOOT = "com.asrevo.cvhome.uaa.seed.apply-on-boot";

    private SeedProperties() {
    }

}
