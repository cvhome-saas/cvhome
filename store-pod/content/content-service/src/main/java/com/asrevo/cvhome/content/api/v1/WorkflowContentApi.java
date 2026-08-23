package com.asrevo.cvhome.content.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.Actors;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.common.BulkRequest;
import com.asrevo.cvhome.content.model.common.BulkResult;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.common.PersistableContent;
import com.asrevo.cvhome.content.model.common.PublishRequest;
import com.asrevo.cvhome.content.model.common.ReadableContentRowList;
import com.asrevo.cvhome.content.model.common.ReadableRevision;
import com.asrevo.cvhome.content.model.common.SavedContent;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.ContentTypeBinding;
import com.asrevo.cvhome.content.service.ListQuery;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;

/**
 * The private console API every workflow type shares. A concrete subclass supplies the {@code @RequestMapping}
 * prefix and the type's binding; Spring picks up the inherited mappings.
 *
 * <p>
 * Every method takes the store (from {@code ?store=}) and the language (from {@code ?lang=}, the locale the console
 * is editing in — used as the "source" locale for publish gating and stale marking).
 * </p>
 */
public abstract class WorkflowContentApi<P extends PersistableContent, R extends P> {

    protected final ContentItemService items;

    protected final ContentTypeBinding<P, R> binding;

    protected WorkflowContentApi(ContentItemService items, ContentTypeBinding<P, R> binding) {
        this.items = items;
        this.binding = binding;
    }

    @GetMapping
    @PreAuthorize(ContentPermissions.READ)
    public ReadableContentRowList list(StoreMerchantId merchantStore, LanguageCode language,
                                       @RequestParam(required = false) ContentStatus status,
                                       @RequestParam(required = false) String locale,
                                       @RequestParam(required = false) TranslationState state,
                                       @RequestParam(required = false) String q,
                                       Pageable pageable) {
        LanguageCode localeCode = locale == null || locale.isBlank() || "all".equalsIgnoreCase(locale) ? null
                : new LanguageCode(locale);
        return items.list(binding, merchantStore, language, new ListQuery(status, localeCode, state, q), pageable);
    }

    @GetMapping("{id}")
    @PreAuthorize(ContentPermissions.READ)
    public R get(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException {
        return items.get(binding, id, merchantStore);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent create(StoreMerchantId merchantStore, LanguageCode language, @RequestBody @Valid P body)
            throws ContentConflictException, ContentRuleException {
        return items.create(binding, body, merchantStore, language, Actors.current());
    }

    @PutMapping("{id}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent update(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                               @RequestBody @Valid P body)
            throws ContentNotFoundException, ContentConflictException, ContentRuleException {
        return items.update(binding, id, body, merchantStore, language, Actors.current());
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ContentPermissions.MANAGE)
    public void delete(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                       @RequestParam(defaultValue = "false") boolean force)
            throws ContentNotFoundException, ContentConflictException {
        items.delete(binding, id, merchantStore, force);
    }

    @PostMapping("{id}/publish")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent publish(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                @RequestBody(required = false) PublishRequest body)
            throws ContentNotFoundException, ContentRuleException, InvalidContentRequestException {
        return items.transition(binding, id, merchantStore, ContentStatus.PUBLISHED, body, language,
                Actors.current());
    }

    @PostMapping("{id}/unpublish")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent unpublish(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException, ContentRuleException, InvalidContentRequestException {
        return items.transition(binding, id, merchantStore, ContentStatus.DRAFT, null, language, Actors.current());
    }

    @PostMapping("{id}/submit-review")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent submitReview(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException, ContentRuleException, InvalidContentRequestException {
        return items.transition(binding, id, merchantStore, ContentStatus.REVIEW, null, language, Actors.current());
    }

    @PostMapping("{id}/archive")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent archive(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException, ContentRuleException, InvalidContentRequestException {
        return items.transition(binding, id, merchantStore, ContentStatus.ARCHIVED, null, language,
                Actors.current());
    }

    @PostMapping("{id}/restore")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent restoreArchived(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException, ContentRuleException, InvalidContentRequestException {
        return items.transition(binding, id, merchantStore, ContentStatus.DRAFT, null, language, Actors.current());
    }

    @GetMapping("{id}/revisions")
    @PreAuthorize(ContentPermissions.READ)
    public List<ReadableRevision> revisions(StoreMerchantId merchantStore, LanguageCode language,
                                            @PathVariable Long id) throws ContentNotFoundException {
        return items.revisions(binding, id, merchantStore);
    }

    @PostMapping("{id}/revisions/{version}/restore")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent restoreRevision(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                        @PathVariable Integer version)
            throws ContentNotFoundException, ContentConflictException, ContentRuleException {
        return items.restore(binding, id, version, merchantStore, language, Actors.current());
    }

    @PutMapping("{id}/translations/{locale}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedContent translation(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                    @PathVariable String locale, @RequestBody @Valid ContentTranslation body)
            throws ContentNotFoundException {
        return items.updateTranslation(binding, id, new LanguageCode(locale), body, merchantStore, Actors.current());
    }

    @GetMapping("slug-available")
    @PreAuthorize(ContentPermissions.READ)
    public EntityExists slugAvailable(StoreMerchantId merchantStore, LanguageCode language, @RequestParam String slug,
                                      @RequestParam(required = false) Long excludeId) {
        // "exists" = the slug is free to use, mirroring the legacy code/exists pre-flight the console already knows
        return new EntityExists(items.slugAvailable(merchantStore, slug, excludeId));
    }

    @PostMapping("bulk")
    @ResponseStatus(HttpStatus.MULTI_STATUS)
    @PreAuthorize(ContentPermissions.MANAGE)
    public List<BulkResult> bulk(StoreMerchantId merchantStore, LanguageCode language,
                                 @RequestBody @Valid BulkRequest body) throws InvalidContentRequestException {
        return items.bulk(binding, body, merchantStore, language, Actors.current());
    }

}
