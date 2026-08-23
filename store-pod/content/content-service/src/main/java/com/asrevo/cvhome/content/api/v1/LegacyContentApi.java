package com.asrevo.cvhome.content.api.v1;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.facade.LegacyContentFacade;
import com.asrevo.cvhome.content.model.legacy.LegacyContentBox;
import com.asrevo.cvhome.content.model.legacy.LegacyContentPage;
import com.asrevo.cvhome.content.model.legacy.LegacyContentPageList;

import lombok.RequiredArgsConstructor;

/**
 * The public reads the storefront calls today, in their original URLs and shapes. Removed once landing-ui reads the
 * storefront API ({@code /api/v1/storefront/**}).
 *
 * @deprecated compatibility surface only; do not add to it
 */
@Deprecated(since = "content platform", forRemoval = true)
@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
public class LegacyContentApi {

    private final LegacyContentFacade facade;

    @GetMapping("pages")
    public LegacyContentPageList pages(StoreMerchantId merchantStore, LanguageCode language, Pageable pageable) {
        return facade.pages(merchantStore, language, pageable);
    }

    @GetMapping("pages/{code}")
    public LegacyContentPage page(StoreMerchantId merchantStore, LanguageCode language, @PathVariable String code)
            throws ContentNotFoundException {
        return facade.pageByCode(merchantStore, language, code);
    }

    @GetMapping("pages/name/{name}")
    public LegacyContentPage pageByName(StoreMerchantId merchantStore, LanguageCode language,
                                        @PathVariable String name) throws ContentNotFoundException {
        return facade.pageByName(merchantStore, language, name);
    }

    @GetMapping("boxes/{code}")
    public LegacyContentBox box(StoreMerchantId merchantStore, LanguageCode language, @PathVariable String code)
            throws ContentNotFoundException {
        return facade.box(merchantStore, language, code);
    }

}
