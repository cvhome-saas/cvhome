package com.asrevo.cvhome.sso.config;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.asrevo.cvhome.s2s.model.AdminUserProperties;
import com.asrevo.cvhome.s2s.model.TestStoreProperties;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The two boot-time password seeders.
 *
 * <p>
 * Both exist so a deployment's configured passwords win over whatever the seed SQL inserted, and both only ever
 * <em>update</em> an account the schema already created — neither creates one. That matters: an initializer that
 * created accounts would mint a super-admin on any deployment that happened to set the property, and the seeded
 * ids are what the roles and permissions rows point at.
 * </p>
 *
 * <p>
 * A password that is absent or blank is a deployment that did not ask for a seed, so the run is skipped rather
 * than writing an empty hash — which would leave an account whose password is the empty string.
 * </p>
 */
class SeedInitializersTest {

    private static final UUID SUPER_ADMIN_ID = UUID.fromString("65D8419C-8765-4B8B-A15F-910DCE959931");
    private static final String PASSWORD = "s3cret";
    private static final String ENCODED = "{bcrypt}$2a$10$encoded";

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    @Test
    void theSuperAdminsPasswordIsReplacedWithTheConfiguredOne() {
        User user = user(SUPER_ADMIN_ID);
        when(users.findById(SUPER_ADMIN_ID)).thenReturn(Optional.of(user));
        when(encoder.encode(PASSWORD)).thenReturn(ENCODED);

        adminInitializer(PASSWORD).onApplicationReady();

        assertThat(user.getPasswordHash()).isEqualTo(ENCODED);
        verify(users).save(user);
    }

    @ParameterizedTest(name = "password = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void nopasswordConfiguredMeansNoSeedRatherThanAnEmptyHash(String password) {
        adminInitializer(password).onApplicationReady();

        // Writing an empty hash would leave an account whose password is the empty string.
        verify(users, never()).findById(any(UUID.class));
        verify(users, never()).save(any());
    }

    @Test
    void asuperAdminRowThatDoesNotExistIsNotCreated() {
        when(users.findById(SUPER_ADMIN_ID)).thenReturn(Optional.empty());

        adminInitializer(PASSWORD).onApplicationReady();

        // The seeded id is what the roles and permissions rows point at; minting one here would orphan them.
        verify(users, never()).save(any());
    }

    @Test
    void everyConfiguredTestUsersPasswordIsSynced() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        User firstUser = user(first);
        User secondUser = user(second);
        when(users.findById(first)).thenReturn(Optional.of(firstUser));
        when(users.findById(second)).thenReturn(Optional.of(secondUser));
        when(encoder.encode(PASSWORD)).thenReturn(ENCODED);

        testUserInitializer(new TestStoreProperties.TestUser(first, PASSWORD),
                new TestStoreProperties.TestUser(second, PASSWORD)).onApplicationReady();

        assertThat(firstUser.getPasswordHash()).isEqualTo(ENCODED);
        assertThat(secondUser.getPasswordHash()).isEqualTo(ENCODED);
    }

    @Test
    void atestUserWithNoPasswordIsSavedWithoutOneRatherThanBlanked() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        user.setPasswordHash(ENCODED);
        when(users.findById(id)).thenReturn(Optional.of(user));

        testUserInitializer(new TestStoreProperties.TestUser(id, "  ")).onApplicationReady();

        assertThat(user.getPasswordHash()).isEqualTo(ENCODED);
        verify(encoder, never()).encode(any());
    }

    @Test
    void noconfiguredTestUsersMeansNothingToSync() {
        new TestUserDatabaseInitializer(new TestStoreProperties(null), users, encoder).onApplicationReady();
        new TestUserDatabaseInitializer(new TestStoreProperties(List.of()), users, encoder).onApplicationReady();

        verify(users, never()).save(any());
    }

    private AdminUserDatabaseInitializer adminInitializer(String password) {
        return new AdminUserDatabaseInitializer(new AdminUserProperties(password), users, encoder);
    }

    private TestUserDatabaseInitializer testUserInitializer(TestStoreProperties.TestUser... testUsers) {
        return new TestUserDatabaseInitializer(new TestStoreProperties(List.of(testUsers)), users, encoder);
    }

    private static User user(UUID id) {
        User user = new User();
        user.setId(id);
        user.setUsername("someone");
        user.setEmail("someone@example.com");
        return user;
    }

}
