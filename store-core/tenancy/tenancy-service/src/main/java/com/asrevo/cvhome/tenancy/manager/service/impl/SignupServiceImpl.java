package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.errors.DuplicateSignupEmailException;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;
import com.asrevo.cvhome.tenancy.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.tenancy.manager.dto.SignUpUser;
import com.asrevo.cvhome.tenancy.manager.service.InternalOrgService;
import com.asrevo.cvhome.tenancy.manager.service.SignupService;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class SignupServiceImpl implements SignupService {

    /** The role an organization's first administrator gets, and the only role signup may confer. */
    private static final String OWNER_ROLE = "ORG_ADMIN";

    /**
     * The one failure this service cannot repair. Worth the constant: it is the line an operator has to act on.
     */
    private static final String ORPHAN_LEFT_BEHIND = """
            Signup for org {} rolled back but uaa user {} could not be deleted \
            — that address cannot be signed up again until it is removed by hand""";

    private final UserAccountService userAccountService;

    private final InternalOrgService internalOrgService;

    /**
     * Creates the organization and its first administrator, or neither.
     *
     * <p>
     * The transaction is the first half of the fix. The organization row was committed by {@code createOrgForUser}
     * before uaa was asked for anything, so a duplicate email — the single most likely way for a signup to fail —
     * left an organization with no user, no way in, and nothing to notice it. Nothing retried, because nothing had
     * failed as far as this service was concerned.
     * </p>
     *
     * <p>
     * {@link #compensateOnRollback} is the second half. Holding both in one transaction rolls the organization back
     * when uaa refuses, but the remaining window is the other way round: uaa accepts the user and the local commit
     * then fails, leaving an orphaned <em>user</em> — and the address it holds is the one thing that stops the
     * visitor simply trying again, because uaa's unique constraint will now refuse them by name. An orphan is only
     * recoverable by hand if nobody deletes it, so this deletes it.
     * </p>
     *
     * <p>
     * <strong>The owner is recorded here, and this is the only place it is ever written on a live signup.</strong>
     * {@code manager_org.owner_user_id} and {@code ManagerOrgDto.ownerUserId} both predate this method by a release
     * and neither had a writer, so the column was null for every organization on the platform — which is why
     * {@code OrgManagerApi.changePassword} had no id to resolve and passed the organization's own. uaa answers
     * {@code createUser} with the created account, so the id costs no extra call; it is written inside the same
     * transaction, so an organization that commits always knows who owns it.
     * </p>
     */
    @Override
    @Transactional
    public ReadableUser createOrgUser(CreateOrgRequest request)
            throws DuplicateSignupEmailException, UaaApiUnavailableException {
        SignUpUser signUp = request.user().normalized();
        ManagerOrgId org =
                internalOrgService.createOrgForUser(new Email(signUp.emailAddress()), signUp.organizationNameOrDefault());

        ReadableUser created = createAdministrator(signUp, org);
        compensateOnRollback(created, org);
        recordOwner(org, created);
        return created;
    }

    /**
     * Asks uaa for the account, with only the fields a signup is allowed to decide.
     *
     * <p>
     * Every field on the {@code PersistableUser} handed to uaa is set here, from a {@link SignUpUser} that cannot
     * express any of the privileged ones. That is the difference between overwriting a stranger's input and never
     * accepting it: the previous version overwrote {@code roles}, {@code org} and {@code active} but not
     * {@code store}, so a public request could put someone else's store id into the new account's uaa metadata,
     * which is exactly what the store-scoped permission checks read.
     * </p>
     *
     * <p>
     * The username is the address, which is what the login form asks for, and it is already lowercased by
     * {@code normalized()} — uaa's unique constraint is case-sensitive, so without that
     * {@code Ada@example.com} and {@code ada@example.com} are two accounts everybody involved believes to be one.
     * </p>
     */
    private ReadableUser createAdministrator(SignUpUser signUp, ManagerOrgId org)
            throws DuplicateSignupEmailException, UaaApiUnavailableException {
        PersistableUser user = new PersistableUser();
        user.setFirstName(signUp.firstName());
        user.setLastName(signUp.lastName());
        user.setEmailAddress(signUp.emailAddress());
        user.setUserName(signUp.emailAddress());
        user.setPassword(signUp.password());
        user.setActive(true);
        user.setOrg(org.id().toString());
        user.setRoles(Set.of(OWNER_ROLE));

        try {
            return userAccountService.createUser(user);
        } catch (UaaConflictException e) {
            // Logged rather than chained: the shared advice walks the cause chain, and a chained
            // RemoteServiceException would render with uaa's generic conflict code instead of this one.
            log.info("Signup refused for {}: uaa already has that account", signUp.emailAddress(), e);
            throw DuplicateSignupEmailException.of(signUp.emailAddress());
        }
    }

    /**
     * Deletes the uaa account if this transaction does not commit.
     *
     * <p>
     * uaa is not in the transaction — it is a network hop to a service with its own database — so the window
     * between it accepting the user and this transaction committing is a real one, and everything in it (the owner
     * write, the commit itself, a connection lost at exactly the wrong moment) can still fail. What is left behind
     * is an account whose address the visitor cannot reuse: they retry, uaa's unique constraint refuses, and the
     * console tells them they already have an account they have never been able to log into.
     * </p>
     *
     * <p>
     * Registered as a synchronization rather than done in a {@code catch}, because the commit is the part most
     * likely to fail and no {@code catch} inside this method ever sees it. A failure to compensate is logged and
     * swallowed for the same reason: {@code afterCompletion} runs after the caller's fate is already decided, so
     * throwing here would replace a legible failure with an obscure one and still leave the orphan.
     * </p>
     */
    private void compensateOnRollback(ReadableUser created, ManagerOrgId org) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    return;
                }
                try {
                    userAccountService.deleteUser(created.getId());
                    log.warn("Signup for org {} rolled back; uaa user {} deleted", org, created.getId());
                } catch (Exception e) {
                    log.error(ORPHAN_LEFT_BEHIND, org, created.getId(), e);
                }
            }
        });
    }

    /**
     * Writes the new administrator's uaa id onto the organization.
     *
     * <p>
     * {@link OrgNotFoundException} cannot happen — the row was created two statements ago in this same transaction
     * — so it is caught rather than added to the signature, which would push an impossible case out to every
     * caller. If it ever does happen the signup has a much larger problem than the owner column, and the log line
     * is what says so.
     * </p>
     */
    private void recordOwner(ManagerOrgId org, ReadableUser created) {
        try {
            internalOrgService.recordOwner(org, created.getId());
        } catch (OrgNotFoundException e) {
            log.error("Org {} vanished between creation and owner assignment", org, e);
        }
    }

}
