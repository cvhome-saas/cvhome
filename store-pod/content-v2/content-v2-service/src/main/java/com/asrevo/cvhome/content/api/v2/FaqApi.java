package com.asrevo.cvhome.content.api.v2;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.FaqGroupNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidFaqReorderException;
import com.asrevo.cvhome.content.model.faq.FaqGroupView;
import com.asrevo.cvhome.content.model.faq.FaqGroupWriteRequest;
import com.asrevo.cvhome.content.model.faq.FaqReorderRequest;
import com.asrevo.cvhome.content.model.faq.FaqView;
import com.asrevo.cvhome.content.model.faq.FaqWriteRequest;
import com.asrevo.cvhome.content.service.FaqService;

@RestController
@RequestMapping("/api/v2/private/content/faq")
public class FaqApi {
    private final FaqService service;

    public FaqApi(FaqService service) {
        this.service = service;
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public FaqGroupView createGroup(@Valid @RequestBody FaqGroupWriteRequest request,
                                    StoreMerchantId merchantStore, LanguageCode language) {
        return service.createGroup(merchantStore, language, request);
    }

    @GetMapping("/groups")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public List<FaqGroupView> listGroups(StoreMerchantId merchantStore, LanguageCode language) {
        return service.listGroups(merchantStore, language);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public FaqView create(@Valid @RequestBody FaqWriteRequest request, StoreMerchantId merchantStore,
                          LanguageCode language, Principal principal) throws FaqGroupNotFoundException,
            ContentNotFoundException, InvalidFaqReorderException {
        return service.create(merchantStore, language, request, principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public List<FaqView> list(@RequestParam Long groupId, StoreMerchantId merchantStore, LanguageCode language)
            throws ContentNotFoundException {
        return service.list(merchantStore, groupId);
    }

    @PutMapping("/order")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public List<FaqView> reorder(@Valid @RequestBody FaqReorderRequest request, StoreMerchantId merchantStore,
                                 LanguageCode language) throws FaqGroupNotFoundException,
            InvalidFaqReorderException, ContentNotFoundException {
        return service.reorder(merchantStore, request);
    }
}
