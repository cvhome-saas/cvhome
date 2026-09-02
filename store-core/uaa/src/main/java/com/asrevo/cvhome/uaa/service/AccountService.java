package com.asrevo.cvhome.uaa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.errors.CurrentPasswordMismatchException;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.password.PasswordService;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import com.asrevo.cvhome.uaa.session.SessionAdminService;
import com.asrevo.cvhome.uaa.token.TokenRevocationService;

import lombok.RequiredArgsConstructor;

/** Self-service: what an account does to itself. */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository users;

    private final PasswordService passwords;

    private final SessionAdminService sessions;

    private final TokenRevocationService tokens;

    private final AuditService audit;

    /**
     * The current password proves it is the person and not a hijacked session; the new one goes through the policy;
     * every other session and every token ends, because a password change is what a person does after a scare.
     */
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword, String keepSessionId)
            throws CurrentPasswordMismatchException, PasswordPolicyViolationException, PasswordReusedException,
            PasswordCompromisedException {
        if (!passwords.matches(user, currentPassword)) {
            throw CurrentPasswordMismatchException.of();
        }
        passwords.setPassword(user, newPassword);
        users.save(user);
        sessions.revokeAll(user.getUsername(), keepSessionId);
        tokens.revokeAllForUser(user.getUsername());
        audit.record(AuditRecord.of(AuditEventType.USER_PASSWORD_CHANGED).user(user.getId(), user.getUsername()));
    }

}
