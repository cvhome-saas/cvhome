package com.asrevo.cvhome.content.api.v2;

import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.page.PageView;
import com.asrevo.cvhome.content.model.page.PageWriteRequest;
import com.asrevo.cvhome.content.service.PageService;

@RestController
@RequestMapping("/api/v2/private/content/pages")
public class PageApi {
    private final PageService pageService;

    public PageApi(PageService pageService) {
        this.pageService = pageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public PageView create(@Valid @RequestBody PageWriteRequest request, StoreMerchantId merchantStore,
                           LanguageCode language, Principal principal) throws ContentNotFoundException {
        return pageService.create(merchantStore, language, request, principal.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public PageView find(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ContentNotFoundException {
        return pageService.find(merchantStore, id);
    }
}
