package com.asrevo.cvhome.content.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.PolicyVersion;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.PolicyVersionStatus;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.policy.PolicyCompliance;
import com.asrevo.cvhome.content.model.policy.PolicyTemplate;
import com.asrevo.cvhome.content.model.policy.PublishPolicyVersionRequest;
import com.asrevo.cvhome.content.model.policy.ReadablePolicyVersion;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.PolicyVersionRepository;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * Legal policies: the head row (type, draft text, workflow) plus immutable published versions. Publishing the head
 * cuts a new LIVE version from its current text and archives the previous one; old versions stay readable.
 */
@Service
@RequiredArgsConstructor
public class PolicyService {

    /**
     * Which policy types a selling region requires. Until the store's regions are known here, every store is held
     * to the EU list, which is the strictest the platform sells into.
     */
    private static final String EU = "EU";

    private static final String UK = "UK";

    private static final String US = "US";

    private static final String AR = "ar";

    private static final Map<PolicyType, List<String>> REQUIRED_BY = Map.of(
            PolicyType.TERMS, List.of(EU, UK, US),
            PolicyType.PRIVACY, List.of(EU, UK, US),
            PolicyType.RETURNS, List.of(EU, UK),
            PolicyType.COOKIES, List.of(EU, UK),
            PolicyType.SHIPPING, List.of());

    private static final Map<PolicyType, String[]> TITLES = Map.of(
            PolicyType.TERMS, new String[] {"Terms of service", "شروط الخدمة"},
            PolicyType.PRIVACY, new String[] {"Privacy policy", "سياسة الخصوصية"},
            PolicyType.RETURNS, new String[] {"Returns & refunds", "الإرجاع والاسترداد"},
            PolicyType.SHIPPING, new String[] {"Shipping policy", "سياسة الشحن"},
            PolicyType.COOKIES, new String[] {"Cookie notice", "إشعار ملفات تعريف الارتباط"},
            PolicyType.CUSTOM, new String[] {"Policy", "سياسة"});

    private final PolicyVersionRepository versions;

    private final ContentRepository contents;

    private final Clock clock;

