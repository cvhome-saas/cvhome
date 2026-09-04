package com.asrevo.cvhome.sso.idp;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.dto.IdentityProviderDto;
import com.asrevo.cvhome.sso.idp.egress.EgressGuard;
import com.asrevo.cvhome.sso.idp.egress.EgressPolicy;
import com.asrevo.cvhome.sso.realm.RealmMode;
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;
import com.asrevo.cvhome.sso.support.FakeCrypto;
import com.asrevo.cvhome.uaa.errors.IdpAliasTakenException;
import com.asrevo.cvhome.uaa.errors.IdpNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Creating, changing and ordering identity providers.
 *
 * <p>
 * The alias is the provider's public name — it appears in the authorization URL a shopper is sent to — so it is
 * normalised to lower case on the way in and checked for collisions. On update the collision check is skipped when
 * the alias has not changed, which is what stops a provider colliding with itself and refusing every save that
 * touched anything else.
 * </p>
 *
 * <p>
 * Every write evicts the cached {@code ClientRegistration}, and an alias change evicts <em>both</em> names: the old
 * one so the previous registration cannot answer, and the new one in case something had already cached a miss.
 * Leaving either behind sends a shopper to a provider configured the way it used to be.
 * </p>
 */
class IdentityProviderCrudTest {

    private static final Instant NOW = Instant.parse("2026-09-04T10:00:00Z");
    private static final String ALIAS = "corp";
    private static final String OTHER_ALIAS = "partner";

    private final FakeCrypto crypto = new FakeCrypto((byte) 0x09);

    private final IdentityProviderRepository providers = mock(IdentityProviderRepository.class);
    private final IdentityProviderMapper mapper = new IdentityProviderMapper(crypto);
    private final DynamicClientRegistrationRepository registrations =
            mock(DynamicClientRegistrationRepository.class);
    private final AuditService audit = mock(AuditService.class);

    private final IdentityProviderService service = new IdentityProviderService(providers, mapper,
            new ClientRegistrationFactory(mapper), registrations, audit,
            Clock.fixed(NOW, ZoneOffset.UTC), egress(), "https://uaa.example/", RestClient.builder());

    private static EgressGuard egress() {
        SsoRealmProperties realm = new SsoRealmProperties();
        realm.setMode(RealmMode.SINGLE);
        return new EgressGuard(new EgressPolicy(Set.of("http", "https"), true, null, 0, 0),
                new SsoTenantIdentifierResolver(realm));
    }

    private IdentityProvider existing(String alias, boolean enabled, boolean hideOnLogin, int sortOrder) {
        IdentityProvider provider = IdpFixtures.provider(AccountLinking.LINK, true, true);
        // The fixture stores a placeholder envelope; toDto decrypts, so it has to be a real one for this crypto.
        provider.setClientIdEnc(crypto.encrypt("client-1".getBytes(StandardCharsets.UTF_8)).serialize());
        provider.setAlias(alias);
        provider.setEnabled(enabled);
        provider.setHideOnLogin(hideOnLogin);
        provider.setSortOrder(sortOrder);
        return provider;
    }

