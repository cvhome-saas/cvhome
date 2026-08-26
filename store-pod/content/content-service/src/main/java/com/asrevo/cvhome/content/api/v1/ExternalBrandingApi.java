package com.asrevo.cvhome.content.api.v1;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.ExternalBrandingService;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.model.site.SiteBranding;
import com.asrevo.cvhome.content.service.SiteSettingsService;

import lombok.RequiredArgsConstructor;

/**
 * The store's brand imagery as another pod service sees it.
 *
 * <p>
 * Implements {@link ExternalBrandingService} so the route and the client contract cannot drift.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/private/content/external/branding")
@RequiredArgsConstructor
public class ExternalBrandingApi implements ExternalBrandingService {

    private final SiteSettingsService siteSettings;

    /**
     * Reads use {@code CONTENT.READ}, which already falls through to "same store pod" for a service caller.
     */
    @Override
    @GetMapping
    @PreAuthorize(ContentPermissions.READ)
    public SiteBranding branding(StoreMerchantId merchantStore, LanguageCode language) {
        return siteSettings.branding(merchantStore, language);
    }

}
