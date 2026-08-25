package com.asrevo.cvhome.content.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.Actors;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.common.PublishRequest;
import com.asrevo.cvhome.content.model.common.SavedContent;
import com.asrevo.cvhome.content.model.policy.PersistablePolicy;
import com.asrevo.cvhome.content.model.policy.PolicyCompliance;
import com.asrevo.cvhome.content.model.policy.PolicyTemplate;
import com.asrevo.cvhome.content.model.policy.PublishPolicyVersionRequest;
import com.asrevo.cvhome.content.model.policy.ReadablePolicy;
import com.asrevo.cvhome.content.model.policy.ReadablePolicyVersion;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.PolicyService;
import com.asrevo.cvhome.content.service.binding.PolicyBinding;

/**
 * Legal policies. Publishing the head (inherited {@code POST {id}/publish}) cuts a new immutable version from its
 * text; {@code publish-version} does the same with an explicit effective date and note.
 */
@RestController
@RequestMapping("/api/v1/private/content/policies")
public class PolicyApi extends WorkflowContentApi<PersistablePolicy, ReadablePolicy> {

    private final PolicyService policies;

    public PolicyApi(ContentItemService items, PolicyBinding binding, PolicyService policies) {
        super(items, binding);
        this.policies = policies;
    }

    @GetMapping("compliance")
    @PreAuthorize(ContentPermissions.READ)
    public List<PolicyCompliance> compliance(StoreMerchantId merchantStore, LanguageCode language) {
        return policies.compliance(merchantStore);
    }

    @GetMapping("templates")
    @PreAuthorize(ContentPermissions.READ)
    public PolicyTemplate template(StoreMerchantId merchantStore, LanguageCode language,
                                   @RequestParam PolicyType type,
                                   @RequestParam(required = false) String jurisdiction) {
        return policies.template(type, jurisdiction);
    }

    @GetMapping("{id}/versions")
    @PreAuthorize(ContentPermissions.READ)
    public List<ReadablePolicyVersion> versions(StoreMerchantId merchantStore, LanguageCode language,
                                                @PathVariable Long id) throws ContentNotFoundException {
        return policies.versions(items.load(binding, id, merchantStore));
    }

    @GetMapping("{id}/versions/{version}")
    @PreAuthorize(ContentPermissions.READ)
    public ReadablePolicyVersion version(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                         @PathVariable int version) throws ContentNotFoundException {
        return policies.version(items.load(binding, id, merchantStore), version);
    }

    /**
     * Publishes the head (which cuts a new version when its text changed) and records the effective date and note
     * on the live version.
     */
    @PostMapping("{id}/publish-version")
    @PreAuthorize(ContentPermissions.MANAGE)
    public ReadablePolicyVersion publishVersion(StoreMerchantId merchantStore, LanguageCode language,
                                                @PathVariable Long id,
                                                @RequestBody(required = false) @Valid PublishPolicyVersionRequest body)
            throws ContentNotFoundException, ContentRuleException, InvalidContentRequestException {
        PublishRequest publish = new PublishRequest();
        if (body != null && body.getEffectiveFrom() != null) {
            publish.setPublishAt(body.getEffectiveFrom());
        }
        SavedContent saved = items.transition(binding, id, merchantStore, ContentStatus.PUBLISHED, publish, language,
                Actors.current());
        Content head = items.load(binding, saved.getId(), merchantStore);
        return policies.annotateLive(head, body, Actors.current());
    }

    /**
     * Copies an old version's text back onto the head as its draft text — a new head version, never a mutation.
     */
    @PostMapping("{id}/versions/{version}/restore-text")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent restoreText(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                    @PathVariable int version)
            throws ContentNotFoundException, ContentConflictException, ContentRuleException {
        Content head = items.load(binding, id, merchantStore);
        ReadablePolicy dto = items.toReadable(binding, head);
        dto.setTranslations(policies.textOf(head, version));
        return items.update(binding, id, dto, merchantStore, language, Actors.current());
    }

}
