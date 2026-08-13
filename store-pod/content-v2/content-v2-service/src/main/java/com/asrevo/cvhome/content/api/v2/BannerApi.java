package com.asrevo.cvhome.content.api.v2;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.BannerArtworkRequiredException;
import com.asrevo.cvhome.content.errors.BannerCapacityExceededException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentVersionConflictException;
import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.errors.InvalidMediaException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.LifecycleRequest;
import com.asrevo.cvhome.content.model.banner.BannerPlacement;
import com.asrevo.cvhome.content.model.banner.BannerView;
import com.asrevo.cvhome.content.model.banner.BannerWriteRequest;
import com.asrevo.cvhome.content.service.BannerService;

@RestController
@RequestMapping("/api/v2/private/content/banners")
public class BannerApi {
    private final BannerService service;

    public BannerApi(BannerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public BannerView create(@Valid @RequestBody BannerWriteRequest request, StoreMerchantId merchantStore,
                             LanguageCode language, Principal principal) throws BannerCapacityExceededException,
            InvalidMediaException, ContentNotFoundException {
        return service.create(merchantStore, language, request, principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public List<BannerView> list(@RequestParam BannerPlacement placement, StoreMerchantId merchantStore,
                                 LanguageCode language) throws ContentNotFoundException {
        return service.list(merchantStore, language, placement);
    }

    @PostMapping("/{id}/lifecycle/{target}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public BannerView transition(@PathVariable Long id, @PathVariable ContentStatus target,
                                 @RequestHeader("If-Match") long version,
                                 @Valid @RequestBody LifecycleRequest request, StoreMerchantId merchantStore,
                                 LanguageCode language, Principal principal) throws ContentNotFoundException,
            ContentVersionConflictException, IllegalContentTransitionException, BannerArtworkRequiredException {
        return service.transition(merchantStore, language, id, version, target, request, principal.getName());
    }
}
