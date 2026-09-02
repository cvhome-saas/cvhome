package com.asrevo.cvhome.uaa.web;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** The list's filters over the seeded accounts, and the counts that must agree with them. */
@DatabaseIntegrationTest
class UserSearchIntegrationTest {

    private static final String USERS = "/api/v1/admin/users%s";

    private static final String USERNAME = "username";

    private static final String ORG1_ADMIN = "org1-admin";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    private String admin;

    @BeforeEach
    void setUp() throws Exception {
        uaa = new UaaClient(port);
        admin = uaa.superAdminToken();
    }

    @Test
    void qSearchesUsernameEmailAndNames() throws Exception {
        List<String> byUsername = usernames("?q=org1-store");
        assertThat(byUsername).isNotEmpty().allMatch(name -> name.contains("org1-store"));

        List<String> byName = usernames("?q=Store2%20Mod");
        assertThat(byName).contains("org1-store2-moderator", "org2-store2-moderator");
    }

    @Test
    void statusAndRoleNarrowTogether() throws Exception {
        assertThat(usernames("?status=ACTIVE&role=ORG_ADMIN")).containsExactlyInAnyOrder(ORG1_ADMIN, "org2-admin");
        assertThat(usernames("?status=DISABLED")).doesNotContain(ORG1_ADMIN);
        assertThat(usernames("?metadata%5Borg%5D=no-such-org")).isEmpty();
    }

    @Test
    void countsAddUp() throws Exception {
        JsonNode counts = UaaClient.body(uaa.bearer(UaaClient.GET, String.format(USERS, "/counts"), null, admin));

        long total = counts.get("total").asLong();
        assertThat(total).isGreaterThan(0);
        assertThat(counts.get("active").asLong() + counts.get("pending").asLong() + counts.get("locked").asLong()
                + counts.get("disabled").asLong()).isEqualTo(total);
    }

    private List<String> usernames(String query) throws Exception {
        JsonNode page = UaaClient.body(uaa.bearer(UaaClient.GET, String.format(USERS, query), null, admin));
        return page.get("content").findValuesAsText(USERNAME);
    }

}
