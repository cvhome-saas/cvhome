package com.asrevo.cvhome.content.api.v1;

import java.util.List;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.config.ContentProperties;
import com.asrevo.cvhome.content.entity.Redirect;
import com.asrevo.cvhome.content.model.summary.ContentSummary;
import com.asrevo.cvhome.content.service.RedirectService;
import com.asrevo.cvhome.content.service.SummaryService;

import lombok.RequiredArgsConstructor;

/**
 * Cross-type private endpoints: the hub summary and the redirect list.
 */
@RestController
@RequestMapping("/api/v1/private/content")
@RequiredArgsConstructor
public class ContentAdminApi {

    private final SummaryService summary;

    private final RedirectService redirects;

    private final ContentProperties properties;

    @GetMapping("summary")
    @PreAuthorize(ContentPermissions.READ)
    public ContentSummary summary(StoreMerchantId merchantStore, LanguageCode language) {
        return summary.summary(merchantStore, properties.media().quota().toBytes());
    }

    @GetMapping("redirects")
    @PreAuthorize(ContentPermissions.READ)
    public List<Redirect> redirects(StoreMerchantId merchantStore, LanguageCode language) {
        return redirects.list(merchantStore);
    }

}
