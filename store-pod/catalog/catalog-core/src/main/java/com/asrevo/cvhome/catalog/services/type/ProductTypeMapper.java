package com.asrevo.cvhome.catalog.services.type;

import java.util.HashMap;
import java.util.Map;

import com.asrevo.cvhome.catalog.entity.ProductType;
import com.asrevo.cvhome.catalog.entity.ProductTypeDescription;
import com.asrevo.cvhome.catalog.model.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.type.ReadableProductType;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public final class ProductTypeMapper {

    private ProductTypeMapper() {
    }

    public static ReadableProductType toReadable(ProductType type, LanguageCode language, boolean allLanguages) {
        ReadableProductType readable = new ReadableProductType();
        readable.setId(type.getId());
        readable.setCode(type.getCode());
        readable.setAllowAddToCart(type.isAllowAddToCart());
        readable.setVisible(type.isVisible());
        if (LanguageCode.isLanguage(language)) {
            type.description(language).map(ProductTypeMapper::description).ifPresent(readable::setDescription);
        }
        if (allLanguages) {
            readable.setDescriptions(type.getDescriptions().stream().map(ProductTypeMapper::description).toList());
        }
        return readable;
    }

    public static void apply(PersistableProductType source, ProductType target) {
        target.setAllowAddToCart(source.isAllowAddToCart());
        target.setVisible(source.isVisible());
        Map<LanguageCode, ProductTypeDescription> existing = new HashMap<>();
        target.getDescriptions().forEach(d -> existing.put(d.getLanguageCode(), d));
        target.getDescriptions().clear();
        for (com.asrevo.cvhome.catalog.model.type.ProductTypeDescription d : source.getDescriptions()) {
            ProductTypeDescription entity = existing.getOrDefault(d.getLanguage(), new ProductTypeDescription(target));
            entity.setLanguageCode(d.getLanguage());
            entity.setName(d.getName());
            entity.setTitle(d.getTitle());
            entity.setDescription(d.getDescription());
            target.getDescriptions().add(entity);
        }
    }

    private static com.asrevo.cvhome.catalog.model.type.ProductTypeDescription description(
            ProductTypeDescription d) {
        var readable = new com.asrevo.cvhome.catalog.model.type.ProductTypeDescription();
        readable.setId(d.getId());
        readable.setLanguage(d.getLanguageCode());
        readable.setName(d.getName());
        readable.setTitle(d.getTitle());
        readable.setDescription(d.getDescription());
        return readable;
    }
}
