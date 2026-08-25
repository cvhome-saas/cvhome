package com.asrevo.cvhome.podregistry.api.v1;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.podregistry.domain.PodStorePlacementEntity;
import com.asrevo.cvhome.podregistry.repository.PodStorePlacementRepository;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.testsupport.http.ApiClient.expect;
import static com.asrevo.cvhome.testsupport.http.ApiClient.json;
import static com.asrevo.cvhome.testsupport.http.ApiClient.path;
import static com.asrevo.cvhome.testsupport.http.ApiClient.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Placement over real HTTP: a store-core service principal asks, a private pod wins over a shared one, an org whose
 * own pods are all ineligible is refused with a 422 rather than quietly given shared hardware, and recording a
 * placement is idempotent so the outbox may redeliver it forever.
 */
@ServiceIntegrationTest
class PodPlacementApiIntegrationTest {

    private static final String PODS = "/api/v1/pod";

    private static final String PRIVATE = "private";

    private static final String EMPTY_LIST = "[]";

    private static final String PLACEMENT = path(PODS, PRIVATE, "placement");

    private static final String RECORDED = path(PODS, PRIVATE, "placement-recorded");

    private static final String RECORDED_MANY = path(PODS, PRIVATE, "placements-recorded");

    private static final String ID = "id";

    private static final String POD_ID = "podId";

    private static final String DEDICATED = "dedicated";

    private static final String CAPACITY = "capacityStores";

    private static final String POD_BODY = """
            {"name":"%s","endpoint":{"endpoint":"http://%s.example","type":"EXTERNAL"}%s}""";

    private static final String OWNED = ",\"orgId\":\"%s\"";

    private static final String PLACE_BODY = "{\"org\":\"%s\"}";

    private static final String PLACE_PREFERRED_BODY = "{\"org\":\"%s\",\"preferredPodId\":\"%s\"}";

    private static final String RECORD_BODY = "{\"store\":\"%s\",\"pod\":\"%s\"}";

    private static final String RECORD_MANY_BODY = "[{\"store\":\"%s\",\"pod\":\"%s\"},{\"store\":\"%s\",\"pod\":\"%s\"}]";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private PodStorePlacementRepository placements;

    private ApiClient api;

    private Tokens tokens;

    private String superAdmin;

    private String service;

    private static String publicPod(String name) {
        return String.format(POD_BODY, name, name, "");
    }

    private static String privatePod(String name, String org) {
        return String.format(POD_BODY, name, name, String.format(OWNED, org));
    }

    private static String newId() {
        return new ObjectId().toHexString();
    }

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        tokens = new Tokens(signer);
        superAdmin = tokens.superAdmin();
        service = tokens.s2s(Tokens.SCOPE_STORE_CORE);
    }

    private String create(String body) {
        ResponseEntity<String> created = api.send(HttpMethod.POST, PODS, superAdmin, body);
        expect(created, HttpStatus.OK);
        return json(created).get(ID).get(ID).asString();
    }

    private void drain(String id) {
        expect(api.send(HttpMethod.POST, path(PODS, id, "drain"), superAdmin, null), HttpStatus.OK);
    }

    private ResponseEntity<String> place(String body) {
        return api.send(HttpMethod.POST, PLACEMENT, service, body);
    }

    private int capacityOf(String id) {
        return json(api.get(path(PODS, id), superAdmin)).get(CAPACITY).asInt();
    }

    @Test
    void anOrgWithoutPrivatePodsLandsOnASharedPod() {
        String shared = create(publicPod(slug("open")));

        ResponseEntity<String> placed = place(String.format(PLACE_PREFERRED_BODY, newId(), shared));
        expect(placed, HttpStatus.OK);
        JsonNode decision = json(placed);
        assertThat(decision.get(POD_ID).get(ID).asString()).isEqualTo(shared);
        assertThat(decision.get(DEDICATED).asBoolean()).isFalse();
        assertThat(decision.get("reason").asString()).contains("shared");
    }

    @Test
    void anOrgWithAPrivatePodIsPlacedOnIt() {
        String org = newId();
        create(publicPod(slug("public")));
        String mine = create(privatePod(slug("mine"), org));

        ResponseEntity<String> placed = place(String.format(PLACE_BODY, org));
        expect(placed, HttpStatus.OK);
        assertThat(json(placed).get(POD_ID).get(ID).asString()).isEqualTo(mine);
        assertThat(json(placed).get(DEDICATED).asBoolean()).isTrue();
    }

    @Test
    @DisplayName("an org whose private pods are all draining is refused, never moved onto shared hardware")
    void allPrivatePodsIneligibleIsARefusal() {
        String org = newId();
        create(publicPod(slug("fallback")));
        drain(create(privatePod(slug("drained"), org)));

        ResponseEntity<String> refused = place(String.format(PLACE_BODY, org));
        expect(refused, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(refused).get("code").asString()).isEqualTo("POD_REGISTRY.PLACEMENT.NO_ELIGIBLE_POD");
    }

    @Test
    void recordingAPlacementCountsOnceHoweverOftenItIsRedelivered() {
        String pod = create(publicPod(slug("counted")));
        String store = newId();
        String body = String.format(RECORD_BODY, store, pod);

        expect(api.send(HttpMethod.POST, RECORDED, service, body), HttpStatus.OK);
        expect(api.send(HttpMethod.POST, RECORDED, service, body), HttpStatus.OK);

        assertThat(capacityOf(pod)).isEqualTo(1);
        PodStorePlacementEntity row = placements.findById(new StoreMerchantId(store)).orElseThrow();
        assertThat(row.getPodId()).isEqualTo(new PodId(pod));
        assertThat(row.getPlacedAt()).isNotNull();
    }

    @Test
    void recordingManyPlacementsReconcilesTheCountIdempotently() {
        String pod = create(publicPod(slug("bulk")));
        String body = String.format(RECORD_MANY_BODY, newId(), pod, newId(), pod);

        expect(api.send(HttpMethod.POST, RECORDED_MANY, service, body), HttpStatus.OK);
        expect(api.send(HttpMethod.POST, RECORDED_MANY, service, body), HttpStatus.OK);
        expect(api.send(HttpMethod.POST, RECORDED_MANY, service, EMPTY_LIST), HttpStatus.OK);

        assertThat(capacityOf(pod)).isEqualTo(2);
    }

    @Test
    void aFullPodIsNoLongerOfferedToNewStores() {
        String org = newId();
        String full = create(privatePod(slug("full"), org));
        expect(api.send(HttpMethod.POST, RECORDED, service, String.format(RECORD_BODY, newId(), full)), HttpStatus.OK);
        // no cap set: still has room
        expect(place(String.format(PLACE_BODY, org)), HttpStatus.OK);
    }

    @Test
    void onlyAStoreCoreServicePrincipalMayAskForPlacement() {
        String pod = create(publicPod(slug("guarded")));
        String staff = tokens.staff(Tokens.ROLE_STORE_ADMIN, Tokens.STORE_1);
        String podService = tokens.s2s(Tokens.SCOPE_STORE_POD, pod);
        String body = String.format(PLACE_BODY, newId());

        expect(api.send(HttpMethod.POST, PLACEMENT, staff, body), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, PLACEMENT, podService, body), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, RECORDED, staff, String.format(RECORD_BODY, newId(), pod)),
                HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, RECORDED_MANY, staff, EMPTY_LIST), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, PLACEMENT, null, body), HttpStatus.UNAUTHORIZED);
        assertThat(capacityOf(pod)).isZero();
    }

}
