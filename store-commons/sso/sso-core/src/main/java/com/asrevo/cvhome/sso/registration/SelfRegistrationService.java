package com.asrevo.cvhome.sso.registration;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.password.PasswordService;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.uaa.errors.EmailTakenException;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.errors.SelfRegistrationDisabledException;
import com.asrevo.cvhome.uaa.errors.UsernameTakenException;

import lombok.RequiredArgsConstructor;

/**
 * Someone creating their own account, where the realm allows it.
 *
 * <p>
 * Shoppers are the reason this exists — a storefront's register page is a normal part of buying something — and
 * the platform realm keeps it off, which is why the {@code self_registration_enabled} setting shipped with no
 * endpoint behind it. Whether it is allowed is the realm's answer, not the deployment's, so a merchant can close
 * registration on their store without affecting any other.
 * </p>
 *
 * <p>
 * The password goes through the same {@link PasswordService} as every other way a password is set here, so a
 * shopper gets the realm's policy, its history check and its breach check rather than a bare hash. That is a real
 * change from what shoppers had before, where registration encoded the password directly.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SelfRegistrationService {

    private final UserRepository users;

    private final PasswordService passwords;

    private final SettingsService settings;

    private final AuditService audit;

    @Transactional
    public void register(RegistrationRequest request)
            throws SelfRegistrationDisabledException, UsernameTakenException, EmailTakenException,
            PasswordPolicyViolationException, PasswordReusedException, PasswordCompromisedException {
        if (!settings.current().selfRegistrationEnabled()) {
            throw SelfRegistrationDisabledException.create();
        }
        // Both checks are realm-scoped by Hibernate's tenant filter: the same address is a different person in
        // every store, which is the whole reason shoppers cannot share one pool.
        if (users.existsByUsernameIgnoreCase(request.username())) {
            throw UsernameTakenException.of(request.username());
        }
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw EmailTakenException.of(request.email());
        }

        User user = User.create(request.username(), request.email(), request.firstName(), request.lastName());
        user.setEnabled(true);
        // The same funnel every other password goes through: the realm's policy, its history and its breach check.
        passwords.setPassword(user, request.password());
        users.save(user);

        audit.record(AuditRecord.of(AuditEventType.USER_CREATED).user(user.getId(), user.getUsername()));
    }

}
