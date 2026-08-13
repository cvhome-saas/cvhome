package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.policy.ContentPolicy;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentVersionConflictException;
import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.errors.PolicyNotFoundException;
import com.asrevo.cvhome.content.errors.PublishedPolicyImmutableException;
import com.asrevo.cvhome.content.events.PolicyVersionPublishedEvent;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.LifecycleRequest;
import com.asrevo.cvhome.content.model.policy.PolicyType;
import com.asrevo.cvhome.content.model.policy.PolicyView;
import com.asrevo.cvhome.content.model.policy.PolicyWriteRequest;
import com.asrevo.cvhome.content.repository.ContentPolicyRepository;
import com.asrevo.cvhome.content.repository.ContentRepository;

@Service
public class PolicyService {
    private final ContentV2Service contentService;
    private final ContentRepository contentRepository;
    private final ContentPolicyRepository policyRepository;
    private final Clock clock = Clock.systemUTC();

    public PolicyService(ContentV2Service contentService, ContentRepository contentRepository,
                         ContentPolicyRepository policyRepository) {
        this.contentService = contentService;
        this.contentRepository = contentRepository;
        this.policyRepository = policyRepository;
    }

    @Transactional
    public PolicyView create(StoreMerchantId store, LanguageCode language, PolicyWriteRequest request, String actor)
            throws ContentNotFoundException, PublishedPolicyImmutableException {
        if (request.content().type() != ContentType.POLICY) {
            throw PublishedPolicyImmutableException.forId(-1L);
        }
        ContentView contentView = contentService.create(store, language, request.content(), actor);
        Content content = contentRepository.findByIdAndStoreMerchantId(contentView.id(), store)
                .orElseThrow(() -> ContentNotFoundException.forId(contentView.id()));
        ContentPolicy policy = new ContentPolicy();
        policy.setContent(content);
        policy.setStoreMerchantId(store);
        policy.setPolicyType(request.policyType());
        policy.setPolicyVersion(request.policyVersion());
        policy.setEffectiveDate(request.effectiveDate());
        policy.setAcceptanceRequired(request.acceptanceRequired());
        policy.setJurisdiction(request.jurisdiction());
        policy.setDisplayLocations(request.displayLocations());
        return toView(policyRepository.save(policy), contentView);
    }

    @Transactional(readOnly = true)
    public List<PolicyView> list(StoreMerchantId store, PolicyType type) throws ContentNotFoundException {
        List<PolicyView> result = new ArrayList<>();
        for (ContentPolicy policy : policyRepository
                .findAllByStoreMerchantIdAndPolicyTypeOrderByEffectiveDateDesc(store, type)) {
            result.add(toView(policy, contentService.find(store, policy.getId())));
        }
        return List.copyOf(result);
    }

    @Transactional(readOnly = true)
    public PolicyView active(StoreMerchantId store, PolicyType type) throws PolicyNotFoundException,
            ContentNotFoundException {
        return list(store, type).stream().filter(PolicyView::active)
                .findFirst().orElseThrow(() -> PolicyNotFoundException.forId(-1L));
    }

    @Transactional
    public PolicyView publish(StoreMerchantId store, Long id, long version, String actor)
            throws PolicyNotFoundException, ContentNotFoundException, ContentVersionConflictException,
            IllegalContentTransitionException, PublishedPolicyImmutableException {
        ContentPolicy target = policyRepository.findByIdAndStoreMerchantId(id, store)
                .orElseThrow(() -> PolicyNotFoundException.forId(id));
        if (target.isActive() || target.getContent().getStatus() == ContentStatus.PUBLISHED) {
            throw PublishedPolicyImmutableException.forId(id);
        }
        List<ContentPolicy> versions = policyRepository
                .findForUpdateByStoreMerchantIdAndPolicyTypeOrderByEffectiveDateDesc(store, target.getPolicyType());
        versions.stream().filter(ContentPolicy::isActive).forEach(it -> it.setActive(false));
        policyRepository.saveAllAndFlush(versions);
        target.setActive(true);
        ContentView contentView = contentService.transition(store, id, version, ContentStatus.PUBLISHED,
                new LifecycleRequest(null, null, "policy-version-published"), actor);
        Content content = contentRepository.findByIdAndStoreMerchantId(id, store)
                .orElseThrow(() -> ContentNotFoundException.forId(id));
        content.policyPublished(new PolicyVersionPublishedEvent(store, target.getPolicyType(), id,
                target.getPolicyVersion(), actor, clock.instant()));
        contentRepository.save(content);
        policyRepository.save(target);
        return toView(target, contentView);
    }

    private static PolicyView toView(ContentPolicy policy, ContentView content) {
        return new PolicyView(content, policy.getPolicyType(), policy.getPolicyVersion(), policy.getEffectiveDate(),
                policy.isAcceptanceRequired(), policy.getJurisdiction(), policy.isActive(),
                Set.copyOf(policy.getDisplayLocations()));
    }
}
