package com.asrevo.cvhome.content.service.binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.FaqGroup;
import com.asrevo.cvhome.content.model.faq.FaqMeta;
import com.asrevo.cvhome.content.model.faq.PersistableFaq;
import com.asrevo.cvhome.content.model.faq.ReadableFaq;
import com.asrevo.cvhome.content.service.ContentMapper;
import com.asrevo.cvhome.content.service.ContentTypeBinding;
import com.asrevo.cvhome.content.service.FaqService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * FAQ entries: question = title, answer = body; group = parentId, position = sortOrder; keywords and the
 * checkout-help flag in {@code meta}.
 */
@Component
@RequiredArgsConstructor
public class FaqBinding implements ContentTypeBinding<PersistableFaq, ReadableFaq> {

    private final FaqService faq;

    @Override
    public ContentType type() {
        return ContentType.FAQ;
    }

    @Override
    public Class<PersistableFaq> persistableClass() {
        return PersistableFaq.class;
    }

    @Override
    public ReadableFaq newReadable() {
        return new ReadableFaq();
    }

    @Override
    public void apply(Content entity, PersistableFaq dto) {
        entity.setParentId(dto.getGroupId() != null ? dto.getGroupId() : faq.defaultGroupId(entity.getStoreMerchantId()));
        if (dto.getPosition() != null) {
            entity.setSortOrder(dto.getPosition());
        }
        List<String> keywords = dto.getKeywords() == null ? List.of()
                : dto.getKeywords().stream().map(String::trim).filter(k -> !k.isEmpty()).distinct().toList();
        entity.setMeta(JsonCodec.write(new FaqMeta(keywords, dto.isShowInCheckoutHelp())));
    }

    @Override
    public void populate(Content entity, ReadableFaq dto) {
        FaqMeta meta = meta(entity);
        dto.setGroupId(entity.getParentId());
        dto.setPosition(entity.getSortOrder());
        dto.setKeywords(meta.keywords() == null ? new ArrayList<>() : new ArrayList<>(meta.keywords()));
        dto.setShowInCheckoutHelp(meta.showInCheckoutHelp());
        dto.setStatus(entity.getStatus());
        dto.setLocales(ContentMapper.locales(entity));
        dto.setAudit(ContentMapper.audit(entity));
        dto.setGroupName(groupName(entity, null));
    }

    @Override
    public String subtitle(Content entity, LanguageCode language) {
        String group = groupName(entity, language);
        int position = entity.getSortOrder() == null ? 0 : entity.getSortOrder();
        return String.format("%s · position %d", group == null ? "—" : group, position + 1);
    }

    private String groupName(Content entity, LanguageCode language) {
        if (entity.getParentId() == null) {
            return null;
        }
        Map<Long, FaqGroup> groups = faq.byIds(entity.getStoreMerchantId());
        FaqGroup g = groups.get(entity.getParentId());
        if (g == null) {
            return null;
        }
        Map<String, String> names = FaqService.names(g);
        String wanted = language == null ? null : names.get(language.code());
        if (wanted != null) {
            return wanted;
        }
        return names.values().stream().findFirst().orElse(g.getKey());
    }

    public static FaqMeta meta(Content entity) {
        FaqMeta m = JsonCodec.read(entity.getMeta(), FaqMeta.class);
        return m != null ? m : new FaqMeta(List.of(), false);
    }

}
