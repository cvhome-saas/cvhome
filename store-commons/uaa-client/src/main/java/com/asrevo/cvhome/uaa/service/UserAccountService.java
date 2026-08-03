package com.asrevo.cvhome.uaa.service;

import java.util.Map;
import java.util.Set;

import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.api.errors.UaaOperationForbiddenException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;

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

}
