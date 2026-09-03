package com.asrevo.cvhome.cua.web.dto;

/**
 * One brokered provider as the merchant console shows it.
 *
 * <p>
 * {@code appSecret} is always {@code null} on the way out. The stored secret is encrypted at rest, and an API that
 * hands it back defeats that: it puts every merchant's OAuth secret in a browser's network tab and in whatever
 * logs sit between. The field stays in the shape so the console's form binding is unchanged — a blank secret means
 * "leave the stored one alone", which is what {@code PersistableSocialLoginConfig} already assumed.
 * </p>
 */
public record ReadableSocialLoginConfig(String providerId, String name, String appId, String appSecret,
                                        boolean enabled) {

    public static ReadableSocialLoginConfig of(String providerId, String name, String appId, boolean enabled) {
        return new ReadableSocialLoginConfig(providerId, name, appId, null, enabled);
    }

}
