package com.asrevo.cvhome.sso.idp;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.domain.IdpType;
import com.asrevo.cvhome.sso.dto.IdpPresetDto;

import static org.assertj.core.api.Assertions.assertThat;

/** What each preset fixes and what it leaves to the administrator. */
class IdpPresetTest {

    @Test
    void wellKnownPresetsCarryTheirEndpointsAndGenericOnesDoNot() {
        assertThat(IdpPreset.GOOGLE.type()).isEqualTo(IdpType.OIDC);
        assertThat(IdpPreset.GOOGLE.tokenUri()).isNotBlank();
        assertThat(IdpPreset.GITHUB.type()).isEqualTo(IdpType.OAUTH2);
        assertThat(IdpPreset.GITHUB.userInfoUri()).isNotBlank();
        assertThat(IdpPreset.GENERIC_OIDC.generic()).isTrue();
        assertThat(IdpPreset.GENERIC_OIDC.tokenUri()).isNull();
        assertThat(IdpPreset.MICROSOFT.issuerUri()).as("common endpoints: issuer varies per tenant").isNull();
    }

    /** Facebook came from cua, where a merchant configures their own app; shoppers use it, staff do not. */
    @Test
    void facebookIsOauth2AndNamesTheGraphFieldsItNeeds() {
        assertThat(IdpPreset.FACEBOOK.type()).isEqualTo(IdpType.OAUTH2);
        assertThat(IdpPreset.FACEBOOK.issuerUri()).isNull();
        // Unnamed, the Graph API answers with an id and nothing else, and the account has no email to link on.
        assertThat(IdpPreset.FACEBOOK.userInfoUri()).contains("fields=", "email");
    }

    @Test
    void appleIsTheOddOneOutAndSaysSo() {
        assertThat(IdpPreset.APPLE.formPost()).isTrue();
        assertThat(IdpPreset.APPLE.pkce()).isFalse();
        assertThat(IdpPreset.GOOGLE.pkce()).isTrue();
        assertThat(IdpPresetDto.of(IdpPreset.APPLE).verified()).isFalse();
        assertThat(IdpPresetDto.of(IdpPreset.GENERIC_OAUTH2).needsEndpoints()).isTrue();
        assertThat(IdpPreset.catalogue()).hasSize(7);
    }

}
