package com.asrevo.cvhome.commons.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The permission catalog the SSO console renders and the token carries.
 *
 * <p>
 * {@link Permission#key()} is a wire contract: it is what lands in a role's granted set and what
 * {@link Permission#fromKey(String)} reads back off a persisted role. A duplicated or renamed key silently widens or
 * revokes access, and neither shows up as a compile error, so the keys are pinned here.
 * </p>
 */
class PermissionTest {

    @ParameterizedTest
    @EnumSource(Permission.class)
    void everyPermissionRoundTripsThroughItsKey(Permission permission) {
        assertThat(Permission.fromKey(permission.key())).contains(permission);
    }

    @ParameterizedTest
    @EnumSource(Permission.class)
    void everyPermissionCarriesAGroupAndADescription(Permission permission) {
        assertThat(permission.group()).isNotNull();
        assertThat(permission.description()).isNotBlank();
    }

    @Test
    void keysAreUniqueSoFromKeyIsUnambiguous() {
        assertThat(Arrays.stream(Permission.values()).map(Permission::key).collect(Collectors.toSet()))
                .hasSameSizeAs(Permission.values());
    }

    @Test
    void keysAreTheColonSeparatedResourceActionShapeTheConsoleExpects() {
        assertThat(Permission.USERS_READ.key()).isEqualTo("users:read");
        assertThat(Permission.KEYS_ROTATE.key()).isEqualTo("keys:rotate");
        assertThat(Permission.USERS_READ.group()).isEqualTo(Permission.PermissionGroup.IDENTITY);
        assertThat(Permission.KEYS_ROTATE.group()).isEqualTo(Permission.PermissionGroup.SYSTEM);
    }

    @Test
    void anUnknownKeyIsEmptyRatherThanAnException() {
        assertThat(Permission.fromKey("users:delete")).isEmpty();
        assertThat(Permission.fromKey("")).isEmpty();
    }
}
