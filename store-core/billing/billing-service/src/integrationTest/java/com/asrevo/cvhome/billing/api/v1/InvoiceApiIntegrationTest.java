package com.asrevo.cvhome.billing.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.billing.api.BillingApiSupport;
import com.asrevo.cvhome.billing.api.BillingFixtures;
import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.billing.domain.SubscriptionInvoiceEntity;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.billing.api.BillingApiSupport.INVOICE_STORE;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.NEIGHBOUR_STORE;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_A;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_B;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.V1;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.expect;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.json;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.path;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A store's own invoice history.
 *
 * <p>
 * Carries the same tenant guard as {@code SubscriptionApi}, and for the same reason: the shared permission checker
 * cannot tell which org a store belongs to, so the boundary lives in which query the controller chooses. A neighbour
 * whose invoices are visible here is a neighbour whose revenue is visible.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class InvoiceApiIntegrationTest {

    private static final String LIST = path(V1, "invoice", "list");

    private static final String CONTENT = "content";

    private static final String MINE = "in_it_mine";

    private static final String NEIGHBOURS = "in_it_neighbours";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private BillingFixtures fixtures;

    @Autowired
    private SubscriptionInvoiceRepository invoices;

    private BillingApiSupport api;

    private String orgAdmin;

    @BeforeEach
    void setUp() {
        api = new BillingApiSupport(port, signer);
        orgAdmin = api.orgAdmin(ORG_A);
        fixtures.publishPrices();
        fixtures.active(INVOICE_STORE, fixtures.dearestPrice());
        record(MINE, INVOICE_STORE, ORG_A);
        record(NEIGHBOURS, NEIGHBOUR_STORE, ORG_B);
    }

    /** One settled invoice, written straight in: the paths that create them are Stripe's, and Stripe is stubbed. */
    private void record(String id, String store, String org) {
        StripeInvoiceId invoiceId = new StripeInvoiceId(id);
        if (invoices.findById(invoiceId).isPresent()) {
            return;
        }
        invoices.save(SubscriptionInvoiceEntity.record(invoiceId, new StoreMerchantId(store), new ManagerOrgId(org),
                        new StripeSubscriptionId("sub_" + id), "CVH-" + id, InvoiceStatus.PAID,
                        new Money(new CurrencyCode("USD"), 3000L), 3000L, java.time.Instant.now())
                .settled(InvoiceStatus.PAID, 3000L, java.time.Instant.now()));
    }

    @Test
    @DisplayName("an org admin reads its store's invoices")
    void listsTheStoresInvoices() {
        ResponseEntity<String> response = api.get(scoped(LIST, INVOICE_STORE), orgAdmin);

        expect(response, HttpStatus.OK);
        JsonNode page = json(response);
        assertThat(page.get(CONTENT)).isNotEmpty();
        assertThat(page.get(CONTENT).valueStream().map(it -> it.get("id").get("id").asString()).toList())
                .contains(MINE)
                .doesNotContain(NEIGHBOURS);
    }

    @Test
    @DisplayName("amounts come back in minor units, in the currency they were billed in")
    void invoicesAreInMinorUnits() {
        JsonNode invoice = json(api.get(scoped(LIST, INVOICE_STORE), orgAdmin)).get(CONTENT).get(0);

        // Minor units end to end — what Stripe speaks — so there is no rounding step between the catalog and an
        // invoice, and zero-decimal currencies work unchanged.
        assertThat(invoice.get("amountPaid").get("minorUnits").asLong()).isEqualTo(3000L);
        assertThat(invoice.get("amountPaid").get("currency").get("code").asString()).isEqualTo("USD");
        assertThat(invoice.get("status").asString()).isEqualTo(InvoiceStatus.PAID.name());
    }

    @Test
    @DisplayName("a store moderator may read the store's invoices")
    void aModeratorMayRead() {
        expect(api.get(scoped(LIST, INVOICE_STORE), api.storeModerator(INVOICE_STORE, ORG_A)), HttpStatus.OK);
    }

    @Test
    @DisplayName("the neighbouring org's admin sees nothing of this store's invoices")
    void anotherOrgSeesNothing() {
        ResponseEntity<String> response = api.get(scoped(LIST, INVOICE_STORE), api.orgAdmin(ORG_B));

        // The query is narrowed by org, so the page comes back empty rather than 403 — the invoices are simply not
        // reachable. Empty and *not* containing the row we know is there is the whole assertion.
        expect(response, HttpStatus.OK);
        assertThat(json(response).get(CONTENT)).isEmpty();
    }

    @Test
    @DisplayName("this org's admin cannot read the neighbour's invoices either")
    void cannotReachTheNeighbour() {
        ResponseEntity<String> response = api.get(scoped(LIST, NEIGHBOUR_STORE), orgAdmin);

        expect(response, HttpStatus.OK);
        assertThat(json(response).get(CONTENT)).isEmpty();
        // And the neighbour's invoice really exists, so the empty page above is a refusal rather than an absence.
        assertThat(invoices.findById(new StripeInvoiceId(NEIGHBOURS))).isPresent();
    }

    @Test
    @DisplayName("the platform operator spans orgs and sees the store's invoices whichever tenant it is")
    void theOperatorSpansOrgs() {
        ResponseEntity<String> response = api.get(scoped(LIST, NEIGHBOUR_STORE), api.superAdmin());

        // "This merchant says they paid" is the support question a platform console exists to answer.
        expect(response, HttpStatus.OK);
        assertThat(json(response).get(CONTENT).valueStream().map(it -> it.get("id").get("id").asString()).toList())
                .contains(NEIGHBOURS);
    }

    @Test
    @DisplayName("a store admin of another store is refused")
    void aForeignStoreAdminIsRefused() {
        expect(api.get(scoped(LIST, INVOICE_STORE), api.storeAdmin(NEIGHBOUR_STORE, ORG_B)), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("no token at all is refused")
    void anonymousIsRefused() {
        expect(api.get(scoped(LIST, INVOICE_STORE), null), HttpStatus.UNAUTHORIZED);
    }

}
