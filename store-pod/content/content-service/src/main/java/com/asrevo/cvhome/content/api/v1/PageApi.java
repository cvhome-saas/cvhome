package com.asrevo.cvhome.content.api.v1;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.content.model.page.ReadablePage;
import com.asrevo.cvhome.content.service.ContentItemService;
import com.asrevo.cvhome.content.service.binding.PageBinding;

@RestController
@RequestMapping("/api/v1/private/content/pages")
public class PageApi extends WorkflowContentApi<PersistablePage, ReadablePage> {

    public PageApi(ContentItemService items, PageBinding binding) {
        super(items, binding);
    }

}
