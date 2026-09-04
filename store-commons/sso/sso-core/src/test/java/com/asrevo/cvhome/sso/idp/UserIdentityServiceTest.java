package com.asrevo.cvhome.sso.idp;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRecords;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.domain.UserIdentity;
import com.asrevo.cvhome.sso.dto.UserIdentityDto;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;
import com.asrevo.cvhome.sso.repo.UserIdentityRepository;
import com.asrevo.cvhome.uaa.errors.IdentityNotFoundException;
import com.asrevo.cvhome.uaa.errors.LastCredentialException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * An account's linked external logins.
 *
 * <p>
 * <strong>Unlinking is refused when it would leave the account with no way to sign in.</strong> No password and
 * no other identity means nobody — including the owner — can get back in, and the account would need an
 * administrator to rescue it. That check is the reason this class is not two repository calls.
 * </p>
 *
 * <p>
 * The lookup filters on the owner as well as the id, so an identity id belonging to somebody else reads as "not
 * found" rather than being unlinked from their account.
 * </p>
 */
class UserIdentityServiceTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-0000000000d1");
    private static final UUID OTHER_ACCOUNT = UUID.fromString("00000000-0000-0000-0000-0000000000d2");
    private static final UUID PROVIDER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000e1");
    private static final UUID IDENTITY_ID = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    private static final String ALIAS = "corp";
    private static final String USERNAME = "someone";
    private static final String SUBJECT = "sub-1";
    private static final String HASH = "{bcrypt}$2a$10$abcdefghijklmnopqrstuv";
    private static final String CORP = "Corp";
    private static final String SOMEONE_EXAMPLE_COM = "someone@example.com";

    private final UserIdentityRepository identities = mock(UserIdentityRepository.class);
    private final IdentityProviderRepository providers = mock(IdentityProviderRepository.class);
    private final AuditService audit = mock(AuditService.class);
    private final UserIdentityService service = new UserIdentityService(identities, providers, audit);

    @Test
    void thelistNamesEachIdentitysProviderInTheOrderTheyWereLinked() {
        when(providers.findAll()).thenReturn(List.of(provider()));
        when(identities.findByUserIdOrderByLinkedAtAsc(ACCOUNT_ID)).thenReturn(List.of(identity()));

        List<UserIdentityDto> listed = service.list(ACCOUNT_ID);

        assertThat(listed).singleElement().satisfies(dto -> {
            assertThat(dto.providerAlias()).isEqualTo(ALIAS);
            assertThat(dto.providerName()).isEqualTo(CORP);
            assertThat(dto.subject()).isEqualTo(SUBJECT);
        });
    }

    @Test
    void anIdentityWhoseProviderHasBeenDeletedIsStillListedWithoutOne() {
        when(providers.findAll()).thenReturn(List.of());
        when(identities.findByUserIdOrderByLinkedAtAsc(ACCOUNT_ID)).thenReturn(List.of(identity()));

        // Better a row the owner can unlink than an identity that has become invisible.
        assertThat(service.list(ACCOUNT_ID)).singleElement().satisfies(dto -> {
            assertThat(dto.providerAlias()).isNull();
            assertThat(dto.providerName()).isNull();
        });
    }

    @Test
    void unlinkingRemovesTheIdentityAndAuditsItAgainstTheProvider() throws Exception {
        User user = user(HASH);
        when(identities.findById(IDENTITY_ID)).thenReturn(Optional.of(identity()));
        when(providers.findById(PROVIDER_ID)).thenReturn(Optional.of(provider()));

        service.unlink(user, IDENTITY_ID);

        verify(identities).delete(org.mockito.ArgumentMatchers.any(UserIdentity.class));
        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.IDENTITY_UNLINKED);
        assertThat(AuditRecords.targetTypeOf(record)).isEqualTo(AuditTargetType.IDP);
        assertThat(AuditRecords.targetNameOf(record)).isEqualTo(ALIAS);
        assertThat(AuditRecords.detailOf(record)).isEqualTo("subject sub-1");
    }

    @Test
    void aproviderDeletedSinceTheLinkWasMadeIsAuditedAsUnknownRatherThanFailing() throws Exception {
        when(identities.findById(IDENTITY_ID)).thenReturn(Optional.of(identity()));
        when(providers.findById(PROVIDER_ID)).thenReturn(Optional.empty());

        service.unlink(user(HASH), IDENTITY_ID);

        assertThat(AuditRecords.targetNameOf(recorded())).isEqualTo("?");
    }

    @Test
    void anIdentityBelongingToSomebodyElseReadsAsNotFound() {
        UserIdentity theirs = identity();
        theirs.setUserId(OTHER_ACCOUNT);
        when(identities.findById(IDENTITY_ID)).thenReturn(Optional.of(theirs));

        assertThatThrownBy(() -> service.unlink(user(HASH), IDENTITY_ID))
                .isInstanceOf(IdentityNotFoundException.class);
    }

    @Test
    void anUnknownIdentityIdReadsAsNotFound() {
        when(identities.findById(IDENTITY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.unlink(user(HASH), IDENTITY_ID))
                .isInstanceOf(IdentityNotFoundException.class);
    }

    @Test
    void thelastCredentialCannotBeUnlinked() {
        when(identities.findById(IDENTITY_ID)).thenReturn(Optional.of(identity()));
        when(identities.countByUserId(ACCOUNT_ID)).thenReturn(1L);

        // No password and no other identity leaves nobody — the owner included — able to sign in.
        assertThatThrownBy(() -> service.unlink(user(null), IDENTITY_ID))
                .isInstanceOf(LastCredentialException.class);
        verify(identities, never()).delete(org.mockito.ArgumentMatchers.any(UserIdentity.class));
    }

    @Test
    void anAccountWithNoPasswordMayStillUnlinkOneOfSeveralIdentities() throws Exception {
        when(identities.findById(IDENTITY_ID)).thenReturn(Optional.of(identity()));
        when(identities.countByUserId(ACCOUNT_ID)).thenReturn(2L);
        when(providers.findById(PROVIDER_ID)).thenReturn(Optional.of(provider()));

        service.unlink(user(null), IDENTITY_ID);

        verify(identities).delete(org.mockito.ArgumentMatchers.any(UserIdentity.class));
    }

    private AuditRecord recorded() {
        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).record(captor.capture());
        return captor.getValue();
    }

    private static UserIdentity identity() {
        UserIdentity identity = new UserIdentity();
        identity.setId(IDENTITY_ID);
        identity.setUserId(ACCOUNT_ID);
        identity.setProviderId(PROVIDER_ID);
        identity.setSubject(SUBJECT);
        identity.setEmail(SOMEONE_EXAMPLE_COM);
        identity.setLinkedAt(Instant.EPOCH);
        return identity;
    }

    private static User user(String passwordHash) {
        User user = new User();
        user.setId(ACCOUNT_ID);
        user.setUsername(USERNAME);
        user.setEmail(SOMEONE_EXAMPLE_COM);
        user.setPasswordHash(passwordHash);
        return user;
    }

    private static IdentityProvider provider() {
        IdentityProvider provider = new IdentityProvider();
        provider.setId(PROVIDER_ID);
        provider.setAlias(ALIAS);
        provider.setDisplayName(CORP);
        provider.setPreset(IdpPreset.GENERIC_OIDC);
        provider.setType(IdpPreset.GENERIC_OIDC.type());
        provider.setAccountLinking(AccountLinking.LINK);
        provider.setCreatedAt(Instant.EPOCH);
        provider.setUpdatedAt(Instant.EPOCH);
        return provider;
    }

}
