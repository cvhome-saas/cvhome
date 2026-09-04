package com.asrevo.cvhome.sso.dto;

import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.sso.domain.IdpType;
import com.asrevo.cvhome.sso.idp.IdpPreset;

/** One entry of the type chooser: what the preset fills in and what the administrator still has to supply. */
public record IdpPresetDto(IdpPreset preset, IdpType type, String displayName, boolean generic, boolean needsIssuer,
                           boolean needsEndpoints, List<String> defaultScopes, Map<String, String> defaultMapping,
                           boolean verified) {

    public static IdpPresetDto of(IdpPreset preset) {
        boolean generic = preset.generic();
        return new IdpPresetDto(preset, preset.type(), preset.displayName(), generic,
                preset == IdpPreset.GENERIC_OIDC, preset == IdpPreset.GENERIC_OAUTH2,
                preset.scopes().isBlank() ? List.of() : List.of(preset.scopes().split(" ")), preset.attributeMapping(),
                preset != IdpPreset.APPLE);
    }

}
