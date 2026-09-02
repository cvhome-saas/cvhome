package com.asrevo.cvhome.uaa.web;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Roles through the API: the gate, the catalogue, a custom role's life, a system role's limits, and the audit rows
 * each write leaves.
 */
@DatabaseIntegrationTest
class AdminRoleApiIntegrationTest {

    private static final String ROLES = "/api/v1/admin/roles";

    private static final String ORG_ADMIN_ID = "4ca169a8-e8ac-4874-acae-795bf7b27832";

    private static final String AUDIT_COUNT = "select count(*) from uaa.audit_events where event_type = ? and target_id = ?";

    private static final String NAME = "name";

    private static final String ORG_ADMIN = "ORG_ADMIN";

    private static final String EFFECTIVE = "effectivePermissions";

    private static final String USERS_INVITE = "users:invite";

    private static final String USERS_READ = "users:read";

    private static final String CODE = "code";

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    private static final String ONE = "%s/%s";

    private static final String CREATE_BODY = """
            {"name":"regional_buyer","description":"Buys for a region","scope":"ORGANIZATION",
             "inheritsFromId":"%s","permissions":["users:read"]}""";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    private UaaClient uaa;

    private String token;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        uaa = new UaaClient(port);
        token = uaa.superAdminToken();
    }

    private long audited(String type, String targetId) {
        Long n = jdbc.queryForObject(AUDIT_COUNT, Long.class, type, targetId);
        return n == null ? 0 : n;
    }

    @Test
    void aCustomRoleIsCreatedUpdatedAndDeletedWithAuditRows() throws IOException, InterruptedException {
        String body = String.format(CREATE_BODY, ORG_ADMIN_ID);
        HttpResponse<String> created = uaa.bearer(UaaClient.POST, ROLES, body, token);
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        JsonNode role = UaaClient.body(created);
        String id = role.get("id").asText();
        assertThat(role.get(NAME).asText()).isEqualTo("REGIONAL_BUYER");
        assertThat(role.get("inheritsFromName").asText()).isEqualTo(ORG_ADMIN);
        assertThat(role.get(EFFECTIVE).toString()).contains(USERS_INVITE).contains(USERS_READ);
        assertThat(role.get("systemRole").asBoolean()).isFalse();
        assertThat(audited("role.created", id)).isEqualTo(1);

        HttpResponse<String> updated = uaa.bearer(UaaClient.PUT, String.format(ONE, ROLES, id),
                "{\"permissions\":[\"users:read\",\"audit:read\"],\"clearInheritsFrom\":true}", token);
        assertThat(updated.statusCode()).as(updated.body()).isEqualTo(200);
        assertThat(UaaClient.body(updated).get("inheritsFromId").isNull()).isTrue();
        assertThat(UaaClient.body(updated).get(EFFECTIVE).toString()).doesNotContain(USERS_INVITE);
        assertThat(audited("role.permissions.updated", id)).isEqualTo(1);

        assertThat(uaa.bearer(UaaClient.DELETE, String.format(ONE, ROLES, id), null, token).statusCode()).isEqualTo(200);
        assertThat(audited("role.deleted", id)).isEqualTo(1);
    }

    @Test
    void aSystemRoleRefusesRenameAndDeleteButTakesPermissions() throws IOException, InterruptedException {
        String path = String.format(ONE, ROLES, ORG_ADMIN_ID);

        HttpResponse<String> renamed = uaa.bearer(UaaClient.PUT, path, "{\"name\":\"OWNER\"}", token);
        assertThat(renamed.statusCode()).isEqualTo(403);
        assertThat(UaaClient.body(renamed).get(CODE).asText()).isEqualTo("UAA.ROLE.SYSTEM_IMMUTABLE");

        HttpResponse<String> deleted = uaa.bearer(UaaClient.DELETE, path, null, token);
        assertThat(deleted.statusCode()).isEqualTo(403);

        HttpResponse<String> unknown = uaa.bearer(UaaClient.PUT, path, "{\"permissions\":[\"users:fly\"]}", token);
        assertThat(unknown.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(unknown).get(CODE).asText()).isEqualTo("UAA.PERMISSION.UNKNOWN");
    }

    @Test
    void theCatalogueAndTheGate() throws IOException, InterruptedException {
        HttpResponse<String> catalogue = uaa.bearer(UaaClient.GET, String.format("%s/permissions", ROLES), null, token);
        assertThat(catalogue.statusCode()).isEqualTo(200);
        List<String> keys = UaaClient.body(catalogue).findValues("key").stream().map(JsonNode::asText).toList();
        assertThat(keys).contains(USERS_READ, "settings:write");

        HttpResponse<String> listed = uaa.bearer(UaaClient.GET, ROLES, null, token);
        JsonNode content = UaaClient.body(listed).get("content");
        assertThat(content.findValues(NAME).stream().map(JsonNode::asText)).contains("STORE_RETAIL", ORG_ADMIN);
        Map<String, JsonNode> byName = new java.util.HashMap<>();
        content.forEach(n -> byName.put(n.get(NAME).asText(), n));
        assertThat(byName.get(SUPER_ADMIN_ROLE).get("permissions").size()).isEqualTo(keys.size());
        assertThat(byName.get(ORG_ADMIN).get("userCount").asLong()).isGreaterThanOrEqualTo(2);

        assertThat(uaa.bearer(UaaClient.GET, ROLES, null, uaa.storeCoreToken()).statusCode()).isEqualTo(403);
    }

}
