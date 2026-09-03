package com.asrevo.cvhome.uaa.service;

import java.util.Map;
import java.util.Set;

import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.api.errors.UaaOperationForbiddenException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.domain.user.InviteUserRequest;
import com.asrevo.cvhome.uaa.domain.user.IssuedLink;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserCounts;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.domain.user.UserSearchFilters;

/**
 * Managing uaa users from another service — the contract callers depend on, in the caller's vocabulary.
 *
 * <p>
 * Unlike the payment API, this needs only one interface. The two-interface split in {@code payment-external-api}
 * exists because a single {@code @HttpExchange} interface was implemented by both the server's controller and the
 * generated client proxy, so its {@code throws} clause had to be two vocabularies at once. Nothing on the server
 * implements this one — uaa's endpoints are {@code AdminUserController}'s, with their own signatures — so it simply
 * states what a caller can receive.
 * </p>
 *
 * <p>
 * The types below are narrower than {@code AdminUserClient}'s, which declare {@code UaaApiException} throughout: at
 * the transport any endpoint can answer anything, while here each operation has a meaning. The implementation is what
 * makes the narrowing true, by deciding that any failure it cannot name leaves the outcome undecided.
 * </p>
 *
 * <p>
 * {@link UaaApiUnavailableException} is on every method and never absent: uaa is a network hop, and a caller that
 * cannot express "I never found out" will eventually record a guess as fact.
 * </p>
 */
public interface UserAccountService {

    /**
     * @throws UaaConflictException that username or email already exists in uaa
     */
    ReadableUser createUser(PersistableUser user) throws UaaConflictException, UaaApiUnavailableException;

    /**
     * @throws UaaUserNotFoundException no user exists with that id
     * @throws UaaConflictException     the new username or email collides with another user
     */
    ReadableUser updateUser(PersistableUser user)
            throws UaaUserNotFoundException, UaaConflictException, UaaApiUnavailableException;

    /**
     * @throws UaaUserNotFoundException no user exists with that id
     */
    ReadableUser current(String id) throws UaaUserNotFoundException, UaaApiUnavailableException;

    ReadableUserList list(Map<String, String> filters, Integer pageNumber, Integer pageSize)
            throws UaaApiUnavailableException;

    /**
     * @throws UaaUserNotFoundException      no user exists with that id
     * @throws UaaOperationForbiddenException the target is the super administrator, which cannot be removed
     */
    void deleteUser(String userId)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException;

    /**
     * @throws UaaUserNotFoundException      no user exists with that id
     * @throws UaaOperationForbiddenException the target is the super administrator, which cannot be disabled
     */
    void enableUser(String userId)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException;

    /**
     * @throws UaaUserNotFoundException      no user exists with that id
     * @throws UaaOperationForbiddenException the target is the super administrator, which cannot be disabled
     */
    void disableUser(String userId)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException;

    /**
     * @throws UaaUserNotFoundException no user exists with that id
     */
    ReadableUser findOne(String userId) throws UaaUserNotFoundException, UaaApiUnavailableException;

    /**
     * @throws UaaUserNotFoundException no user exists with that id
     */
    void changePassword(String userId, UserPassword request)
            throws UaaUserNotFoundException, UaaApiUnavailableException;

    Set<String> getAssignableRoles() throws UaaApiUnavailableException;

    // --- lifecycle -----------------------------------------------------------------------------------------------

    /**
     * A page of accounts, narrowed by text, status, role and metadata.
     *
     * <p>
     * The one to reach for when a service needs "the members of this organisation" or "everyone still pending":
     * {@link #list(Map, Integer, Integer)} filters on metadata alone, which cannot express either.
     * </p>
     */
    ReadableUserList search(UserSearchFilters filters, Integer pageNumber, Integer pageSize)
            throws UaaApiUnavailableException;

    /** How many accounts are in each state, counted at the moment of the call. */
    UserCounts counts() throws UaaApiUnavailableException;

    /**
     * Invites someone: creates the account with no password and answers the one-time link, once.
     *
     * <p>
     * uaa stores only the token's hash, so the returned link is the only readable copy that will ever exist — hand
     * it to the person, and never write it to a log. A caller that loses it issues a new invitation.
     * </p>
     *
     * @throws UaaConflictException that username or email already exists in uaa
     */
    IssuedLink invite(InviteUserRequest request) throws UaaConflictException, UaaApiUnavailableException;

    /**
     * Issues a one-time password-reset link for an existing account, answered once and never again.
     *
     * @param revokeSessions end the account's sessions and tokens now rather than when the password changes
     * @throws UaaUserNotFoundException       no user exists with that id
     * @throws UaaOperationForbiddenException the target is the super administrator, whose password no other
     *                                        administrator may reset
     */
    IssuedLink createResetLink(String userId, boolean revokeSessions)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException;

}
