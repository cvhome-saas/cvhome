package com.asrevo.cvhome.uaa.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.asrevo.cvhome.uaa.api.errors.UaaApiException;
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
import com.asrevo.cvhome.uaa.sdk.AdminUserClient;
import com.asrevo.cvhome.uaa.sdk.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.InvitationResponse;
import com.asrevo.cvhome.uaa.sdk.dto.PageRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageResponse;
import com.asrevo.cvhome.uaa.sdk.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UserDto;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import lombok.AllArgsConstructor;

/**
 * Adapts {@link AdminUserClient} to the caller-facing contract, which is also where the SDK's transport-level
 * vocabulary narrows to a per-operation one.
 *
 * <p>
 * {@code AdminUserClient} declares {@code UaaApiException} throughout, because at the transport any uaa endpoint can
 * answer anything. Each method here names what its operation can actually mean and folds everything else into
 * {@link UaaApiUnavailableException} — the judgement the compiler cannot make, made once, in the one place that knows
 * what the call was for.
 * </p>
 */
@AllArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    public static final String ORG_KEY = "org";

    public static final String STORE_KEY = "store";

    private AdminUserClient client;

    private static Map<String, String> extractMetadata(PersistableUser user) {
        HashMap<String, String> m = new HashMap<>();
        if (Objects.nonNull(user.getOrg())) {
            m.put(ORG_KEY, user.getOrg());
        }
        if (Objects.nonNull(user.getStore())) {
            m.put(STORE_KEY, user.getStore());
        }
        return m;
    }

    private static ReadableUser toReadableUser(UserDto u) {
        ReadableUser readableUser = new ReadableUser();
        readableUser.setId(u.id().toString());
        readableUser.setEmailAddress(u.email());
        readableUser.setUserName(u.username());
        readableUser.setFirstName(u.firstName());
        readableUser.setLastName(u.lastName());
        readableUser.setOrg((String) u.metadata().getOrDefault(ORG_KEY, null));
        readableUser.setStore((String) u.metadata().getOrDefault(STORE_KEY, null));
        readableUser.setActive(u.enabled());
        readableUser.setStatus(u.status());
        readableUser.setEmailVerified(u.emailVerified());
        readableUser.setLastSignInAt(u.lastSignInAt());
        readableUser.setRoles(u.roles());
        return readableUser;
    }

    private static ReadableUserList toReadableList(PageResponse<UserDto> response) {
        ReadableUserList list = new ReadableUserList();
        list.setTotalElements(response.totalElements());
        list.setTotalPages(response.totalPages());
        list.setSize(response.size());
        list.setPageNumber(response.number());
        list.setContent(response.content().stream().map(UserAccountServiceImpl::toReadableUser).toList());
        return list;
    }

    /**
     * Anything this contract does not name leaves the outcome undecided, which is the only thing a caller can safely
     * act on: a code the SDK could not name might have been a refusal or might have been a hiccup, and pretending to
     * know which is how a caller records a guess as a fact. uaa's own code and status ride along on the wrapper.
     */
    private static UaaApiUnavailableException undecided(UaaApiException cause) {
        return cause instanceof UaaApiUnavailableException unavailable ? unavailable
                : UaaApiUnavailableException.wrapping(cause);
    }

    /**
     * The three mutations that share a failure shape: uaa refuses because the user is missing, refuses because it is
     * the super administrator, or leaves the outcome unknown.
     */
    private static void mutate(Mutation mutation)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException {
        try {
            mutation.run();
        } catch (UaaUserNotFoundException | UaaOperationForbiddenException e) {
            throw e;
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    @Override
    public ReadableUser createUser(PersistableUser user) throws UaaConflictException, UaaApiUnavailableException {
        try {
            var createdUser = client.createUser(CreateUserRequest.builder()
                    .email(user.getEmailAddress())
                    .username(user.getUserName())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(user.getRoles())
                    .metadata(extractMetadata(user))
                    .build());
            client.resetPassword(createdUser.id().toString(), user.getPassword());
            return toReadableUser(createdUser);
        } catch (UaaConflictException e) {
            throw e;
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    @Override
    public ReadableUser updateUser(PersistableUser user)
            throws UaaUserNotFoundException, UaaConflictException, UaaApiUnavailableException {
        try {
            var updatedUser = client.updateUser(user.getId(),
                    UpdateUserRequest.builder()
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .enabled(user.isActive())
                            .roles(user.getRoles())
                            .metadata(extractMetadata(user))
                            .build());

            return toReadableUser(updatedUser);
        } catch (UaaUserNotFoundException | UaaConflictException e) {
            throw e;
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    @Override
    public ReadableUser current(String id) throws UaaUserNotFoundException, UaaApiUnavailableException {
        return findOne(id);
    }

    @Override
    public ReadableUserList list(Map<String, String> filters, Integer pageNumber, Integer pageSize)
            throws UaaApiUnavailableException {
        PageResponse<UserDto> response;
        try {
            response = client.listUsers(filters, new PageRequest(pageNumber, pageSize));
        } catch (UaaApiException e) {
            // A listing names no failure of its own: either uaa answered or the caller found nothing out.
            throw undecided(e);
        }
        return toReadableList(response);
    }

    @Override
    public ReadableUserList search(UserSearchFilters filters, Integer pageNumber, Integer pageSize)
            throws UaaApiUnavailableException {
        try {
            return toReadableList(client.searchUsers(toSdk(filters), new PageRequest(pageNumber, pageSize)));
        } catch (UaaApiException e) {
            // A search names no failure of its own: either uaa answered or the caller found nothing out.
            throw undecided(e);
        }
    }

    @Override
    public UserCounts counts() throws UaaApiUnavailableException {
        try {
            var counts = client.counts();
            return new UserCounts(counts.total(), counts.active(), counts.pending(), counts.locked(), counts.disabled());
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    @Override
    public IssuedLink invite(InviteUserRequest request) throws UaaConflictException, UaaApiUnavailableException {
        try {
            InvitationResponse response = client.inviteUser(new com.asrevo.cvhome.uaa.sdk.dto.InviteUserRequest(
                    request.email(), request.username(), request.firstName(), request.lastName(), request.roles(),
                    request.metadata()));
            return new IssuedLink(toReadableUser(response.user()), response.link(), response.expiresAt());
        } catch (UaaConflictException e) {
            throw e;
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    @Override
    public IssuedLink createResetLink(String userId, boolean revokeSessions)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException {
        try {
            InvitationResponse response = client.createResetLink(userId, revokeSessions);
            return new IssuedLink(toReadableUser(response.user()), response.link(), response.expiresAt());
        } catch (UaaUserNotFoundException | UaaOperationForbiddenException e) {
            throw e;
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    private static com.asrevo.cvhome.uaa.sdk.dto.UserSearchFilters toSdk(UserSearchFilters filters) {
        UserSearchFilters held = filters == null ? UserSearchFilters.none() : filters;
        return new com.asrevo.cvhome.uaa.sdk.dto.UserSearchFilters(held.q(), held.status(), held.role(),
                held.metadata());
    }

    @Override
    public void deleteUser(String userId)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException {
        mutate(() -> client.deleteUser(userId));
    }

    @Override
    public void enableUser(String userId)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException {
        mutate(() -> client.enableUser(userId));
    }

    @Override
    public void disableUser(String userId)
            throws UaaUserNotFoundException, UaaOperationForbiddenException, UaaApiUnavailableException {
        mutate(() -> client.disableUser(userId));
    }

    @Override
    public ReadableUser findOne(String userId) throws UaaUserNotFoundException, UaaApiUnavailableException {
        try {
            return toReadableUser(client.getUser(userId));
        } catch (UaaUserNotFoundException e) {
            throw e;
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    @Override
    public void changePassword(String userId, UserPassword request)
            throws UaaUserNotFoundException, UaaApiUnavailableException {
        try {
            client.resetPassword(userId, request.getChangePassword());
        } catch (UaaUserNotFoundException e) {
            throw e;
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    @Override
    public Set<String> getAssignableRoles() throws UaaApiUnavailableException {
        Set<String> reservedRoles = Set.of("USER", "ORG_ADMIN");
        try {
            return client.getAssignableRoles()
                    .stream()
                    .filter(it -> !reservedRoles.contains(it))
                    .collect(Collectors.toSet());
        } catch (UaaApiException e) {
            throw undecided(e);
        }
    }

    /**
     * A uaa call with no return value, so {@link #mutate(Mutation)} can serve the three that share a failure shape.
     */
    @FunctionalInterface
    private interface Mutation {

        void run() throws UaaApiException;

    }

}
