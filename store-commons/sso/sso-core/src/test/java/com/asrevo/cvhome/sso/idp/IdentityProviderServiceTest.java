package com.asrevo.cvhome.sso.idp;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.dto.PublicIdpDto;
import com.asrevo.cvhome.sso.idp.egress.EgressGuard;
import com.asrevo.cvhome.sso.idp.egress.EgressPolicy;
import com.asrevo.cvhome.sso.realm.RealmMode;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;
import com.asrevo.cvhome.sso.support.FakeCrypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Home-realm discovery picks the longest matching domain rule, hidden providers included. */
class IdentityProviderServiceTest {

    private static final String BROAD = "broad";

    private static final String NARROW = "narrow";

    private final IdentityProviderRepository providers = mock(IdentityProviderRepository.class);

    private final IdentityProviderMapper mapper = new IdentityProviderMapper(new FakeCrypto((byte) 0x09));

    private final IdentityProviderService service = new IdentityProviderService(providers, mapper,
            new ClientRegistrationFactory(mapper), mock(DynamicClientRegistrationRepository.class), mock(AuditService.class),
            Clock.systemUTC(), egress(), "https://uaa.example/", RestClient.builder());

    /** Permissive on purpose: what the guard refuses is EgressGuardTest's subject, not this file's. */
    private static EgressGuard egress() {
        SsoRealmProperties realm = new SsoRealmProperties();
        realm.setMode(RealmMode.SINGLE);
        return new EgressGuard(new EgressPolicy(Set.of("http", "https"), true, null, 0, 0),
                new SsoTenantIdentifierResolver(realm));
    }

    @Test
    void discoversByLongestSuffixAndIgnoresNonMatches() {
        IdentityProvider broad = IdpFixtures.provider(AccountLinking.CONFIRM, false, true);
        broad.setAlias(BROAD);
        broad.setEmailDomains("example.com");
        IdentityProvider narrow = IdpFixtures.provider(AccountLinking.CONFIRM, false, true);
        narrow.setAlias(NARROW);
        narrow.setEmailDomains("eng.example.com");
        narrow.setHideOnLogin(true);
        when(providers.findByEnabledTrueOrderBySortOrderAscDisplayNameAsc()).thenReturn(List.of(broad, narrow));

        Optional<PublicIdpDto> eng = service.discoverByEmail("Grace@Eng.Example.com");
        Optional<PublicIdpDto> plain = service.discoverByEmail("ada@example.com");

        assertThat(eng).map(PublicIdpDto::alias).contains(NARROW);
        assertThat(plain).map(PublicIdpDto::alias).contains(BROAD);
        assertThat(service.discoverByEmail("x@other.org")).isEmpty();
        assertThat(service.discoverByEmail("not-an-email")).isEmpty();
        assertThat(service.visibleForLogin()).extracting(PublicIdpDto::alias).containsExactly(BROAD);
        assertThat(service.redirectUri(BROAD)).isEqualTo("https://uaa.example/login/oauth2/code/broad");
    }

}
