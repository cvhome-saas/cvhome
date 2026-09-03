package com.asrevo.cvhome.sso.dto;

import java.time.Instant;
import java.util.UUID;

/** A linked external identity, as an account's owner or an administrator sees it. */
public record UserIdentityDto(UUID id, String providerAlias, String providerName, String subject, String email,
                              Instant linkedAt, Instant lastLoginAt) {
}
