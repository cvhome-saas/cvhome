package com.asrevo.cvhome.content.api.v2;

import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.asrevo.cvhome.content.errors.InvalidMenuTreeException;
import com.asrevo.cvhome.content.model.menu.MenuView;
import com.asrevo.cvhome.content.model.menu.MenuWriteRequest;
import com.asrevo.cvhome.content.service.MenuService;

@RestController
@RequestMapping("/api/v2/private/content/menus")
public class MenuApi {
    private final MenuService service;

    public MenuApi(MenuService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public MenuView create(@Valid @RequestBody MenuWriteRequest request, StoreMerchantId merchantStore,
                           LanguageCode language, Principal principal) throws InvalidMenuTreeException,
            ContentNotFoundException {
        return service.create(merchantStore, language, request, principal.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public MenuView replace(@PathVariable Long id, @Valid @RequestBody MenuWriteRequest request,
                            StoreMerchantId merchantStore, LanguageCode language) throws InvalidMenuTreeException,
            ContentNotFoundException {
        return service.replace(merchantStore, language, id, request);
    }

    @GetMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CONTENT.*')")
    public MenuView find(@RequestParam String handle, StoreMerchantId merchantStore, LanguageCode language)
            throws ContentNotFoundException {
        return service.find(merchantStore, language, handle);
    }
}