    @BeforeEach
    void setUp() {
        when(providers.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void anAliasIsLowerCasedAndTrimmedBeforeItIsCheckedOrStored() throws Exception {
        when(providers.existsByAlias(ALIAS)).thenReturn(false);

        service.create(IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.LINK, true));

        // It appears in the authorization URL a shopper is sent to, so it cannot vary by how it was typed.
        verify(providers).existsByAlias(ALIAS);
    }

    @Test
    void anAliasAnotherProviderAlreadyHoldsIsAConflict() {
        when(providers.existsByAlias(ALIAS)).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.LINK, true)))
                .isInstanceOf(IdpAliasTakenException.class);
        verify(providers, never()).save(any());
    }

    @Test
    void aNewProviderIsAppendedAtTheEndOfTheExistingOrder() throws Exception {
        when(providers.existsByAlias(ALIAS)).thenReturn(false);
        when(providers.count()).thenReturn(3L);

        service.create(IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.LINK, true));

        ArgumentCaptor<IdentityProvider> saved = ArgumentCaptor.forClass(IdentityProvider.class);
        verify(providers).save(saved.capture());
        assertThat(saved.getValue().getSortOrder()).isEqualTo(3);
    }

    @Test
    void keepingTheSameAliasOnAnUpdateIsNotACollisionWithItself() throws Exception {
        IdentityProvider provider = existing(ALIAS, true, false, 0);
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));

        service.update(provider.getId(), IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.LINK, true));

        verify(providers, never()).existsByAlias(any());
    }

    @Test
    void renamingOntoAnotherProvidersAliasIsRefused() {
        IdentityProvider provider = existing(OTHER_ALIAS, true, false, 0);
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(providers.existsByAlias(ALIAS)).thenReturn(true);

        assertThatThrownBy(() -> service.update(provider.getId(),
                IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.LINK, true)))
                .isInstanceOf(IdpAliasTakenException.class);
    }

    @Test
    void anAliasChangeEvictsBothTheOldAndTheNewRegistration() throws Exception {
        IdentityProvider provider = existing(OTHER_ALIAS, true, false, 0);
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));
        when(providers.existsByAlias(ALIAS)).thenReturn(false);

        service.update(provider.getId(), IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.LINK, true));

        // The old one so the previous registration cannot answer; the new one in case a miss was cached.
        verify(registrations).evict(OTHER_ALIAS);
        verify(registrations).evict(ALIAS);
    }

    @Test
    void deletingAProviderEvictsItsRegistrationAndRecordsWhatWasThere() throws Exception {
        IdentityProvider provider = existing(ALIAS, true, false, 0);
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));

        service.delete(provider.getId());

        verify(providers).delete(provider);
        verify(registrations).evict(ALIAS);
        verify(audit).record(any());
    }

    @Test
    void enablingAndDisablingBothEvictAndStampTheClock() throws Exception {
        IdentityProvider provider = existing(ALIAS, false, false, 0);
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));

        IdentityProviderDto after = service.setEnabled(provider.getId(), true);

        assertThat(after.enabled()).isTrue();
        assertThat(provider.getUpdatedAt()).isEqualTo(NOW);
        verify(registrations).evict(ALIAS);
    }

    @Test
    void anUnknownProviderIsATypedNotFoundOnEveryPathThatResolvesOne() {
        UUID missing = UUID.randomUUID();
        when(providers.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(missing)).isInstanceOf(IdpNotFoundException.class);
        assertThatThrownBy(() -> service.delete(missing)).isInstanceOf(IdpNotFoundException.class);
        assertThatThrownBy(() -> service.setEnabled(missing, true)).isInstanceOf(IdpNotFoundException.class);
        assertThatThrownBy(() -> service.update(missing,
                IdpFixtures.request(IdpPreset.GENERIC_OIDC, AccountLinking.LINK, true)))
                .isInstanceOf(IdpNotFoundException.class);
    }

    @Test
    void theLoginPageShowsOnlyEnabledProvidersThatAreNotHidden() {
        when(providers.findByEnabledTrueOrderBySortOrderAscDisplayNameAsc())
                .thenReturn(List.of(existing(ALIAS, true, false, 0), existing(OTHER_ALIAS, true, true, 1)));

        // "Hidden" is for a provider reached only by email discovery or a direct link, not shown as a button.
        assertThat(service.visibleForLogin()).extracting(it -> it.alias()).containsExactly(ALIAS);
    }

    @Test
    void reorderingPutsTheNamedAliasesFirstAndAppendsWhateverWasNotNamed() {
        IdentityProvider first = existing(ALIAS, true, false, 0);
        IdentityProvider second = existing(OTHER_ALIAS, true, false, 1);
        when(providers.findAllByOrderBySortOrderAscDisplayNameAsc()).thenReturn(List.of(first, second));

        service.reorder(List.of(OTHER_ALIAS));

        // An alias the caller forgot keeps a stable position rather than colliding on 0 with everything else.
        assertThat(second.getSortOrder()).isZero();
        assertThat(first.getSortOrder()).isOne();
        verify(providers).saveAll(any());
    }

    @Test
    void reorderingIgnoresAnAliasThatMatchesNoProvider() {
        IdentityProvider only = existing(ALIAS, true, false, 5);
        when(providers.findAllByOrderBySortOrderAscDisplayNameAsc()).thenReturn(List.of(only));

        service.reorder(List.of("nope", ALIAS));

        assertThat(only.getSortOrder()).isZero();
    }

    @Test
    void theLookupsByAliasAndIdAreThinPassThroughs() {
        IdentityProvider provider = existing(ALIAS, true, false, 0);
        when(providers.findByAlias(ALIAS)).thenReturn(Optional.of(provider));
        when(providers.findById(provider.getId())).thenReturn(Optional.of(provider));

        assertThat(service.byAlias(ALIAS)).contains(provider);
        assertThat(service.byId(provider.getId())).contains(provider);
    }

    @Test
    void theListingKeepsTheRepositorysOrder() {
        when(providers.findAllByOrderBySortOrderAscDisplayNameAsc())
                .thenReturn(List.of(existing(OTHER_ALIAS, true, false, 0), existing(ALIAS, true, false, 1)));

        assertThat(service.list()).extracting(it -> it.alias()).containsExactly(OTHER_ALIAS, ALIAS);
    }

    @Test
    void theRedirectUriIsBuiltFromTheIssuerWithoutDoublingItsSlash() {
        assertThat(service.redirectUri(ALIAS)).doesNotContain("//login").contains(ALIAS);
    }
}
