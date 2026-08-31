package com.asrevo.cvhome.content.api.v1;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.Actors;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.api.v1.support.PreviewTokens;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.model.layout.PersistableLayout;
import com.asrevo.cvhome.content.model.layout.PersistableSavedSection;
import com.asrevo.cvhome.content.model.layout.PublishLayoutRequest;
import com.asrevo.cvhome.content.model.layout.PublishedLayout;
import com.asrevo.cvhome.content.model.layout.ReadableLayout;
import com.asrevo.cvhome.content.model.layout.ReadableRevisionRow;
import com.asrevo.cvhome.content.model.layout.SavedSection;
import com.asrevo.cvhome.content.service.PageLayoutService;
import com.asrevo.cvhome.content.service.SectionPresetService;

import lombok.RequiredArgsConstructor;

/**
 * The storefront builder's write surface: one layout document per page, edited as a draft and published whole.
 * Not a {@link WorkflowContentApi} — a layout has no slug, list or per-item status; its lifecycle is the
 * draft/published pair plus revisions.
 *
 * <p>
 * Every mutating call carries the {@code draftVersion} the builder loaded ({@code baseVersion}); a mismatch is
 * a 409 and the builder reloads rather than clobbering someone else's save.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/private/content/layouts")
@RequiredArgsConstructor
public class LayoutApi {

    private final PageLayoutService layouts;

    private final SectionPresetService presets;

    private final PreviewTokens previews;

    @GetMapping("{page}")
    @PreAuthorize(ContentPermissions.READ)
    public ReadableLayout get(StoreMerchantId merchantStore, LanguageCode language, @PathVariable PageKind page) {
        return layouts.get(merchantStore, page);
    }

    @PutMapping("{page}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public ReadableLayout put(StoreMerchantId merchantStore, LanguageCode language, @PathVariable PageKind page,
                              @RequestBody @Valid PersistableLayout body)
            throws InvalidContentRequestException, ContentConflictException {
        return layouts.save(merchantStore, page, body, Actors.current());
    }

    @PostMapping("{page}/publish")
    @PreAuthorize(ContentPermissions.MANAGE)
    public PublishedLayout publish(StoreMerchantId merchantStore, LanguageCode language,
                                   @PathVariable PageKind page, @RequestBody @Valid PublishLayoutRequest body)
            throws ContentConflictException, ContentRuleException {
        return layouts.publish(merchantStore, page, body.baseVersion(), Actors.current());
    }

    @PostMapping("{page}/discard")
    @PreAuthorize(ContentPermissions.MANAGE)
    public ReadableLayout discard(StoreMerchantId merchantStore, LanguageCode language, @PathVariable PageKind page,
                                  @RequestBody @Valid PublishLayoutRequest body)
            throws ContentConflictException {
        return layouts.discard(merchantStore, page, body.baseVersion(), Actors.current());
    }

    @GetMapping("{page}/revisions")
    @PreAuthorize(ContentPermissions.READ)
    public List<ReadableRevisionRow> revisions(StoreMerchantId merchantStore, LanguageCode language,
                                               @PathVariable PageKind page) {
        return layouts.revisions(merchantStore, page);
    }

    @PostMapping("{page}/revisions/{version}/restore")
    @PreAuthorize(ContentPermissions.MANAGE)
    public ReadableLayout restore(StoreMerchantId merchantStore, LanguageCode language, @PathVariable PageKind page,
                                  @PathVariable int version) throws ContentNotFoundException {
        return layouts.restore(merchantStore, page, version, Actors.current());
    }

    /**
     * A short-lived token the storefront accepts to render this page's draft ({@code ?preview=<token>}). The
     * token's subject is {@code layout:<page>}, matching what {@code StorefrontApi.layout} verifies.
     */
    @PostMapping("{page}/preview-token")
    @PreAuthorize(ContentPermissions.READ)
    public Map<String, String> previewToken(StoreMerchantId merchantStore, LanguageCode language,
                                            @PathVariable PageKind page) {
        return Map.of("token", previews.issue(merchantStore, previewSlug(page)));
    }

    static String previewSlug(PageKind page) {
        return "layout:" + page.name();
    }

    // ------------------------------------------------------------------------------------- saved sections

    @GetMapping("section-presets")
    @PreAuthorize(ContentPermissions.READ)
    public List<SavedSection> sectionPresets(StoreMerchantId merchantStore, LanguageCode language) {
        return presets.list(merchantStore);
    }

    @PostMapping("section-presets")
    @PreAuthorize(ContentPermissions.MANAGE)
    public SavedSection saveSectionPreset(StoreMerchantId merchantStore, LanguageCode language,
                                          @RequestBody @Valid PersistableSavedSection body)
            throws InvalidContentRequestException {
        return presets.save(merchantStore, body, Actors.current());
    }

    @DeleteMapping("section-presets/{id}")
    @PreAuthorize(ContentPermissions.MANAGE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSectionPreset(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException {
        presets.delete(merchantStore, id);
    }

}
