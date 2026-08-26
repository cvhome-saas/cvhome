package com.asrevo.cvhome.content.api.v1;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.Actors;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.site.PersistableSiteSettings;
import com.asrevo.cvhome.content.model.site.ReadableSiteSettings;
import com.asrevo.cvhome.content.service.SiteSettingsService;

import lombok.RequiredArgsConstructor;

/**
 * How the store looks: brand imagery, social links and site-level SEO. One record per store, so there is no id in
 * the path and no create — the first read makes the row.
 *
 * <p>
 * These used to be spread across merchant (logo, banner, slider images, social links) and the legacy content
 * snippets ({@code meta-title}, {@code meta-description}). Both are gone; this is the only place they live now.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/private/content/site-settings")
@RequiredArgsConstructor
public class SiteSettingsApi {

    private final SiteSettingsService service;

    @GetMapping
    @PreAuthorize(ContentPermissions.READ)
    public ReadableSiteSettings get(StoreMerchantId merchantStore, LanguageCode language) {
        return service.get(merchantStore, language);
    }

    /**
     * Replaces the whole record. A {@code null} media slot clears it.
     *
     * @throws ContentNotFoundException a referenced asset is not in this store's media library
     */
    @PutMapping
    @PreAuthorize(ContentPermissions.MANAGE)
    public ReadableSiteSettings put(StoreMerchantId merchantStore, LanguageCode language,
                                    @RequestBody @Valid PersistableSiteSettings body)
            throws ContentNotFoundException {
        return service.put(merchantStore, body, language, Actors.current());
    }

}
