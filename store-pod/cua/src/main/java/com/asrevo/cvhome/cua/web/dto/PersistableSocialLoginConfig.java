package com.asrevo.cvhome.cua.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * One brokered provider as the merchant console saves it.
 *
 * <p>
 * A blank {@code appSecret} means "keep the one already stored", which is what lets the console save a form it was
 * never given the secret for.
 * </p>
 */
public record PersistableSocialLoginConfig(@NotBlank String providerId, @NotBlank String appId, String appSecret,
                                           boolean enabled) {
}
