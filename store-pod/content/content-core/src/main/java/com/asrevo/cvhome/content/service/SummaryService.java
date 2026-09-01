package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.summary.ContentSummary;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.ContentSpecifications;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * The hub's KPI cards and rail counts. Media figures are filled in by the media service once it exists; until then
 * they read zero with the configured quota.
 */
@Service
@RequiredArgsConstructor
public class SummaryService {

    private static final Duration STALE_AFTER = Duration.ofDays(30);

    private final ContentRepository repository;

    private final BindingRegistry bindings;

    private final Clock clock;

    private final List<MediaFigures> mediaFigures;

    @Transactional(readOnly = true)
    public ContentSummary summary(StoreMerchantId store, long bytesQuota) {
        ContentSummary s = new ContentSummary();
        List<ContentType> workflow = bindings.workflowTypes();

        s.setPublishedItems(repository.countByStoreMerchantIdAndContentTypeInAndStatus(store, workflow,
                ContentStatus.PUBLISHED));
        s.getDrafts().setTotal(repository.countByStoreMerchantIdAndContentTypeInAndStatus(store, workflow,
                ContentStatus.DRAFT));
        s.getDrafts().setStaleOver30Days(repository.countStale(store, ContentStatus.DRAFT, workflow,
                clock.instant().minus(STALE_AFTER)));

        Specification<Content> awaiting = (root, q, cb) -> cb.and(
                cb.equal(root.get("storeMerchantId"), store),
                root.get("contentType").in(workflow));
        List<Content> awaitingItems = repository.findAll(awaiting.and(ContentSpecifications.awaitingTranslation()));
        Map<String, Long> byLocale = new TreeMap<>();
        for (Content c : awaitingItems) {
            for (ContentDescription d : c.getDescriptions()) {
                if (d.getState() != TranslationState.TRANSLATED) {
                    byLocale.merge(d.getLanguageCode().code(), 1L, Long::sum);
                }
            }
        }
        s.getAwaitingTranslation().setTotal(awaitingItems.size());
        s.getAwaitingTranslation().setByLocale(byLocale);

        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("pages", repository.countByStoreMerchantIdAndContentType(store, ContentType.PAGE));
        counts.put("posts", repository.countByStoreMerchantIdAndContentType(store, ContentType.POST));
        counts.put("banners", repository.countByStoreMerchantIdAndContentType(store, ContentType.BANNER));
        counts.put("faq", repository.countByStoreMerchantIdAndContentType(store, ContentType.FAQ));
        counts.put("policies", repository.countByStoreMerchantIdAndContentType(store, ContentType.POLICY));
        counts.put("media", 0L);
        counts.put("menus", 0L);

        s.getMedia().setBytesQuota(bytesQuota);
        for (MediaFigures f : mediaFigures) {
            f.contribute(store, s, counts);
        }
        s.setCounts(counts);
        return s;
    }

    /**
     * Hook for the media and menu services to add their figures without the summary knowing them.
     */
    public interface MediaFigures {

        void contribute(StoreMerchantId store, ContentSummary summary, Map<String, Long> counts);

    }

}
