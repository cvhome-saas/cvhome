package com.asrevo.cvhome.merchant.content.service.populator.content;

import java.util.List;
import java.util.Optional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.content.entity.content.Content;
import com.asrevo.cvhome.merchant.content.entity.content.ContentDescription;
import com.asrevo.cvhome.merchant.content.model.content.box.ReadableContentBox;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.populator.AbstractDataPopulator;

public class ReadableContentBoxPopulator extends AbstractDataPopulator<Content, StoreMerchantId, ReadableContentBox> {

    @Override
    public ReadableContentBox populate(Content source, ReadableContentBox target, StoreMerchantId store,
                                       LanguageCode language) {

        if (target == null) {
            target = new ReadableContentBox();
        }
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setVisible(source.isVisible());

        if (LanguageCode.isAllLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(List.of());
            target.setDescriptions(descriptionSet.stream().map(this::populateDescription).toList());
        }
        if (LanguageCode.isLanguage(language)) {
            var descriptionSet = Optional.ofNullable(source.getDescriptions()).orElse(List.of());
            var description = descriptionSet.stream()
                    .filter(it -> language.equals(it.getLanguageCode()))
                    .findFirst()
                    .map(this::populateDescription)
                    .orElse(null);
            target.setDescription(description);
        }

        return target;
    }

    @Override
    protected ReadableContentBox createTarget() {
        return null;
    }

    com.asrevo.cvhome.merchant.content.model.content.common.ContentDescription populateDescription(
            ContentDescription description) {
        if (description == null) {
            return null;
        }
        var d = new com.asrevo.cvhome.merchant.content.model.content.common.ContentDescription();
        d.setName(description.getName());
        d.setDescription(description.getDescription());
        d.setMetaDescription(description.getMetatagDescription());
        d.setId(description.getId());
        d.setFriendlyUrl(description.getSeUrl());
        d.setTitle(description.getTitle());
        if (description.getLanguageCode() != null) {
            d.setLanguage(description.getLanguageCode());
        }
        return d;
    }

}
