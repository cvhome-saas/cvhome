package com.asrevo.cvhome.uaa.idp;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.uaa.domain.AccountLinking;
import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.dto.IdentityProviderDto;
import com.asrevo.cvhome.uaa.dto.IdentityProviderRequest;
import com.asrevo.cvhome.uaa.support.FakeCrypto;

import static org.assertj.core.api.Assertions.assertThat;

/** Credentials are envelopes in the row and absent from the DTO; blanks take the preset's defaults. */
class IdentityProviderMapperTest {

    private static final String DOMAIN = "example.com";

    private static final String GOOGLE_ALIAS = "google";

    private static final String EMAIL = "email";

    private static final String REDIRECT = "https://uaa/login/oauth2/code/corp";

    private final IdentityProviderMapper mapper = new IdentityProviderMapper(new FakeCrypto((byte) 0x21));

    @Test
    void encryptsBothCredentialsAndNeverEchoesTheSecret() {
        IdentityProvider p = mapper.toNewEntity(IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.CONFIRM, true), 3,
                Instant.EPOCH);

        assertThat(EncryptedValue.isEncrypted(p.getClientIdEnc())).isTrue();
        assertThat(EncryptedValue.isEncrypted(p.getClientSecretEnc())).isTrue();
        assertThat(mapper.clientId(p)).isEqualTo(IdpFixtures.CLIENT_ID);
        assertThat(mapper.clientSecret(p)).isEqualTo(IdpFixtures.SECRET);
        IdentityProviderDto dto = mapper.toDto(p, REDIRECT);
        assertThat(dto.clientId()).isEqualTo(IdpFixtures.CLIENT_ID);
        assertThat(dto.hasClientSecret()).isTrue();
        assertThat(dto.toString()).doesNotContain(IdpFixtures.SECRET);
        assertThat(dto.sortOrder()).isEqualTo(3);
        assertThat(dto.emailDomains()).containsExactly(DOMAIN);
        assertThat(dto.redirectUri()).isEqualTo(REDIRECT);
    }

    @Test
    void blanksTakeThePresetAndABlankSecretKeepsTheStoredOne() {
        IdentityProviderRequest google = new IdentityProviderRequest(GOOGLE_ALIAS, null, IdpPreset.GOOGLE, null, IdpFixtures.CLIENT_ID,
                IdpFixtures.SECRET, null, null, null, null, null, List.of(), null, null, null, null, null, null, null, null);
        IdentityProvider p = mapper.toNewEntity(google, 0, Instant.EPOCH);

        assertThat(p.getDisplayName()).isEqualTo("Google");
        assertThat(p.getAuthorizationUri()).isEqualTo(IdpPreset.GOOGLE.authorizationUri());
        assertThat(IdentityProviderMapper.scopes(p)).containsExactly("openid", "profile", EMAIL);
        assertThat(IdentityProviderMapper.mapping(p)).containsEntry("given_name", "firstName");

        IdentityProviderRequest again = new IdentityProviderRequest(GOOGLE_ALIAS, "Google Workspace", IdpPreset.GOOGLE, true,
                IdpFixtures.CLIENT_ID, "", null, null, null, null, null, List.of(), null, null, List.of("Example.COM"),
                AccountLinking.LINK, true, List.of("USER"), false, Map.of(EMAIL, EMAIL));
        mapper.apply(p, again, Instant.EPOCH);

        assertThat(mapper.clientSecret(p)).isEqualTo(IdpFixtures.SECRET);
        assertThat(p.isHideOnLogin()).isTrue();
        assertThat(p.getEmailDomains()).isEqualTo(DOMAIN);
        assertThat(p.isTrustEmailVerified()).isFalse();
    }

}
