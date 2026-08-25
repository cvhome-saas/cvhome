package com.asrevo.cvhome.podregistry.api.v1;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.testsupport.http.ApiClient.expect;
import static com.asrevo.cvhome.testsupport.http.ApiClient.json;
import static com.asrevo.cvhome.testsupport.http.ApiClient.path;
import static com.asrevo.cvhome.testsupport.http.ApiClient.query;
import static com.asrevo.cvhome.testsupport.http.ApiClient.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The registry over real HTTP: reads for super admins, org admins and store-core service principals; writes for
 * super admins only; an org admin sees only its own private pods, never another org's.
 */
@ServiceIntegrationTest
class PodApiIntegrationTest {

    private static final String PODS = "/api/v1/pod";

    private static final String LIST = path(PODS, "list");

    private static final String DRAIN = "drain";

    private static final String RESUME = "resume";

    private static final String ENDPOINT = "endpoint";

    private static final String TOTAL = "totalElements";

    private static final String OTHER_ORG = "32a034a43cd77581d105c87b";

    private static final String ID = "id";

    private static final String NAME = "name";

    private static final String CONTENT = "content";

    private static final String LIFECYCLE = "lifecycleState";

    private static final String DRAINING = "DRAINING";

    private static final String ACTIVE = "ACTIVE";

    private static final String MISSING = "507f1f77bcf86cd799439099";

    private static final long ONE_HOUR = 3600;

    private static final String POD_BODY = """
            {"name":"%s","endpoint":{"endpoint":"http://%s.example","type":"EXTERNAL"}%s}""";

    private static final String OWNED = ",\"orgId\":\"%s\"";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ApiClient api;

    private Tokens tokens;

    private String superAdmin;

    private static String publicPod(String name) {
        return String.format(POD_BODY, name, name, "");
    }