    @Transactional(readOnly = true)
    public List<ReadablePolicyVersion> versions(Content head) {
        List<ReadablePolicyVersion> out = new ArrayList<>();
        for (PolicyVersion v : versions.findByContentIdOrderByVersionDesc(head.getId())) {
            out.add(toDto(v, false));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public ReadablePolicyVersion version(Content head, int version) throws ContentNotFoundException {
        return versions.findByContentIdAndVersion(head.getId(), version).map(v -> toDto(v, true))
                .orElseThrow(() -> ContentNotFoundException.byId(head.getId(), head.getStoreMerchantId()));
    }

    @Transactional(readOnly = true)
    public Optional<PolicyVersion> live(Content head) {
        return versions.findFirstByContentIdAndStatus(head.getId(), PolicyVersionStatus.LIVE);
    }

    @Transactional(readOnly = true)
    public Optional<PolicyVersion> versionEntity(Content head, int version) {
        return versions.findByContentIdAndVersion(head.getId(), version);
    }

    public int liveVersion(Content head) {
        return live(head).map(PolicyVersion::getVersion).orElse(0);
    }

    /**
     * Cuts version n+1 from the head's current translations and makes it LIVE; the previous LIVE is ARCHIVED.
     * The head itself must be publishable (title + body in the source locale) — the caller has gated that.
     */
    @Transactional(rollbackFor = Exception.class)
    public ReadablePolicyVersion publishVersion(Content head, PublishPolicyVersionRequest request, String actor) {
        int next = versions.findByContentIdOrderByVersionDesc(head.getId()).stream()
                .findFirst().map(v -> v.getVersion() + 1).orElse(1);
        versions.findFirstByContentIdAndStatus(head.getId(), PolicyVersionStatus.LIVE).ifPresent(prev -> {
            prev.setStatus(PolicyVersionStatus.ARCHIVED);
            // flush before inserting the new LIVE row: Hibernate orders inserts before updates, and the partial
            // unique index (one LIVE per policy) would otherwise fire
            versions.saveAndFlush(prev);
        });
        PolicyVersion v = new PolicyVersion();
        v.setStoreMerchantId(head.getStoreMerchantId().getId());
        v.setContentId(head.getId());
        v.setVersion(next);
        v.setStatus(PolicyVersionStatus.LIVE);
        v.setEffectiveFrom(request != null && request.getEffectiveFrom() != null ? request.getEffectiveFrom()
                : clock.instant());
        v.setNote(request != null ? request.getNote() : null);
        v.setTranslations(JsonCodec.write(ContentMapper.translations(head)));
        v.setPublishedAt(clock.instant());
        v.setPublishedBy(actor);
        return toDto(versions.saveAndFlush(v), true);
    }

    /**
     * Sets the note and effective date on the live version — what the explicit publish-version call adds on top of
     * the cut the publish transition already made.
     */
    @Transactional(rollbackFor = Exception.class)
    public ReadablePolicyVersion annotateLive(Content head, PublishPolicyVersionRequest request, String actor) {
        PolicyVersion live = versions.findFirstByContentIdAndStatus(head.getId(), PolicyVersionStatus.LIVE)
                .orElseGet(() -> {
                    // nothing live yet (head text identical to an archived cut): publish explicitly
                    return null;
                });
        if (live == null) {
            return publishVersion(head, request, actor);
        }
        if (request != null) {
            if (request.getEffectiveFrom() != null) {
                live.setEffectiveFrom(request.getEffectiveFrom());
            }
            if (request.getNote() != null) {
                live.setNote(request.getNote());
            }
        }
        return toDto(versions.saveAndFlush(live), true);
    }

    /**
     * Restores an old version's text onto the head as the new draft text. Never mutates the version.
     */
    @Transactional(readOnly = true)
    public List<ContentTranslation> textOf(Content head, int version) throws ContentNotFoundException {
        PolicyVersion v = versions.findByContentIdAndVersion(head.getId(), version)
                .orElseThrow(() -> ContentNotFoundException.byId(head.getId(), head.getStoreMerchantId()));
        return translations(v);
    }

    public void forget(Content head) {
        versions.deleteByContentId(head.getId());
    }

    /**
     * Refuses a second head of the same type in the store.
     */
    public void assertTypeFree(StoreMerchantId store, PolicyType type, Long excludeId)
            throws com.asrevo.cvhome.content.errors.ContentConflictException {
        for (Content c : contents.findAllByType(store, ContentType.POLICY)) {
            if (c.getPolicyType() == type && !c.getId().equals(excludeId)) {
                throw com.asrevo.cvhome.content.errors.ContentConflictException.policyTypeActive(type.name(),
                        c.getId(), store);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<PolicyCompliance> compliance(StoreMerchantId store) {
        List<Content> heads = contents.findAllByType(store, ContentType.POLICY);
        List<PolicyCompliance> out = new ArrayList<>();
        for (PolicyType type : PolicyType.values()) {
            if (type == PolicyType.CUSTOM) {
                continue;
            }
            Content head = heads.stream().filter(c -> c.getPolicyType() == type).findFirst().orElse(null);
            out.add(new PolicyCompliance(type, REQUIRED_BY.getOrDefault(type, List.of()),
                    head == null ? null : head.getStatus(), head == null ? null : head.getId()));
        }
        return out;
    }

    /**
     * Starter text from the classpath ({@code policy-templates/<type>[-<jurisdiction>].<lang>.html}).
     */
    public PolicyTemplate template(PolicyType type, String jurisdiction) {
        List<ContentTranslation> translations = new ArrayList<>();
        String base = type.name().toLowerCase(Locale.ROOT);
        String j = jurisdiction == null ? "" : jurisdiction.trim().toLowerCase(Locale.ROOT);
        for (String lang : List.of("en", AR)) {
            String body = read(String.format("policy-templates/%s-%s.%s.html", base, j, lang));
            if (body == null) {
                body = read(String.format("policy-templates/%s.%s.html", base, lang));
            }
            if (body != null) {
                ContentTranslation t = new ContentTranslation();
                t.setLanguage(new LanguageCode(lang));
                t.setTitle(titleOf(type, lang));
                t.setBody(body);
                translations.add(t);
            }
        }
        return new PolicyTemplate(type, jurisdiction, translations);
    }

    private static String titleOf(PolicyType type, String lang) {
        return TITLES.get(type)[AR.equals(lang) ? 1 : 0];
    }

    private static String read(String path) {
        ClassPathResource res = new ClassPathResource(path);
        if (!res.exists()) {
            return null;
        }
        try (InputStream in = res.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException _) {
            return null;
        }
    }

    public static boolean isLive(Content head, Optional<PolicyVersion> live) {
        return head.getStatus() == ContentStatus.PUBLISHED && live.isPresent();
    }

    @SuppressWarnings("unchecked")
    public static List<ContentTranslation> translations(PolicyVersion v) {
        ContentTranslation[] arr = JsonCodec.read(v.getTranslations(), ContentTranslation[].class);
        return arr == null ? List.of() : List.of(arr);
    }

    static ReadablePolicyVersion toDto(PolicyVersion v, boolean withText) {
        ReadablePolicyVersion d = new ReadablePolicyVersion();
        d.setVersion(v.getVersion());
        d.setStatus(v.getStatus());
        d.setNote(v.getNote());
        d.setEffectiveFrom(v.getEffectiveFrom());
        d.setPublishedAt(v.getPublishedAt());
        d.setPublishedBy(v.getPublishedBy());
        if (withText) {
            d.setTranslations(translations(v));
        }
        return d;
    }

}
