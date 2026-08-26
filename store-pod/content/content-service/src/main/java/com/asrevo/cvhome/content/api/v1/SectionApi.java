package com.asrevo.cvhome.content.api.v1;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.api.v1.support.ContentPermissions;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.section.PersistableSection;
import com.asrevo.cvhome.content.model.section.ReadableSection;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.SectionService;
import com.asrevo.cvhome.content.service.binding.SectionBinding;

/**
 * The blocks that make up the store's home page.
 */
@RestController
@RequestMapping("/api/v1/private/content/sections")
public class SectionApi extends WorkflowContentApi<PersistableSection, ReadableSection> {

    private final SectionService sections;

    public SectionApi(ContentItemService items, SectionBinding binding, SectionService sections) {
        super(items, binding);
        this.sections = sections;
    }

    /**
     * Sets the page order in one write. Sections the body omits keep their relative order, after the ones it names.
     */
    @PatchMapping("reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ContentPermissions.MANAGE)
    public void reorder(StoreMerchantId merchantStore, LanguageCode language,
                        @RequestBody @NotNull List<Long> body) throws ContentNotFoundException {
        sections.reorder(merchantStore, body);
    }

}
