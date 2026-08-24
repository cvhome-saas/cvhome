package com.asrevo.cvhome.catalog.services.group;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.ProductGroup;
import com.asrevo.cvhome.catalog.entity.ProductGroupDescription;
import com.asrevo.cvhome.catalog.model.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.services.product.ProductMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductGroupMapper {

    private final ProductMapper productMapper;

    /**
     * The group with its members. Members are the minimal product shape: the strip that renders them needs copy,
     * images and the sku to fetch price and stock from inventory, nothing more.
     */
    public ReadableProductGroup toReadable(ProductGroup group, LanguageCode language, boolean allLanguages) {
        ReadableProductGroup readable = summary(group, language, allLanguages);
        if (group.getParentProduct() != null) {
            readable.setParentProduct(productMapper.toMinimal(group.getParentProduct(), language));
        }
        readable.setProducts(group.getProducts().stream().map(p -> productMapper.toMinimal(p, language)).toList());
        return readable;
    }

    /**
     * The group without its members, for the console's list.
     */
    public ReadableProductGroup summary(ProductGroup group, LanguageCode language, boolean allLanguages) {
        ReadableProductGroup readable = new ReadableProductGroup();
        readable.setId(group.getId());
        readable.setCode(group.getCode());
        readable.setActive(group.isActive());
        if (LanguageCode.isLanguage(language)) {
            group.description(language).map(ProductGroupMapper::description).ifPresent(readable::setDescription);
        }
        if (allLanguages) {
            readable.setDescriptions(group.getDescriptions().stream().map(ProductGroupMapper::description).toList());
        }
        return readable;
    }

    public static void apply(PersistableProductGroup source, ProductGroup target) {
        target.setCode(source.getCode());
        target.setActive(source.isActive());
        Map<LanguageCode, ProductGroupDescription> existing = new HashMap<>();
        target.getDescriptions().forEach(d -> existing.put(d.getLanguageCode(), d));
        target.getDescriptions().clear();
        for (com.asrevo.cvhome.catalog.model.group.ProductGroupDescription d : source.getDescriptions()) {
            ProductGroupDescription entity = existing.getOrDefault(d.getLanguage(),
                    new ProductGroupDescription(target));
            entity.setLanguageCode(d.getLanguage());
            entity.setName(d.getName());
            entity.setDescription(d.getDescription());
            entity.setSeUrl(d.getFriendlyUrl());
            entity.setMetaTitle(d.getTitle());
            entity.setMetaKeywords(d.getKeyWords());
            entity.setMetaDescription(d.getMetaDescription());
            target.getDescriptions().add(entity);
        }
    }

    private static com.asrevo.cvhome.catalog.model.group.ProductGroupDescription description(
            ProductGroupDescription d) {
        var readable = new com.asrevo.cvhome.catalog.model.group.ProductGroupDescription();
        readable.setId(d.getId());
        readable.setLanguage(d.getLanguageCode());
        readable.setName(d.getName());
        readable.setDescription(d.getDescription());
        readable.setFriendlyUrl(d.getSeUrl());
        readable.setTitle(d.getMetaTitle());
        readable.setKeyWords(d.getMetaKeywords());
        readable.setMetaDescription(d.getMetaDescription());
        return readable;
    }
}