    private static String privatePod(String name, String org) {
        return String.format(POD_BODY, name, name, String.format(OWNED, org));
    }

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        tokens = new Tokens(signer);
        superAdmin = tokens.superAdmin();
    }

    private String create(String body) {
        ResponseEntity<String> created = api.send(HttpMethod.POST, PODS, superAdmin, body);
        expect(created, HttpStatus.OK);
        return json(created).get(ID).get(ID).asString();
    }

    private static boolean contains(JsonNode array, String id) {
        for (JsonNode node : array) {
            if (id.equals(node.get(ID).get(ID).asString())) {
                return true;
            }
        }
        return false;
    }

    @Test
    void superAdminLifecycleCreateReadUpdateDrainResumeDelete() {
        String name = slug("pod");
        String id = create(publicPod(name));

        ResponseEntity<String> read = api.get(path(PODS, id), superAdmin);
        expect(read, HttpStatus.OK);
        JsonNode view = json(read);
        assertThat(view.get(NAME).asString()).isEqualTo(name);
        assertThat(view.get("visibility").asString()).isEqualTo("PUBLIC");
        assertThat(view.get(LIFECYCLE).asString()).isEqualTo(ACTIVE);
        assertThat(view.get("capacityStores").asInt()).isZero();

        String renamed = slug("renamed");
        ResponseEntity<String> updated = api.send(HttpMethod.PUT, path(PODS, id), superAdmin, publicPod(renamed));
        expect(updated, HttpStatus.OK);
        assertThat(json(updated).get(NAME).asString()).isEqualTo(renamed);
        assertThat(json(updated).get(ENDPOINT).get(ENDPOINT).asString()).contains(renamed);

        ResponseEntity<String> drained = api.send(HttpMethod.POST, path(PODS, id, DRAIN), superAdmin, null);
        expect(drained, HttpStatus.OK);
        assertThat(json(drained).get(LIFECYCLE).asString()).isEqualTo(DRAINING);
        // draining twice is a recorded no-op, not an error
        ResponseEntity<String> again = api.send(HttpMethod.POST, path(PODS, id, DRAIN), superAdmin, null);
        expect(again, HttpStatus.OK);
        assertThat(json(again).get(LIFECYCLE).asString()).isEqualTo(DRAINING);

        ResponseEntity<String> resumed = api.send(HttpMethod.POST, path(PODS, id, RESUME), superAdmin, null);
        expect(resumed, HttpStatus.OK);
        assertThat(json(resumed).get(LIFECYCLE).asString()).isEqualTo(ACTIVE);

        expect(api.send(HttpMethod.DELETE, path(PODS, id), superAdmin, null), HttpStatus.OK);
        expect(api.get(path(PODS, id), superAdmin), HttpStatus.NOT_FOUND);
    }

    @Test
    void unknownPodIsNotFoundOnEveryManageEndpoint() {
        expect(api.get(path(PODS, MISSING), superAdmin), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PUT, path(PODS, MISSING), superAdmin, publicPod(slug("x"))), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.POST, path(PODS, MISSING, DRAIN), superAdmin, null), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.POST, path(PODS, MISSING, RESUME), superAdmin, null), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, path(PODS, MISSING), superAdmin, null), HttpStatus.NOT_FOUND);
    }

    @Test
    void aDuplicateNameIsAConflictOnCreateAndOnRename() {
        String taken = slug("taken");
        create(publicPod(taken));
        String other = create(publicPod(slug("other")));

        ResponseEntity<String> duplicate = api.send(HttpMethod.POST, PODS, superAdmin, publicPod(taken));
        expect(duplicate, HttpStatus.CONFLICT);
        assertThat(json(duplicate).get("code").asString()).isEqualTo("POD_REGISTRY.POD.NAME_TAKEN");

        expect(api.send(HttpMethod.PUT, path(PODS, other), superAdmin, publicPod(taken)), HttpStatus.CONFLICT);
        // renaming to its own name is fine
        expect(api.send(HttpMethod.PUT, path(PODS, other), superAdmin,
                publicPod(json(api.get(path(PODS, other), superAdmin)).get(NAME).asString())), HttpStatus.OK);
    }

    @Test
    void searchNarrowsThePageByNameAndPagesIt() {
        String needle = slug("needle");
        String id = create(publicPod(needle));
        create(publicPod(slug("hay")));

        ResponseEntity<String> found = api.get(query(PODS, String.format("q=%s", needle)), superAdmin);
        expect(found, HttpStatus.OK);
        JsonNode page = json(found);
        assertThat(page.get(TOTAL).asInt()).isEqualTo(1);
        assertThat(page.get(CONTENT).get(0).get(ID).get(ID).asString()).isEqualTo(id);

        ResponseEntity<String> all = api.get(PODS, superAdmin);
        expect(all, HttpStatus.OK);
        assertThat(json(all).get(CONTENT).size()).isGreaterThanOrEqualTo(2);
        assertThat(json(all).get(TOTAL).asInt()).isGreaterThanOrEqualTo(2);

        ResponseEntity<String> blank = api.get(query(PODS, "q="), superAdmin);
        expect(blank, HttpStatus.OK);
        assertThat(json(blank).get(TOTAL).asInt()).isGreaterThanOrEqualTo(2);
    }

    /**
     * An org admin carrying only the {@code store_pod} scope. {@code Tokens.orgAdmin} mints the role with the
     * {@code store_core} scope, and {@code SecurityUtils.getOrgStoreIdentity} tests that scope before the org-admin
     * role, so such a principal arrives with no org and is treated as platform-wide by {@code PodApi.listPods}. This
     * shape is the one where the per-org filtering is actually exercised.
     */
    private String orgAdminWithoutCoreScope(String org) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", String.format("org-admin@%s", org));
        claims.put("roles", List.of(Tokens.ROLE_ORG_ADMIN));
        claims.put("scope", Tokens.SCOPE_STORE_POD);
        claims.put("org", org);
        claims.put("exp", Instant.now().plusSeconds(ONE_HOUR).getEpochSecond());
        return tokens.custom(claims);
    }

    @Test
    @DisplayName("an org admin sees only its own private pods, on both the unpaged list and the page")
    void orgAdminSeesOnlyItsOwnPrivatePods() {
        String shared = create(publicPod(slug("shared")));
        String mine = create(privatePod(slug("mine"), Tokens.ORG_1));
        String theirs = create(privatePod(slug("theirs"), OTHER_ORG));

        JsonNode orgList = json(api.get(LIST, orgAdminWithoutCoreScope(Tokens.ORG_1)));
        assertThat(contains(orgList, mine)).isTrue();
        assertThat(contains(orgList, theirs)).isFalse();
        assertThat(contains(orgList, shared)).isFalse();

        JsonNode orgPage = json(api.get(PODS, orgAdminWithoutCoreScope(Tokens.ORG_1))).get(CONTENT);
        assertThat(contains(orgPage, mine)).isTrue();
        assertThat(contains(orgPage, theirs)).isFalse();

        JsonNode otherList = json(api.get(LIST, orgAdminWithoutCoreScope(OTHER_ORG)));
        assertThat(contains(otherList, theirs)).isTrue();
        assertThat(contains(otherList, mine)).isFalse();

        JsonNode platform = json(api.get(LIST, superAdmin));
        assertThat(contains(platform, mine)).isTrue();
        assertThat(contains(platform, theirs)).isTrue();
        assertThat(contains(platform, shared)).isTrue();
    }

    @Test
    void aStoreCoreServicePrincipalReadsEveryPodForRouting() {
        String id = create(privatePod(slug("routed"), OTHER_ORG));

        ResponseEntity<String> list = api.get(LIST, tokens.s2s(Tokens.SCOPE_STORE_CORE));
        expect(list, HttpStatus.OK);
        assertThat(contains(json(list), id)).isTrue();
        assertThat(json(list).get(0).has(LIFECYCLE)).isFalse();
    }

    @Test
    void orgAdminAndStoreStaffCannotManagePods() {
        String id = create(publicPod(slug("locked")));
        String orgAdmin = tokens.orgAdmin(Tokens.ORG_1);
        String staff = tokens.staff(Tokens.ROLE_STORE_ADMIN, Tokens.STORE_1);

        expect(api.get(path(PODS, id), orgAdmin), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, PODS, orgAdmin, publicPod(slug("nope"))), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.PUT, path(PODS, id), orgAdmin, publicPod(slug("nope2"))), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, path(PODS, id, DRAIN), orgAdmin, null), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, path(PODS, id), orgAdmin, null), HttpStatus.FORBIDDEN);

        expect(api.get(LIST, staff), HttpStatus.FORBIDDEN);
        expect(api.get(PODS, staff), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, path(PODS, id), staff, null), HttpStatus.FORBIDDEN);

        expect(api.get(LIST, null), HttpStatus.UNAUTHORIZED);
        // still there
        expect(api.get(path(PODS, id), superAdmin), HttpStatus.OK);
    }

}
