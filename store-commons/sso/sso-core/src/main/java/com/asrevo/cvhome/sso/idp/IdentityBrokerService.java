package com.asrevo.cvhome.sso.idp;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.audit.AuditActor;
import com.asrevo.cvhome.sso.audit.AuditActorType;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdentityProvider;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.domain.UserIdentity;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.sso.repo.UserIdentityRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Turns what a provider said into a local account — or into a refusal the sign-in page can explain.
 *
 * <ol>
 * <li>A known identity (provider + subject) signs in as its account, after the account's own checks.</li>
 * <li>Otherwise a local account with the same email is linked per the provider's {@code accountLinking}: silently when
 * the provider vouches for the email and the policy is {@code LINK}; after a password confirmation when it is
 * {@code CONFIRM} (or {@code LINK} without a vouched email); never when it is {@code REJECT}.</li>
 * <li>Otherwise, with {@code jitProvisioning}, the account is created — username is the email, so the JWT
 * {@code sub} stays stable and readable — or the login is refused as an unknown user.</li>
 * </ol>
 *
 * <p>
 * The default roles are granted at every login, idempotently, so a role added to the provider reaches everyone who
 * signs in through it. Locked and disabled accounts are refused here as well as by the password path.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityBrokerService {

    private final UserRepository users;

    private final UserIdentityRepository identities;

    private final RoleRepository roles;

    private final AuditService audit;

    private final Clock clock;

    @Transactional
    public BrokerOutcome resolve(IdentityProvider provider, BrokeredIdentity identity) throws BrokerRefusedException {
        if (identity.subject() == null || identity.subject().isBlank()) {
            throw new BrokerRefusedException(BrokerRefusedException.NO_EMAIL, "The provider sent no stable subject.");
        }
        Instant now = clock.instant();
        Optional<UserIdentity> known = identities.findByProviderIdAndSubject(provider.getId(), identity.subject());
        if (known.isPresent()) {
            User user = users.findById(known.get().getUserId()).orElseThrow(() ->
                    new BrokerRefusedException(BrokerRefusedException.UNKNOWN_USER, "The linked account no longer exists."));
            refuseIfUnusable(user, now);
            known.get().setLastLoginAt(now);
            identities.save(known.get());
            grantDefaultRoles(user, provider);
            fillNames(user, identity);
            return BrokerOutcome.signedIn(users.save(user));
        }
        String email = identity.email() == null ? null : identity.email().trim().toLowerCase(Locale.ROOT);
        Optional<User> byEmail = email == null ? Optional.empty() : users.findByEmailIgnoreCase(email);
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            refuseIfUnusable(user, now);
            return linkExisting(provider, identity, user, now);
        }
        if (!provider.isJitProvisioning()) {
            throw new BrokerRefusedException(BrokerRefusedException.UNKNOWN_USER,
                    "No account matches this login and the provider does not create accounts.");
        }
        if (email == null) {
            throw new BrokerRefusedException(BrokerRefusedException.NO_EMAIL, "The provider sent no email address.");
        }
        return BrokerOutcome.signedIn(provision(provider, identity, email, now));
    }

    private BrokerOutcome linkExisting(IdentityProvider provider, BrokeredIdentity identity, User user, Instant now)
            throws BrokerRefusedException {
        AccountLinking policy = provider.getAccountLinking();
        if (policy == AccountLinking.REJECT) {
            throw new BrokerRefusedException(BrokerRefusedException.REJECTED,
                    "An account with this email exists and this provider may not link to it.");
        }
        boolean vouched = provider.isTrustEmailVerified() && identity.emailVerified();
        if (policy == AccountLinking.LINK && vouched) {
            link(user, provider, identity, now);
            grantDefaultRoles(user, provider);
            return BrokerOutcome.signedIn(users.save(user));
        }
        return BrokerOutcome.confirm(new PendingLink(provider.getId(), provider.getAlias(), provider.getDisplayName(),
                identity.subject(), identity.email(), user.getId(), user.getUsername()));
    }

    /** Completes a {@link BrokerOutcome#confirm confirmed} link: the password has been checked by the caller. */
    @Transactional
    public User completeLink(PendingLink pending, IdentityProvider provider) throws BrokerRefusedException {
        Instant now = clock.instant();
        User user = users.findById(pending.userId()).orElseThrow(() ->
                new BrokerRefusedException(BrokerRefusedException.UNKNOWN_USER, "The account no longer exists."));
        refuseIfUnusable(user, now);
        BrokeredIdentity identity = new BrokeredIdentity(pending.subject(), pending.email(), true, null, null, null);
        link(user, provider, identity, now);
        grantDefaultRoles(user, provider);
        return users.save(user);
    }

    private void link(User user, IdentityProvider provider, BrokeredIdentity identity, Instant now) {
        identities.save(UserIdentity.link(user.getId(), provider.getId(), identity.subject(), identity.email(), now));
        if (identity.emailVerified() && provider.isTrustEmailVerified()) {
            user.setEmailVerified(true);
        }
        audit.record(AuditRecord.of(AuditEventType.IDENTITY_LINKED)
                .actor(new AuditActor(AuditActorType.USER, user.getId().toString(), user.getUsername()))
                .user(user.getId(), user.getUsername())
                .target(AuditTargetType.IDP, provider.getId().toString(), provider.getAlias())
                .detail(String.format("subject %s", identity.subject())));
    }

    private User provision(IdentityProvider provider, BrokeredIdentity identity, String email, Instant now) {
        User user = User.create(email, email, identity.firstName(), identity.lastName());
        user.setEmailVerified(provider.isTrustEmailVerified() && identity.emailVerified());
        user.setActivatedAt(now);
        grantDefaultRoles(user, provider);
        User saved = users.save(user);
        audit.record(AuditRecord.of(AuditEventType.USER_CREATED).actor(AuditActor.SYSTEM)
                .user(saved.getId(), saved.getUsername()).target(AuditTargetType.USER, saved.getId().toString(), email)
                .detail(String.format("provisioned by provider %s", provider.getAlias())));
        link(saved, provider, identity, now);
        return users.save(saved);
    }

    private void grantDefaultRoles(User user, IdentityProvider provider) {
        List<String> names = IdentityProviderMapper.split(provider.getDefaultRoles());
        if (names.isEmpty()) {
            return;
        }
        Set<Role> granted = new HashSet<>(user.getRoles());
        for (String name : names) {
            roles.findByName(name).ifPresentOrElse(granted::add,
                    () -> log.warn("Provider {} grants role {}, which does not exist", provider.getAlias(), name));
        }
        user.setRoles(granted);
    }

    private static void fillNames(User user, BrokeredIdentity identity) {
        if (user.getFirstName() == null && identity.firstName() != null) {
            user.setFirstName(identity.firstName());
        }
        if (user.getLastName() == null && identity.lastName() != null) {
            user.setLastName(identity.lastName());
        }
    }

    private static void refuseIfUnusable(User user, Instant now) throws BrokerRefusedException {
        if (!user.isEnabled()) {
            throw new BrokerRefusedException(BrokerRefusedException.DISABLED, "This account is disabled.");
        }
        if (user.isLocked(now)) {
            throw new BrokerRefusedException(BrokerRefusedException.LOCKED, "This account is locked.");
        }
    }

}
