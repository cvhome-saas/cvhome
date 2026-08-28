package com.asrevo.cvhome.content.service.binding;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.content.model.page.ReadablePage;
import com.asrevo.cvhome.content.service.ContentMapper;
import com.asrevo.cvhome.content.service.ContentTypeBinding;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

/**
 * Static storefront pages ({@code /content/<slug>}).
 */
@Component
public class PageBinding implements ContentTypeBinding<PersistablePage, ReadablePage> {

    @Override
    public ContentType type() {
        return ContentType.PAGE;
    }

    @Override
    public Class<PersistablePage> persistableClass() {
        return PersistablePage.class;
    }

    @Override
    public ReadablePage newReadable() {
        return new ReadablePage();
    }

    @Override
    public void apply(Content entity, PersistablePage dto) {
        entity.setParentId(dto.getParentId());
        entity.setShowInFooter(dto.isShowInFooter());
    }

    @Override
    public void populate(Content entity, ReadablePage dto) {
        dto.setParentId(entity.getParentId());
        dto.setShowInFooter(entity.isShowInFooter());
        dto.setStatus(entity.getStatus());
        dto.setLocales(ContentMapper.locales(entity));
        dto.setAudit(ContentMapper.audit(entity));
    }

    @Override
    public String storefrontPath(Content entity) {
        return String.format("/content/%s", entity.getCode());
    }

}
