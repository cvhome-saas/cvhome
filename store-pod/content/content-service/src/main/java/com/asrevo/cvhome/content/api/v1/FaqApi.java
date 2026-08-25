package com.asrevo.cvhome.content.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.faq.FaqGroup;
import com.asrevo.cvhome.content.model.faq.FaqReorder;
import com.asrevo.cvhome.content.model.faq.PersistableFaq;
import com.asrevo.cvhome.content.model.faq.ReadableFaq;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.FaqService;
import com.asrevo.cvhome.content.service.binding.FaqBinding;

@RestController
@RequestMapping("/api/v1/private/content/faq")
public class FaqApi extends WorkflowContentApi<PersistableFaq, ReadableFaq> {

    private final FaqService faq;

    public FaqApi(ContentItemService items, FaqBinding binding, FaqService faq) {
        super(items, binding);
        this.faq = faq;
    }

    @GetMapping("groups")
    @PreAuthorize(ContentPermissions.READ)
    public List<FaqGroup> groups(StoreMerchantId merchantStore, LanguageCode language) {
        return faq.groups(merchantStore);
    }

    @PostMapping("groups")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ContentPermissions.MANAGE)
    public FaqGroup createGroup(StoreMerchantId merchantStore, LanguageCode language,
                                @RequestBody @Valid FaqGroup body) throws ContentConflictException {
        return faq.create(merchantStore, body);
    }

    @PutMapping("groups/{id}")
    @PreAuthorize(ContentPermissions.MANAGE)
    public FaqGroup updateGroup(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id,
                                @RequestBody @Valid FaqGroup body)
            throws ContentNotFoundException, ContentConflictException {
        return faq.update(merchantStore, id, body);
    }

    @DeleteMapping("groups/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ContentPermissions.MANAGE)
    public void deleteGroup(StoreMerchantId merchantStore, LanguageCode language, @PathVariable Long id)
            throws ContentNotFoundException {
        faq.delete(merchantStore, id);
    }

    /**
     * Moves entries between groups and positions atomically; every touched group is renumbered 0..n.
     */
    @PatchMapping("reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ContentPermissions.MANAGE)
    public void reorder(StoreMerchantId merchantStore, LanguageCode language,
                        @RequestBody @Valid List<FaqReorder> body) throws ContentNotFoundException {
        faq.reorder(merchantStore, body);
    }

}
