package com.asrevo.cvhome.sso.repo;

import java.time.Instant;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.domain.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The account list's filters, as JPA specifications.
 *
 * <p>
 * {@code hasStatus} restates {@link User#status(Instant)} in SQL, so the shape of each branch is what keeps the
 * two agreeing. The one with teeth is the lock test: {@code locked_until} is nullable, and a bare
 * {@code locked_until > now} inside a {@code NOT (...)} evaluates to unknown for a null and drops the row instead
 * of keeping it — so every account that had never been locked would vanish from the ACTIVE filter.
 * </p>
 *
 * <p>
 * The free-text search is trimmed and lower-cased before it reaches SQL and covers the full name as typed, so
 * "Store2 Moderator" finds a row whose first and last names are stored separately.
 * </p>
 */
class UserSpecificationsTest {

    private static final Instant NOW = Instant.parse("2026-07-01T00:00:00Z");
    private static final String ACTIVATEDAT = "activatedAt";

    private CriteriaBuilder builder;
    private Root<User> root;
    private CriteriaQuery<?> query;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        builder = mock(CriteriaBuilder.class, Mockito.RETURNS_DEEP_STUBS);
        root = mock(Root.class, Mockito.RETURNS_DEEP_STUBS);
        query = mock(CriteriaQuery.class, Mockito.RETURNS_DEEP_STUBS);
        when(root.get(Mockito.anyString())).thenReturn(mock(Path.class));
    }

    @Test
    void ametadataFilterAsksPostgresForThatOneJsonKey() {
        UserSpecifications.hasMetadataField("org", "org-1").toPredicate(root, query, builder);

        verify(builder).function(Mockito.eq("jsonb_extract_path_text"), Mockito.eq(String.class), any(), any());
        verify(root).get("metadata");
    }

    @Test
    void afreeTextSearchIsTrimmedLowerCasedAndWrappedBeforeItReachesSql() {
        UserSpecifications.matches("  Ada  ").toPredicate(root, query, builder);

        // Three columns — username, email, and the concatenated full name — one like each.
        verify(builder, times(3)).like(any(), Mockito.eq("%ada%"));
    }

    @Test
    void thefullNameIsSearchedAsTypedEvenThoughItIsStoredInTwoColumns() {
        UserSpecifications.matches("Store2 Moderator").toPredicate(root, query, builder);

        verify(root).get("firstName");
        verify(root).get("lastName");
        // Null-coalesced on both halves, or a row with no last name would never match its first.
        verify(builder, times(2)).coalesce(any(jakarta.persistence.criteria.Expression.class), Mockito.eq(""));
    }

    @Test
    void arolefilterJoinsAndDeduplicatesSoAnAccountIsNotListedOncePerMatchingRow() {
        UserSpecifications.hasRole("SUPER_ADMIN").toPredicate(root, query, builder);

        verify(root).join("roles");
        verify(query).distinct(true);
    }

    @ParameterizedTest(name = "{0} is expressible in SQL")
    @EnumSource(UserStatus.class)
    void everyStatusHasAbranchSoTheListCannotSilentlyMatchNothing(UserStatus status) {
        Predicate predicate = UserSpecifications.hasStatus(status, NOW).toPredicate(root, query, builder);

        assertThat(predicate).isNotNull();
    }

    @Test
    void disabledIsTheOnlyStatusThatDoesNotAlsoRequireTheAccountToBeEnabled() {
        UserSpecifications.hasStatus(UserStatus.DISABLED, NOW).toPredicate(root, query, builder);

        verify(builder).isFalse(any());
        verify(builder, Mockito.never()).isTrue(any());
    }

    @Test
    void thelockTestIsNullSafeSoAnAccountNeverLockedIsNotDroppedFromActive() {
        UserSpecifications.hasStatus(UserStatus.ACTIVE, NOW).toPredicate(root, query, builder);

        // Without the isNotNull, `NOT (locked_until > now)` is unknown for a null and drops the row.
        verify(builder).isNotNull(any());
        verify(root, Mockito.atLeastOnce()).get("lockedUntil");
        verify(root, Mockito.atLeastOnce()).get("lockedPermanently");
    }

    @Test
    void pendingMeansNeitherActivatedNorGivenApassword() {
        UserSpecifications.hasStatus(UserStatus.PENDING, NOW).toPredicate(root, query, builder);

        verify(root).get(ACTIVATEDAT);
        verify(root).get("passwordHash");
    }

    @Test
    void lockedDoesNotLookAtWhetherTheAccountHasEverSignedIn() {
        UserSpecifications.hasStatus(UserStatus.LOCKED, NOW).toPredicate(root, query, builder);

        verify(root, Mockito.never()).get(ACTIVATEDAT);
    }

}
