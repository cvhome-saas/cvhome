package com.asrevo.cvhome.content.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.Actors;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.config.ContentProperties;
import com.asrevo.cvhome.content.entity.Redirect;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.snippet.Snippet;
import com.asrevo.cvhome.content.model.summary.ContentSummary;
import com.asrevo.cvhome.content.service.RedirectService;
import com.asrevo.cvhome.content.service.SnippetService;
import com.asrevo.cvhome.content.service.SummaryService;

import lombok.RequiredArgsConstructor;

/**
 * Cross-type private endpoints: the hub summary, the store snippets (legacy BOX rows) and the redirect list.
 */
@RestController
@RequestMapping("/api/v1/private/content")
@RequiredArgsConstructor
public class ContentAdminApi {

    private final SummaryService summary;

    private final SnippetService snippets;

    private final RedirectService redirects;

    private final ContentProperties properties;

    @GetMapping("summary")
    @PreAuthorize(ContentPermissions.READ)
    public ContentSummary summary(StoreMerchantId merchantStore, LanguageCode language) {
        return summary.summary(merchantStore, properties.media().quota().toBytes());
    }

    @GetMapping("snippets")
    @PreAuthorize(ContentPermissions.READ)
    public List<Snippet> snippets(StoreMerchantId merchantStore, LanguageCode language) {
        return snippets.list(merchantStore);
    }

    @GetMapping("snippets/{code}")
    @PreAuthorize(ContentPermissions.READ)
    public Snippet snippet(StoreMerchantId merchantStore, LanguageCode language, @PathVariable String code)
            throws ContentNotFoundException {
        return snippets.get(merchantStore, code);
    }

    @PutMapping("snippets/{code}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public Snippet putSnippet(StoreMerchantId merchantStore, LanguageCode language, @PathVariable String code,
                              @RequestBody @Valid Snippet body) {
        return snippets.put(merchantStore, code, body, Actors.current());
    }

    @DeleteMapping("snippets/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ContentPermissions.MANAGE)
    public void deleteSnippet(StoreMerchantId merchantStore, LanguageCode language, @PathVariable String code)
            throws ContentNotFoundException {
        snippets.delete(merchantStore, code);
    }

    @GetMapping("redirects")
    @PreAuthorize(ContentPermissions.READ)
    public List<Redirect> redirects(StoreMerchantId merchantStore, LanguageCode language) {
        return redirects.list(merchantStore);
    }

}
