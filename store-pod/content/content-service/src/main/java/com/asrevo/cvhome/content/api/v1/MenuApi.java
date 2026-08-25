package com.asrevo.cvhome.content.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.MenuHandle;
import com.asrevo.cvhome.content.model.menu.Menu;
import com.asrevo.cvhome.content.service.MenuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/private/content/menus")
@RequiredArgsConstructor
public class MenuApi {

    private final MenuService menus;

    @GetMapping
    @PreAuthorize(ContentPermissions.READ)
    public List<Menu> list(StoreMerchantId merchantStore, LanguageCode language) {
        return menus.list(merchantStore);
    }

    @GetMapping("{handle}")
    @PreAuthorize(ContentPermissions.READ)
    public Menu get(StoreMerchantId merchantStore, LanguageCode language, @PathVariable MenuHandle handle) {
        return menus.get(merchantStore, handle);
    }

    /**
     * Replaces the whole tree (the editor saves the full order). One level of nesting.
     */
    @PutMapping("{handle}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public Menu put(StoreMerchantId merchantStore, LanguageCode language, @PathVariable MenuHandle handle,
                    @RequestBody @Valid Menu body) throws ContentRuleException, InvalidContentRequestException {
        return menus.put(merchantStore, handle, body);
    }

}
