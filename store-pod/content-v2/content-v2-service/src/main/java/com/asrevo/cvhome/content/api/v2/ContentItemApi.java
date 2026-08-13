package com.asrevo.cvhome.content.api.v2;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentVersionConflictException;
import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.ContentWriteRequest;
import com.asrevo.cvhome.content.model.LifecycleRequest;
import com.asrevo.cvhome.content.service.ContentV2Service;

@RestController
@RequestMapping("/api/v2/private/content/items")
public class ContentItemApi {
    private final ContentV2Service contentService;

    public ContentItemApi(ContentV2Service contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public ContentView create(@Valid @RequestBody ContentWriteRequest request,
                              StoreMerchantId merchantStore, LanguageCode language, Principal principal) {
        return contentService.create(merchantStore, language, request, principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public List<ContentView> list(StoreMerchantId merchantStore, LanguageCode language) {
        return contentService.list(merchantStore);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public ContentView find(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ContentNotFoundException {
        return contentService.find(merchantStore, id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public ContentView update(@PathVariable Long id, @RequestHeader("If-Match") long expectedVersion,
                              @Valid @RequestBody ContentWriteRequest request, StoreMerchantId merchantStore,
                              LanguageCode language, Principal principal) throws ContentNotFoundException,
            ContentVersionConflictException {
        return contentService.update(merchantStore, language, id, expectedVersion, request, principal.getName());
    }

    @PostMapping("/{id}/lifecycle/{target}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public ContentView transition(@PathVariable Long id, @PathVariable ContentStatus target,
                                  @RequestHeader("If-Match") long expectedVersion,
                                  @Valid @RequestBody LifecycleRequest request, StoreMerchantId merchantStore,
                                  LanguageCode language, Principal principal) throws ContentNotFoundException,
            ContentVersionConflictException, IllegalContentTransitionException {
        return contentService.transition(merchantStore, id, expectedVersion, target, request, principal.getName());
    }
}
