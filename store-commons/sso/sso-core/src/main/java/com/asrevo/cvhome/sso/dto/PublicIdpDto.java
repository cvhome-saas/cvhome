package com.asrevo.cvhome.sso.dto;

import com.asrevo.cvhome.sso.domain.IdpType;
import com.asrevo.cvhome.sso.idp.IdpPreset;

/** A provider as the sign-in page draws it: a button and where it goes. Nothing an anonymous visitor should not see. */
public record PublicIdpDto(String alias, String displayName, IdpPreset preset, IdpType type, String authorizationUrl) {
}
