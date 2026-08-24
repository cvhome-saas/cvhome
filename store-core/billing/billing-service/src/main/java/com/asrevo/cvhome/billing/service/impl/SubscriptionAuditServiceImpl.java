package com.asrevo.cvhome.billing.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.admin.ListAuditQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.SubscriptionAuditView;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.domain.SubscriptionAuditEntity;
import com.asrevo.cvhome.billing.mappers.PlatformBillingMappers;
import com.asrevo.cvhome.billing.repository.SubscriptionAuditRepository;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionAuditServiceImpl implements SubscriptionAuditService {

    /**
     * What the audit trail names as the actor when a webhook drove the change. Not the customer: the customer's act
     * was the payment, and this is the provider telling us what came of it.
     */
    private static final String PROVIDER_ACTOR = "stripe";

    /** How many rows a listing falls back to when the caller asks for no page at all. */
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final SubscriptionAuditRepository auditRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(SubscriptionStatus before, PlanId fromPlan, StoreSubscriptionEntity after,
                       AuditEventType eventType, ChangeSource source, String actor) {
        auditRepository.save(SubscriptionAuditEntity.of(after.getId(), after.getOrgId(), eventType, before,
                after.getStatus(), fromPlan, after.getPlanId(), source, actor));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordFromWebhook(SubscriptionStatus before, PlanId fromPlan, StoreSubscriptionEntity after,
                                  AuditEventType eventType, StripeEventId eventId) {
        auditRepository.save(SubscriptionAuditEntity
                .of(after.getId(), after.getOrgId(), eventType, before, after.getStatus(), fromPlan,
                        after.getPlanId(), ChangeSource.WEBHOOK, PROVIDER_ACTOR)
                .causedBy(eventId));
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Two queries assembled into a {@link PageImpl} by hand: Spring Data JDBC's {@code @Query} has no
     * {@code countQuery} attribute — that is JPA's — so there is no way to ask for a {@code Page} directly.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionAuditView> search(ListAuditQuery query, Pageable pageable) {
        Pageable page = pageable == null || pageable.isUnpaged() ? Pageable.ofSize(DEFAULT_PAGE_SIZE) : pageable;
        ListAuditQuery filter = query == null
                ? new ListAuditQuery(null, null, null, null, null, null)
                : query;
        String store = PlatformBillingMappers.idOf(filter.store());
        String org = PlatformBillingMappers.idOf(filter.org());
        String eventType = PlatformBillingMappers.nameOf(filter.eventType());
        String source = PlatformBillingMappers.nameOf(filter.source());
        Instant from = filter.from();
        Instant to = filter.to();

        List<SubscriptionAuditView> rows = auditRepository.findVisible(store, org, eventType, source, from, to,
                page.getPageSize(), page.getOffset());
        long total = auditRepository.countVisible(store, org, eventType, source, from, to);
        return new PageImpl<>(rows, page, total);
    }

}
