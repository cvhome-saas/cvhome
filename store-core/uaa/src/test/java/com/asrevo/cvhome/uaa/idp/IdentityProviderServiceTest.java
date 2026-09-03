package com.asrevo.cvhome.uaa.idp;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.domain.AccountLinking;
import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.dto.PublicIdpDto;
import com.asrevo.cvhome.uaa.repo.IdentityProviderRepository;
import com.asrevo.cvhome.uaa.support.FakeCrypto;

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
            Clock.systemUTC(), "https://uaa.example/", RestClient.builder());

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
