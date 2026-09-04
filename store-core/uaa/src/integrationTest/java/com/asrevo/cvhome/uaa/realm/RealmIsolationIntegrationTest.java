package com.asrevo.cvhome.uaa.realm;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the realm column is enforced by Hibernate rather than merely present.
 *
 * <p>
 * The distinction matters: {@code realm_id} carries a {@code default 'platform'} in the DDL, so a {@code @TenantId}
 * that had been wired up wrongly would still produce rows that look right and a suite that still passes. These
 * tests write under one realm and read under another, which only behaves if the discriminator is really being
 * applied to every statement.
 * </p>
 *
 * <p>
 * uaa runs {@code SINGLE} and will keep doing so; the second realm here exists only to observe the mechanism that
 * cua will depend on.
 * </p>
 */
@DatabaseIntegrationTest
class RealmIsolationIntegrationTest {

    private static final String HIDDEN = "hidden";

    private static final String SHARED = "shared";

    private static final RealmId STORE_A = RealmId.of("aaaaaaaaaaaaaaaaaaaaaaaa");

    private static final RealmId STORE_B = RealmId.of("bbbbbbbbbbbbbbbbbbbbbbbb");

    @Autowired
    private UserRepository users;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void hibernateStampsTheRealmItWasEnteredIn() {
        UUID id = RealmContext.callIn(STORE_A, () -> users.save(newUser("stamped")).getId());

        String stored = jdbc.queryForObject("select realm_id from uaa.users where id = ?", String.class, id);

        // 'platform' here would mean the column default wrote it and @TenantId did nothing.
        assertThat(stored).isEqualTo(STORE_A.getId());
    }

    @Test
    void aUserOfOneRealmIsInvisibleInAnother() {
        RealmContext.runIn(STORE_A, () -> users.save(newUser(HIDDEN)));

        assertThat(RealmContext.callIn(STORE_A, () -> users.findByUsername(HIDDEN))).isPresent();
        assertThat(RealmContext.callIn(STORE_B, () -> users.findByUsername(HIDDEN))).isEmpty();
    }

    /**
     * By id as well as by name. {@code CurrentUserResolver} looks an account up by the token's {@code uid}, so if
     * a find by primary key ignored the tenant, one store's token could resolve another store's account.
     */
    @Test
    void aUserOfOneRealmIsInvisibleInAnotherByIdToo() {
        UUID id = RealmContext.callIn(STORE_A, () -> users.save(newUser("by-id")).getId());

        assertThat(RealmContext.callIn(STORE_A, () -> users.findById(id))).isPresent();
        assertThat(RealmContext.callIn(STORE_B, () -> users.findById(id))).isEmpty();
    }

    /** The invariant the whole design rests on: one address is a different person in each store. */
    @Test
    void theSameUsernameAndEmailExistOncePerRealm() {
        RealmContext.runIn(STORE_A, () -> users.save(newUser(SHARED)));
        RealmContext.runIn(STORE_B, () -> users.save(newUser(SHARED)));

        Integer rows = jdbc.queryForObject(
                "select count(*) from uaa.users where username = ?", Integer.class, SHARED);
        assertThat(rows).isEqualTo(2);
    }

    private User newUser(String username) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail("%s@example.test".formatted(username));
        user.setPasswordHash("{noop}irrelevant");
        return user;
    }

}
