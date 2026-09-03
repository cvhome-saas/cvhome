package com.asrevo.cvhome.sso.idp;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.domain.UserIdentity;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.sso.repo.UserIdentityRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Every branch of the broker: known, link, confirm, reject, provision, unknown, and the account's own checks. */
class IdentityBrokerServiceTest {

    private static final String ADA = "Ada";

    private static final String LOVELACE = "Lovelace";

    private static final String USER_ROLE = "USER";

    private static final Instant NOW = Instant.parse("2026-09-03T00:00:00Z");

    private static final String SUB = "sub-1";

    private static final String MAIL = "ada@example.com";

    private final UserRepository users = mock(UserRepository.class);

    private final UserIdentityRepository identities = mock(UserIdentityRepository.class);

    private final RoleRepository roles = mock(RoleRepository.class);

    private final IdentityBrokerService broker = new IdentityBrokerService(users, identities, roles, mock(AuditService.class),
            Clock.fixed(NOW, ZoneOffset.UTC));

    private final User ada = User.create(MAIL, MAIL, ADA, null);

    private static BrokeredIdentity identity(boolean verified) {
        return new BrokeredIdentity(SUB, MAIL, verified, ADA, LOVELACE, Map.of());
    }

    @BeforeEach
    void wire() {
        Role user = new Role();
        user.setName(USER_ROLE);
        when(roles.findByName(USER_ROLE)).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(i -> i.getArgument(0));
        when(identities.findByProviderIdAndSubject(any(), anyString())).thenReturn(Optional.empty());
        when(users.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void aKnownIdentitySignsInAndGetsTheDefaultRoles() throws BrokerRefusedException {
        IdentityProvider p = IdpFixtures.provider(AccountLinking.REJECT, false, true);
        UserIdentity link = UserIdentity.link(ada.getId(), p.getId(), SUB, MAIL, NOW.minusSeconds(60));
        when(identities.findByProviderIdAndSubject(p.getId(), SUB)).thenReturn(Optional.of(link));
        when(users.findById(ada.getId())).thenReturn(Optional.of(ada));

        BrokerOutcome outcome = broker.resolve(p, identity(true));

        assertThat(outcome.needsConfirmation()).isFalse();
        assertThat(outcome.user().getUsername()).isEqualTo(MAIL);
        assertThat(outcome.user().getRoles()).extracting(Role::getName).contains(USER_ROLE);
        assertThat(outcome.user().getLastName()).as("empty names filled from the provider").isEqualTo(LOVELACE);
        assertThat(link.getLastLoginAt()).isEqualTo(NOW);
    }

    @Test
    void linkPolicyLinksSilentlyOnlyWhenTheProviderVouches() throws BrokerRefusedException {
        IdentityProvider p = IdpFixtures.provider(AccountLinking.LINK, false, true);
        when(users.findByEmailIgnoreCase(MAIL)).thenReturn(Optional.of(ada));

        assertThat(broker.resolve(p, identity(true)).needsConfirmation()).isFalse();
        verify(identities).save(any(UserIdentity.class));
        assertThat(ada.isEmailVerified()).isTrue();

        BrokerOutcome unverified = broker.resolve(p, identity(false));
        assertThat(unverified.needsConfirmation()).as("falls back to CONFIRM").isTrue();
        assertThat(unverified.pending().username()).isEqualTo(MAIL);
    }

    @Test
    void confirmAsksAndRejectRefuses() throws BrokerRefusedException {
        when(users.findByEmailIgnoreCase(MAIL)).thenReturn(Optional.of(ada));

        BrokerOutcome confirm = broker.resolve(IdpFixtures.provider(AccountLinking.CONFIRM, false, true), identity(true));
        assertThat(confirm.needsConfirmation()).isTrue();
        assertThat(confirm.pending().providerAlias()).isEqualTo(IdpFixtures.ALIAS);
        verify(identities, never()).save(any());

        assertThatThrownBy(() -> broker.resolve(IdpFixtures.provider(AccountLinking.REJECT, false, true), identity(true)))
                .isInstanceOf(BrokerRefusedException.class)
                .extracting(e -> ((BrokerRefusedException) e).code()).isEqualTo(BrokerRefusedException.REJECTED);
    }

    @Test
    void jitProvisionsOrRefusesAnUnknownUser() throws BrokerRefusedException {
        BrokerOutcome created = broker.resolve(IdpFixtures.provider(AccountLinking.CONFIRM, true, true), identity(true));
        assertThat(created.user().getUsername()).isEqualTo(MAIL);
        assertThat(created.user().getActivatedAt()).isEqualTo(NOW);
        assertThat(created.user().isEmailVerified()).isTrue();
        assertThat(created.user().getRoles()).extracting(Role::getName).contains(USER_ROLE);

        assertThatThrownBy(() -> broker.resolve(IdpFixtures.provider(AccountLinking.CONFIRM, false, true), identity(true)))
                .extracting(e -> ((BrokerRefusedException) e).code()).isEqualTo(BrokerRefusedException.UNKNOWN_USER);
    }

    @Test
    void aDisabledOrLockedAccountIsRefusedWhicheverWayItArrives() {
        ada.setEnabled(false);
        when(users.findByEmailIgnoreCase(MAIL)).thenReturn(Optional.of(ada));

        assertThatThrownBy(() -> broker.resolve(IdpFixtures.provider(AccountLinking.LINK, false, true), identity(true)))
                .extracting(e -> ((BrokerRefusedException) e).code()).isEqualTo(BrokerRefusedException.DISABLED);

        ada.setEnabled(true);
        ada.setLockedPermanently(true);
        PendingLink pending = new PendingLink(UUID.randomUUID(), IdpFixtures.ALIAS, "Corp", SUB, MAIL, ada.getId(), MAIL);
        when(users.findById(ada.getId())).thenReturn(Optional.of(ada));
        assertThatThrownBy(() -> broker.completeLink(pending, IdpFixtures.provider(AccountLinking.CONFIRM, false, true)))
                .extracting(e -> ((BrokerRefusedException) e).code()).isEqualTo(BrokerRefusedException.LOCKED);
    }

}
