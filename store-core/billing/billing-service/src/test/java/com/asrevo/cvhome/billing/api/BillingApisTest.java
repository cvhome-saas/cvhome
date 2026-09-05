package com.asrevo.cvhome.billing.api;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.asrevo.cvhome.billing.api.v1.CancelRequest;
import com.asrevo.cvhome.billing.api.v1.CheckoutRequest;
import com.asrevo.cvhome.billing.api.v1.ExternalEntitlementApi;
import com.asrevo.cvhome.billing.api.v1.PlanChangeRequest;
import com.asrevo.cvhome.billing.api.v1.PlatformBillingApi;
import com.asrevo.cvhome.billing.api.v1.StripeWebhookApi;
import com.asrevo.cvhome.billing.api.v1.SubscriptionApi;
import com.asrevo.cvhome.billing.api.v2.BillingStatisticApi;
import com.asrevo.cvhome.billing.commons.PlanEntitlementId;
import com.asrevo.cvhome.billing.commons.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.billing.events.InvoiceRecordedEvent;
import com.asrevo.cvhome.billing.repository.SubscriptionAuditRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.billing.service.EntitlementService;
import com.asrevo.cvhome.billing.service.PlatformBillingService;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.billing.service.stripe.StripeWebhookVerifier;
import com.asrevo.cvhome.billing.service.stripe.WebhookIngestService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The billing endpoints, and the two decisions they make instead of delegating.
 *
 * <p>
 * {@code tenantScopeOf} is the first: a super admin is scoped to {@code null} — every store on the platform — and
 * everybody else to their own organisation. It reads as a one-line helper and is the whole of billing's tenant
 * isolation on these endpoints, so both branches are asserted rather than assumed.
 * </p>
 *
 * <p>
 * The second is the Stripe webhook. A signature that does not verify has to answer 400 and stop, never reaching
 * ingest: Stripe retries a non-2xx, so answering 200 to a forged payload would both accept it and stop the retry of
 * whatever genuine event it was impersonating.
 * </p>
 */
class BillingApisTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");
    private static final String ACTOR = "owner@example.com";
    private static final String PAYLOAD = "{}";
    private static final String HTTPS = "https";
    private static final String CONSOLE = "console-ui";
    private static final String HTTP = "http";
    private static final String CONSOLE_HOST = "console.example.com";
    private static final String CONSOLE_PORT = "4200";
    private static final String INVOICE_KEY = "invoice";
    private static final String INVOICE_ID = "in_1";
    private static final String OBJECT_ID_HEX = "507f1f77bcf86cd799439011";


    private final SubscriptionService subscriptionService = Mockito.mock(SubscriptionService.class);
    private final ServiceDomainProperties serviceDomains = Mockito.mock(ServiceDomainProperties.class);
    private final StripeWebhookVerifier verifier = Mockito.mock(StripeWebhookVerifier.class);
    private final WebhookIngestService ingestService = Mockito.mock(WebhookIngestService.class);
    private final PlatformBillingService platformBillingService = Mockito.mock(PlatformBillingService.class);
    private final SubscriptionInvoiceRepository invoiceRepository =
            Mockito.mock(SubscriptionInvoiceRepository.class);
    private final SubscriptionAuditRepository auditRepository = Mockito.mock(SubscriptionAuditRepository.class);
    private final EntitlementService entitlementService = Mockito.mock(EntitlementService.class);

    private final SubscriptionApi subscriptionApi = new SubscriptionApi(subscriptionService, serviceDomains);
    private final StripeWebhookApi webhookApi = new StripeWebhookApi(verifier, ingestService);
    private final PlatformBillingApi platformApi = new PlatformBillingApi(platformBillingService);
    private final BillingStatisticApi statisticApi =
            new BillingStatisticApi(invoiceRepository, auditRepository, platformBillingService);

    private static ServiceDomain consoleDomain() {
        return new ServiceDomain(CONSOLE, CONSOLE_HOST, CONSOLE_PORT, HTTP, "ns", "gw");
    }

    private static UserOrgStoreIdentity identity(Roles... roles) {
        return new UserOrgStoreIdentity(ORG, STORE, Set.of(roles));
    }

    private static Principal principal() {
        return () -> ACTOR;
    }

    @Test
    void aMerchantIsScopedToItsOwnOrganization() throws Exception {
        subscriptionApi.current(identity(Roles.ROLE_ORG_ADMIN), STORE);

        verify(subscriptionService).current(STORE, ORG);
    }

    @Test
    void aPlatformOperatorIsScopedToEveryStore() throws Exception {
        // A null scope is what "no organisation filter" means downstream; a super admin supports every store.
        subscriptionApi.current(identity(Roles.ROLE_SUPER_ADMIN), STORE);

        verify(subscriptionService).current(STORE, null);
    }

    @Test
    void everyMutationRecordsAnActorAndFallsBackToUnknown() throws Exception {
        subscriptionApi.resume(identity(Roles.ROLE_ORG_ADMIN), STORE, principal());
        subscriptionApi.resume(identity(Roles.ROLE_ORG_ADMIN), STORE, null);

        verify(subscriptionService).resume(STORE, ORG, ACTOR);
        verify(subscriptionService).resume(STORE, ORG, "unknown");
    }

    @Test
    void cancellingTellsTheServiceWhetherTheCallerMayCancelImmediately() throws Exception {
        // Only the platform operator may; the flag is derived from the token, never taken from the request body.
        subscriptionApi.cancel(identity(Roles.ROLE_SUPER_ADMIN), STORE, new CancelRequest(true), principal());
        subscriptionApi.cancel(identity(Roles.ROLE_ORG_ADMIN), STORE, new CancelRequest(true), principal());

        verify(subscriptionService).cancel(STORE, null, true, true, ACTOR);
        verify(subscriptionService).cancel(STORE, ORG, true, false, ACTOR);
    }

    @Test
    void aWebhookWhoseSignatureDoesNotVerifyIsRejectedWithoutReachingIngest() throws Exception {
        when(verifier.verify(eq(PAYLOAD), any())).thenThrow(InvalidWebhookSignatureException.verificationFailed("stripe", true, null));

        ResponseEntity<Void> response = webhookApi.events(PAYLOAD, Map.of());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Mockito.verifyNoInteractions(ingestService);
    }

    @Test
    void averifiedWebhookIsIngestedAndAcknowledged() throws Exception {
        when(verifier.verify(eq(PAYLOAD), any())).thenReturn(null);

        ResponseEntity<Void> response = webhookApi.events(PAYLOAD, Map.of());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(ingestService).ingest(null);
    }

    @Test
    void theOperatorConsoleEndpointsAllDelegateWithTheirQuery() {
        when(platformBillingService.subscriptions(any(), any())).thenReturn(Page.empty());
        when(platformBillingService.invoices(any(), any())).thenReturn(Page.empty());
        when(platformBillingService.audit(any(), any())).thenReturn(Page.empty());

        platformApi.subscriptions(null, PageRequest.of(0, 20));
        platformApi.invoices(null, PageRequest.of(0, 20));
        platformApi.invoiceTotals(null);
        platformApi.audit(null, PageRequest.of(0, 20));

        verify(platformBillingService).subscriptions(eq(null), any());
        verify(platformBillingService).invoices(eq(null), any());
        verify(platformBillingService).invoiceTotals(null);
        verify(platformBillingService).audit(eq(null), any());
    }

    @Test
    void bothStatisticEndpointsConvertTheRangeAndWrapTheirRows() {
        ZonedDateTime from = ZonedDateTime.parse("2026-01-01T00:00:00Z");
        StatisticRange range = new StatisticRange(from, from.plusDays(1));
        when(invoiceRepository.revenueStatistic(any(), any()))
                .thenReturn(List.of(StatisticEntry.of("revenue", 10)));
        when(auditRepository.subscriptionStatistic(any(), any()))
                .thenReturn(List.of(StatisticEntry.of("subscriptions", 2)));

        assertThat(statisticApi.revenueStatistic(range).entries()).hasSize(1);
        assertThat(statisticApi.subscriptionStatistic(range).entries()).hasSize(1);
        statisticApi.planStatistic();
        statisticApi.billingHealth();

        verify(invoiceRepository).revenueStatistic(from.toInstant(), from.plusDays(1).toInstant());
        verify(platformBillingService).planStatistics();
        verify(platformBillingService).health();
    }

    @Test
    void theCheckoutRedirectUsesTheAddressTheBrowserActuallyUsed() throws Exception {
        MockHttpServletRequest forwarded = new MockHttpServletRequest();
        forwarded.setScheme(HTTP);
        forwarded.setServerPort(8080);
        forwarded.addHeader(RedirectionUrlBuilder.SCHEMA_HEADER_KEY, HTTPS);
        forwarded.addHeader(RedirectionUrlBuilder.PORT_HEADER_KEY, "443");
        when(serviceDomains.getService(CONSOLE)).thenReturn(consoleDomain());

        subscriptionApi.checkout(identity(Roles.ROLE_ORG_ADMIN), STORE, new CheckoutRequest(null), forwarded);

        // Behind the gateway the request's own scheme and port are the internal ones. Returning those would send
        // the customer back to a URL that does not resolve from a browser.
        verify(subscriptionService).checkout(STORE, ORG, null,
                "https://%s:443/public/subscription/success".formatted(CONSOLE_HOST),
                "https://%s:443/public/subscription/fail".formatted(CONSOLE_HOST));
    }

    @Test
    void withoutForwardedHeadersTheRequestsOwnSchemeAndPortAreUsed() throws Exception {
        MockHttpServletRequest direct = new MockHttpServletRequest();
        direct.setScheme(HTTPS);
        direct.setServerPort(4200);
        when(serviceDomains.getService(CONSOLE)).thenReturn(consoleDomain());

        subscriptionApi.checkout(identity(Roles.ROLE_ORG_ADMIN), STORE, new CheckoutRequest(null), direct);

        verify(subscriptionService).checkout(STORE, ORG, null,
                "https://%s:%s/public/subscription/success".formatted(CONSOLE_HOST, CONSOLE_PORT),
                "https://%s:%s/public/subscription/fail".formatted(CONSOLE_HOST, CONSOLE_PORT));
    }

    @Test
    void changingPlanIsOneEndpointForBothDirectionsAndRecordsTheActor() throws Exception {
        subscriptionApi.changePlan(identity(Roles.ROLE_ORG_ADMIN), STORE, new PlanChangeRequest(null), principal());

        verify(subscriptionService).changePlan(STORE, ORG, null, ACTOR);
    }

    @Test
    void theEntitlementEndpointsAnswerBothTheSingleAndTheBatchShape() throws Exception {
        ExternalEntitlementApi entitlementApi = new ExternalEntitlementApi(entitlementService);

        entitlementApi.snapshot(STORE);
        entitlementApi.snapshots(List.of(STORE));
        entitlementApi.blockedStores();

        // The batch and blocked-store reads are the ones the pods poll, which is why they are gated on the
        // quota-check token rather than on a single store's entitlement-read.
        verify(entitlementService).snapshot(STORE);
        verify(entitlementService).snapshots(List.of(STORE));
        verify(entitlementService).blockedStores();
    }

    @Test
    void anInvoiceEventKeysOnItsStoreSoOutboxOrderingIsPerStore() {
        InvoiceRecordedEvent bare = InvoiceRecordedEvent.from(STORE, ORG);
        InvoiceRecordedEvent withData = InvoiceRecordedEvent.from(STORE, ORG, Map.of(INVOICE_KEY, INVOICE_ID));

        assertThat(bare.store()).isEqualTo(STORE);
        assertThat(bare.data()).isEmpty();
        assertThat(withData.data()).containsEntry(INVOICE_KEY, INVOICE_ID);
        assertThat(bare.eventType()).isEqualTo("InvoiceRecordedEvent");
    }

    @Test
    void aPlanEntitlementIdParsesAndMintsObjectIdHex() {
        assertThat(new PlanEntitlementId(OBJECT_ID_HEX).getId()).isEqualTo(new ObjectId(OBJECT_ID_HEX));
        assertThat(PlanEntitlementId.newId().getId()).isNotEqualTo(PlanEntitlementId.newId().getId());
    }
}
