package com.asrevo.cvhome.content.api.v1;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.facade.StorefrontFacade;
import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.banner.PersistableBanner;
import com.asrevo.cvhome.content.model.banner.ReadableBanner;
import com.asrevo.cvhome.content.model.storefront.StorefrontBanner;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.binding.BannerBinding;

@RestController
@RequestMapping("/api/v1/private/content/banners")
public class BannerApi extends WorkflowContentApi<PersistableBanner, ReadableBanner> {

    private final StorefrontFacade storefront;

    public BannerApi(ContentItemService items, BannerBinding binding, StorefrontFacade storefront) {
        super(items, binding);
        this.storefront = storefront;
    }

    /**
     * What the storefront would show right now — the banners that win each placement.
     */
    @GetMapping("effective")
    @PreAuthorize(ContentPermissions.READ)
    public List<StorefrontBanner> effective(StoreMerchantId merchantStore, LanguageCode language,
                                            @RequestParam(required = false) BannerPlacement placement) {
        return storefront.effectiveBanners(merchantStore, language, placement);
    }

}
