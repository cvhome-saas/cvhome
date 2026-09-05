package com.asrevo.cvhome.commons.domain;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Role parsing and the identity predicates every {@code @PreAuthorize} check ultimately runs through.
 *
 * <p>
 * {@link Roles#parse(String)} and {@link Groups#parse(String)} answer {@code null} rather than throwing, because they
 * read authorities minted by another deployment: an unknown role has to degrade to "no such role" instead of failing
 * the whole request. {@link UserOrgStoreIdentity#isAnyStoreAdmin()} is the one worth reading twice — it is a union of
 * three roles, and widening it by accident hands a retail account the admin console.
 * </p>
 */
class RolesAndIdentityTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String IMAGE_URL = "https://cdn.example.com/a.png";

    private static UserOrgStoreIdentity identityWith(Roles... roles) {
        return new UserOrgStoreIdentity(ManagerOrgId.newId(), STORE, Set.of(roles));
    }

    @ParameterizedTest
    @EnumSource(Roles.class)
    void everyRoleParsesBackFromItsName(Roles role) {
        assertThat(Roles.parse(role.name())).isEqualTo(role);
    }

    @ParameterizedTest
    @EnumSource(Groups.class)
    void everyGroupParsesBackFromItsName(Groups group) {
        assertThat(Groups.parse(group.name())).isEqualTo(group);
    }

    @Test
    void anUnknownRoleOrGroupIsNullRatherThanAThrow() {
        assertThat(Roles.parse("ROLE_WHATEVER")).isNull();
        assertThat(Roles.parse("")).isNull();
        assertThat(Groups.parse("WHATEVER")).isNull();
        assertThat(Groups.parse(null)).isNull();
    }

    @Test
    void eachRolePredicateAnswersForItsOwnRoleOnly() {
        assertThat(identityWith(Roles.ROLE_SUPER_ADMIN).isSuperAdmin()).isTrue();
        assertThat(identityWith(Roles.ROLE_ORG_ADMIN).isOrgAdmin()).isTrue();
        assertThat(identityWith(Roles.ROLE_STORE_ADMIN).isStoreAdmin()).isTrue();
        assertThat(identityWith(Roles.ROLE_STORE_MODERATOR).isStoreModerator()).isTrue();
        assertThat(identityWith(Roles.ROLE_STORE_RETAIL).isStoreRetail()).isTrue();
        assertThat(identityWith(Roles.ROLE_CUSTOMER).isCustomer()).isTrue();
        assertThat(identityWith(Roles.ROLE_CUSTOMER).isSuperAdmin()).isFalse();
        assertThat(identityWith(Roles.ROLE_STORE_RETAIL).isStoreAdmin()).isFalse();
    }

    @Test
    void anyStoreAdminIsAdminModeratorOrRetailAndNothingElse() {
        assertThat(identityWith(Roles.ROLE_STORE_ADMIN).isAnyStoreAdmin()).isTrue();
        assertThat(identityWith(Roles.ROLE_STORE_MODERATOR).isAnyStoreAdmin()).isTrue();
        assertThat(identityWith(Roles.ROLE_STORE_RETAIL).isAnyStoreAdmin()).isTrue();
        assertThat(identityWith(Roles.ROLE_CUSTOMER).isAnyStoreAdmin()).isFalse();
        assertThat(identityWith(Roles.ROLE_ORG_ADMIN).isAnyStoreAdmin()).isFalse();
        assertThat(identityWith(Roles.ROLE_SUPER_ADMIN).isAnyStoreAdmin()).isFalse();
    }

    @Test
    void orgAdminOrAnyStoreAdminAddsTheOrgAdminButStillRefusesACustomer() {
        assertThat(identityWith(Roles.ROLE_ORG_ADMIN).isOrgAdminOrAnyStoreAdmin()).isTrue();
        assertThat(identityWith(Roles.ROLE_STORE_MODERATOR).isOrgAdminOrAnyStoreAdmin()).isTrue();
        assertThat(identityWith(Roles.ROLE_CUSTOMER).isOrgAdminOrAnyStoreAdmin()).isFalse();
    }

    @Test
    void anIdentityCarriesItsOrgAndStore() {
        UserOrgStoreIdentity identity = identityWith(Roles.ROLE_STORE_ADMIN);
        assertThat(identity.store()).isEqualTo(STORE);
        assertThat(identity.org()).isNotNull();
        assertThat(identity.hasRole(Roles.ROLE_STORE_ADMIN)).isTrue();
        assertThat(identity.hasRole(Roles.ROLE_ORG_ADMIN)).isFalse();
    }

    @Test
    void onlyTheThemesMarkedImplementedAreOffered() {
        List<Theme> implemented = Theme.getImplementedThemes();
        assertThat(implemented).contains(Theme.BASIC, Theme.BEAUTY, Theme.FASHION, Theme.GROCERY)
                .doesNotContain(Theme.DEFAULT, Theme.COSMETICS, Theme.WATCHES)
                .hasSizeLessThan(Theme.values().length);
    }

    @Test
    void theEnumsThatAreJustCataloguesStillHaveTheirConstants() {
        assertThat(ColorTheme.valueOf("DARK")).isEqualTo(ColorTheme.DARK);
        assertThat(ColorTheme.values()).contains(ColorTheme.DEFAULT, ColorTheme.LIGHT);
        assertThat(StorageProviderType.values())
                .containsExactly(StorageProviderType.S3, StorageProviderType.MINIO);
    }

    @Test
    void theIdBearingBaseEntityCarriesItsIdBothWays() {
        assertThat(new Entity(7L).getId()).isEqualTo(7L);
        Entity blank = new Entity();
        assertThat(blank.getId()).isNull();
        blank.setId(9L);
        assertThat(blank.getId()).isEqualTo(9L);
    }

    @Test
    void theStatisticHoldersCarryWhatTheyAreGiven() {
        ZonedDateTime from = ZonedDateTime.parse("2026-01-01T00:00:00Z");
        ZonedDateTime to = from.plusDays(1);
        assertThat(new StatisticRange(from, to).toDate()).isEqualTo(to);
        assertThat(new StatisticList(List.of(StatisticEntry.of("orders", 2))).entries()).hasSize(1);
        assertThat(new ReadableSliderImage(1, "a", IMAGE_URL).url()).isEqualTo(IMAGE_URL);
    }
}
