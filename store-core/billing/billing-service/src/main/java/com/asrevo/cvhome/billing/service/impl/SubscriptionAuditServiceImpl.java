package com.asrevo.cvhome.billing.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.domain.SubscriptionAuditEntity;
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

    private final SubscriptionAuditRepository auditRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(SubscriptionStatus before, StoreSubscriptionEntity after, AuditEventType eventType,
                       ChangeSource source, String actor) {
        auditRepository.save(SubscriptionAuditEntity.of(after.getId(), after.getOrgId(), eventType, before,
                after.getStatus(), null, after.getPlanId(), source, actor));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void recordFromWebhook(SubscriptionStatus before, StoreSubscriptionEntity after, AuditEventType eventType,
                                  StripeEventId eventId) {
        auditRepository.save(SubscriptionAuditEntity
                .of(after.getId(), after.getOrgId(), eventType, before, after.getStatus(), null, after.getPlanId(),
                        ChangeSource.WEBHOOK, PROVIDER_ACTOR)
                .causedBy(eventId));
    }

}
