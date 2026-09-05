package com.asrevo.cvhome.uaa.domain.user;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The user DTOs the admin SDK sends and receives.
 *
 * <p>
 * Mostly Lombok, so most of what looks like code here is generated and excluded from coverage. What is asserted is
 * the part that is hand-written and easy to get wrong: the collection fields default to empty rather than null, so a
 * caller that never sets roles serializes {@code []} instead of {@code null}, and {@link UserSearchFilters}'s two
 * named constructors mean the same thing as passing nulls by hand.
 * </p>
 */
class UserDtosTest {

    private static final String EMAIL = "someone@example.com";
    private static final String ADMIN = "ROLE_STORE_ADMIN";
    private static final String SECRET = "secret";
    private static final String USER_ID = "u1";
    private static final String USERNAME = "someone";
    private static final String NEW_PASSWORD = "new";
    private static final String TIER = "tier";
    private static final String GOLD = "gold";

    @Test
    void aReadableUserStartsWithAnEmptyRoleSetRatherThanNull() {
        ReadableUser user = new ReadableUser();
        assertThat(user.getRoles()).isNotNull().isEmpty();
        user.setRoles(Set.of(ADMIN));
        user.setEmailAddress(EMAIL);
        assertThat(user.getRoles()).containsExactly(ADMIN);
        assertThat(user.getEmailAddress()).isEqualTo(EMAIL);
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void aPersistableUserStartsWithAnEmptyRoleSetToo() {
        PersistableUser user = new PersistableUser();
        assertThat(user.getRoles()).isNotNull().isEmpty();
        assertThat(user.isActive()).isFalse();
        user.setPassword(SECRET);
        user.setRepeatPassword(SECRET);
        assertThat(user.getPassword()).isEqualTo(user.getRepeatPassword());
    }

    @Test
    void aUserEntityCarriesTheIdentityFieldsBothDtosShare() {
        UserEntity entity = new UserEntity();
        entity.setId(USER_ID);
        entity.setUserName(USERNAME);
        assertThat(entity.getId()).isEqualTo(USER_ID);
        assertThat(entity.getUserName()).isEqualTo(USERNAME);
        assertThat(entity.getDefaultLanguage()).isNull();
    }

    @Test
    void aUserPasswordHasBothANoArgsAndAnAllArgsShape() {
        assertThat(new UserPassword().getPassword()).isNull();
        assertThat(new UserPassword("old", NEW_PASSWORD).getChangePassword()).isEqualTo(NEW_PASSWORD);
    }

    @Test
    void aReadableUserListIsAPagedListOfUsers() {
        ReadableUserList list = new ReadableUserList();
        list.setContent(List.of(new ReadableUser()));
        list.setTotalElements(1L);
        assertThat(list.getContent()).hasSize(1);
        assertThat(list.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void searchFiltersHaveTwoNamedShapesThatMeanTheSameAsPassingNulls() {
        assertThat(UserSearchFilters.none())
                .isEqualTo(new UserSearchFilters(null, null, null, Map.of()));
        assertThat(UserSearchFilters.ofMetadata(Map.of(TIER, GOLD)).metadata()).containsEntry(TIER, GOLD);
        assertThat(UserSearchFilters.ofMetadata(Map.of()).q()).isNull();
    }

    @Test
    void theRemainingRecordsCarryWhatTheyAreGiven() {
        InviteUserRequest invite =
                new InviteUserRequest(EMAIL, USERNAME, "Some", "One", List.of(ADMIN), Map.of("via", "console"));
        assertThat(invite.email()).isEqualTo(EMAIL);
        assertThat(invite.roles()).containsExactly(ADMIN);

        Instant expiry = Instant.parse("2026-01-01T00:00:00Z");
        assertThat(new IssuedLink(new ReadableUser(), "https://uaa/confirm", expiry).expiresAt()).isEqualTo(expiry);
        assertThat(new UserCounts(5L, 3L, 1L, 1L, 0L).total()).isEqualTo(5L);
    }
}
