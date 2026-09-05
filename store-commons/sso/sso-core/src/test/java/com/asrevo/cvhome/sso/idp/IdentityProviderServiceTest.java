package com.asrevo.cvhome.sso.idp;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.dto.IdpTestResult;
import com.asrevo.cvhome.sso.dto.PublicIdpDto;
import com.asrevo.cvhome.sso.idp.egress.EgressGuard;
import com.asrevo.cvhome.sso.idp.egress.EgressPolicy;
import com.asrevo.cvhome.sso.realm.RealmMode;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;
import com.asrevo.cvhome.sso.support.FakeCrypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Home-realm discovery picks the longest matching domain rule, hidden providers included. */
class IdentityProviderServiceTest {

    private static final String BROAD = "broad";

    private static final String NARROW = "narrow";

    private static final String ISSUER_BASE = "http://localhost:9/";

    private static final String DISCOVERY_URL = "http://localhost:9/.well-known/openid-configuration";

    private static final String AUTHORIZE_URL = "http://localhost:9/authorize";
    private static final String S_ = "{}";

    private final IdentityProviderRepository providers = mock(IdentityProviderRepository.class);

    private final FakeCrypto crypto = new FakeCrypto((byte) 0x09);

    private final IdentityProviderMapper mapper = new IdentityProviderMapper(crypto);

    private final RestClient.Builder httpBuilder = RestClient.builder();

    private final MockRestServiceServer upstream = MockRestServiceServer.bindTo(httpBuilder).build();

    private final IdentityProviderService service = new IdentityProviderService(providers, mapper,
            new ClientRegistrationFactory(mapper), mock(DynamicClientRegistrationRepository.class), mock(AuditService.class),
            Clock.systemUTC(), egress(), "https://uaa.example/", httpBuilder);

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

    @Test
    void aproviderIsReadBackByItsIdAndAnUnknownIdIsNotFound() throws Exception {
        IdentityProvider provider = stored();

        assertThat(service.get(provider.getId()).alias()).isEqualTo(provider.getAlias());
        assertThatThrownBy(() -> service.get(UUID.randomUUID()))
                .isInstanceOf(com.asrevo.cvhome.uaa.errors.IdpNotFoundException.class);
    }

    @Test
    void enableAndDisableEachRecordWhichTheyWere() throws Exception {
        IdentityProvider provider = stored();

        assertThat(service.setEnabled(provider.getId(), false).enabled()).isFalse();
        assertThat(service.setEnabled(provider.getId(), true).enabled()).isTrue();
    }

    @Test
    void testingAnOidcProviderAsksItsDiscoveryDocumentAndReportsTheIssuerItClaims() throws Exception {
        IdentityProvider provider = stored();

        upstream.expect(requestTo(DISCOVERY_URL))
                .andRespond(withSuccess("{\"issuer\":\"http://localhost:9\"}", MediaType.APPLICATION_JSON));

        IdpTestResult result = service.test(provider.getId());

        assertThat(result.ok()).isTrue();
        assertThat(result.checked()).isEqualTo(DISCOVERY_URL);
        assertThat(result.discoveredIssuer()).isEqualTo("http://localhost:9");
    }

    @Test
    void adiscoveryDocumentWithNoIssuerClaimIsStillReachable() throws Exception {
        IdentityProvider provider = stored();

        upstream.expect(requestTo(DISCOVERY_URL)).andRespond(withSuccess(S_, MediaType.APPLICATION_JSON));

        assertThat(service.test(provider.getId()).discoveredIssuer()).isNull();
    }

    @Test
    void aproviderThatAnswersWithAnErrorStatusFailsWithThatStatus() {
        IdentityProvider provider = stored();

        upstream.expect(requestTo(DISCOVERY_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> service.test(provider.getId()))
                .isInstanceOf(com.asrevo.cvhome.uaa.errors.IdpDiscoveryFailedException.class);
    }

    @Test
    void anOauth2ProviderIsTestedAgainstItsAuthorizationEndpointRatherThanAdiscoveryDocument() throws Exception {
        IdentityProvider provider = stored();
        provider.setType(com.asrevo.cvhome.sso.domain.IdpType.OAUTH2);
        provider.setAuthorizationUri(AUTHORIZE_URL);

        upstream.expect(requestTo(AUTHORIZE_URL)).andRespond(withSuccess(S_, MediaType.APPLICATION_JSON));

        assertThat(service.test(provider.getId()).checked()).isEqualTo(AUTHORIZE_URL);
    }

    @Test
    void aproviderWithNeitherAnIssuerNorAnAuthorizationEndpointHasNothingToTest() {
        IdentityProvider provider = stored();
        provider.setIssuerUri(null);
        provider.setAuthorizationUri(null);

        assertThatThrownBy(() -> service.test(provider.getId()))
                .isInstanceOf(com.asrevo.cvhome.uaa.errors.IdpConfigInvalidException.class);
    }

    private IdentityProvider stored() {
        IdentityProvider provider = IdpFixtures.provider(AccountLinking.CONFIRM, false, true);
        // IdpFixtures parks a placeholder in clientIdEnc; anything that maps the provider has to decrypt it.
        provider.setClientIdEnc(crypto.encrypt("client-1".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .serialize());
        provider.setIssuerUri(ISSUER_BASE);
        provider.setAuthorizationUri(AUTHORIZE_URL);
        provider.setTokenUri("http://localhost:9/token");
        provider.setJwkSetUri("http://localhost:9/jwks");
        provider.setUserInfoUri("http://localhost:9/userinfo");
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(providers.save(provider)).thenReturn(provider);
        return provider;
    }

}
