package com.asrevo.cvhome.billing.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.PlanNotFoundException;
import com.asrevo.cvhome.billing.config.BillingProperties;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.mappers.SubscriptionMappers;
import com.asrevo.cvhome.billing.repository.OrgTrialGrantRepository;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.service.PlanCatalogService;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Whether an org may open another store, and what that store's subscription starts as.
 *
 * <p>
 * The trial-once rule lives entirely in {@code OrgTrialGrantRepository.claim} — a primary-key insert that exactly one
 * of two concurrent first-store creations wins. Reading "has this org had a trial?" and then writing would lose that
 * race, and losing it means giving away free months. These tests drive the claim's answer directly, because that
 * return value <em>is</em> the rule.
 * </p>
 */
class StoreQuotaServiceImplTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private StoreSubscriptionRepository subscriptions;

    private OrgTrialGrantRepository grants;

    private PlanCatalogService catalog;

    private SubscriptionAuditService audit;

    private StoreQuotaServiceImpl service;

    private PlanPriceEntity cheapest;

    @BeforeEach
    void setUp() {
        subscriptions = mock(StoreSubscriptionRepository.class);
        grants = mock(OrgTrialGrantRepository.class);
        catalog = mock(PlanCatalogService.class);
        audit = mock(SubscriptionAuditService.class);
        SubscriptionMappers mappers = mock(SubscriptionMappers.class);
        BillingProperties properties = new BillingProperties(Duration.ofDays(14L), Duration.ofDays(7L),
                new BillingProperties.Quota(3));
        service = new StoreQuotaServiceImpl(subscriptions, grants, catalog, audit, mappers, properties);

        cheapest = PlanPriceEntity.create(PlanId.newId(), new CurrencyCode("USD"), 0L, BillingInterval.MONTH, 0);
        when(subscriptions.save(any(StoreSubscriptionEntity.class)))
                .thenAnswer(it -> it.getArgument(0, StoreSubscriptionEntity.class));
        when(mappers.toView(any(StoreSubscriptionEntity.class))).thenAnswer(it -> {
            StoreSubscriptionEntity entity = it.getArgument(0, StoreSubscriptionEntity.class);
            return new SubscriptionView(entity.getId(), entity.getStatus(), null, null, null, null, null,
                    entity.getTrialEnd(), false, null, null, false, Map.of());
        });
    }

    private StoreSubscriptionEntity saved() {
        ArgumentCaptor<StoreSubscriptionEntity> captor = ArgumentCaptor.forClass(StoreSubscriptionEntity.class);
        verify(subscriptions).save(captor.capture());
        return captor.getValue();
    }

    // ------------------------------------------------------------------------------------------------- quota

    @Test
    @DisplayName("an org under the unpaid-store limit may open another")
    void allowsAnotherStore() {
        when(subscriptions.countByOrgIdAndStatus(ORG, SubscriptionStatus.PENDING)).thenReturn(1);
        when(grants.existsById(ORG)).thenReturn(false);

        StoreQuotaDecision decision = service.checkStoreCreate(ORG);

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.reason()).isNull();
        assertThat(decision.pendingStoreCount()).isEqualTo(1);
        assertThat(decision.trialAvailable()).isTrue();
    }

    @Test
    @DisplayName("an org holding its limit of never-paid-for stores is refused, with a reason to render")
    void refusesStockpiling() {
        when(subscriptions.countByOrgIdAndStatus(ORG, SubscriptionStatus.PENDING)).thenReturn(3);
        when(grants.existsById(ORG)).thenReturn(true);

        StoreQuotaDecision decision = service.checkStoreCreate(ORG);

        // Not a cap on stores an org may own — each store carries its own subscription and pays for itself. What is
        // refused is stockpiling stores nobody ever pays for.
        assertThat(decision.allowed()).isFalse();
        assertThat(decision.reason()).isEqualTo("TOO_MANY_PENDING_STORES");
        assertThat(decision.trialAvailable()).isFalse();
    }

    @Test
    @DisplayName("a paying org is never near the limit, however many stores it owns")
    void paidStoresDoNotCount() {
        when(subscriptions.countByOrgIdAndStatus(ORG, SubscriptionStatus.PENDING)).thenReturn(0);
        when(grants.existsById(ORG)).thenReturn(true);

        assertThat(service.checkStoreCreate(ORG).allowed()).isTrue();
        // Only PENDING is counted; ACTIVE, TRIALING and the rest are not asked about at all.
        verify(subscriptions).countByOrgIdAndStatus(ORG, SubscriptionStatus.PENDING);
    }

    // -------------------------------------------------------------------------------------------- provision

    @Test
    @DisplayName("the org's first store wins the trial claim and starts trialling")
    void firstStoreGetsTheTrial() throws Exception {
        when(subscriptions.findById(STORE)).thenReturn(Optional.empty());
        when(grants.claim(anyString(), anyString(), any(Instant.class), any(Instant.class))).thenReturn(1);
        when(catalog.cheapestActivePrice()).thenReturn(Optional.of(cheapest));
        Instant before = Instant.now();

        service.provision(ORG, STORE);

        StoreSubscriptionEntity entity = saved();
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.TRIALING);
        assertThat(entity.getPlanPriceId()).isEqualTo(cheapest.getId());
        // Fourteen days, from BillingProperties.trialPeriod.
        assertThat(entity.getTrialEnd()).isAfterOrEqualTo(before.plus(Duration.ofDays(14L)).minusSeconds(5));
        verify(audit).record(eq(null), eq(null), any(), eq(AuditEventType.TRIAL_STARTED), eq(ChangeSource.SYSTEM),
                eq(null));
    }

    @Test
    @DisplayName("a store whose org lost the claim starts unpaid")
    void aLaterStoreStartsUnpaid() throws Exception {
        when(subscriptions.findById(STORE)).thenReturn(Optional.empty());
        when(grants.claim(anyString(), anyString(), any(Instant.class), any(Instant.class))).thenReturn(0);

        service.provision(ORG, STORE);

        StoreSubscriptionEntity entity = saved();
        // The loser of the race, or simply a second store. Either way the org has already had its one trial.
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(entity.getPlanId()).isNull();
        verify(audit).record(eq(null), eq(null), any(), eq(AuditEventType.CREATED), eq(ChangeSource.SYSTEM),
                eq(null));
        // The catalog is not consulted at all, because no plan is being granted.
        verify(catalog, never()).cheapestActivePrice();
    }

    @Test
    @DisplayName("provisioning a store that already has a subscription changes nothing")
    void provisioningIsIdempotent() throws Exception {
        StoreSubscriptionEntity existing = StoreSubscriptionEntity.pending(STORE, ORG);
        when(subscriptions.findById(STORE)).thenReturn(Optional.of(existing));

        SubscriptionView view = service.provision(ORG, STORE);

        // It arrives from an outbox handler, so a repeat is routine rather than exceptional — and a second claim
        // attempt would be a second chance at the org's one trial.
        assertThat(view.store()).isEqualTo(STORE);
        verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
        verify(grants, never()).claim(anyString(), anyString(), any(), any());
        verify(audit, never()).record(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("an empty catalog cannot grant a trial, and says which lookup failed")
    void anEmptyCatalogIsReported() {
        when(subscriptions.findById(STORE)).thenReturn(Optional.empty());
        when(grants.claim(anyString(), anyString(), any(Instant.class), any(Instant.class))).thenReturn(1);
        when(catalog.cheapestActivePrice()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.provision(ORG, STORE)).isInstanceOf(PlanNotFoundException.class);
    }

    @Test
    @DisplayName("the trial grant is claimed for this store, with the end date the subscription will carry")
    void theClaimNamesTheStore() throws Exception {
        when(subscriptions.findById(STORE)).thenReturn(Optional.empty());
        when(grants.claim(anyString(), anyString(), any(Instant.class), any(Instant.class))).thenReturn(1);
        when(catalog.cheapestActivePrice()).thenReturn(Optional.of(cheapest));

        service.provision(ORG, STORE);

        ArgumentCaptor<String> store = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Instant> trialEnd = ArgumentCaptor.forClass(Instant.class);
        verify(grants).claim(eq(ORG.getId().toString()), store.capture(), any(Instant.class), trialEnd.capture());
        // The grant row names which store spent the org's trial, which is the question support gets asked.
        assertThat(store.getValue()).isEqualTo(STORE.getId().toString());
        assertThat(saved().getTrialEnd()).isEqualTo(trialEnd.getValue());
    }

}
