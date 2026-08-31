package com.asrevo.cvhome.catalog.services.product;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.ProductSpecification;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductDefinition;
import com.asrevo.cvhome.catalog.services.category.CategoryMapper;
import com.asrevo.cvhome.catalog.services.image.ImageMapper;
import com.asrevo.cvhome.catalog.services.manufacturer.ManufacturerMapper;
import com.asrevo.cvhome.catalog.services.type.ProductTypeMapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.store.model.references.DimensionUnitOfMeasure;
import com.asrevo.cvhome.store.model.references.WeightUnitOfMeasure;

import lombok.RequiredArgsConstructor;

/**
 * Entity to the three product shapes and back. Images need the CDN path and the specification needs the store's
 * units, which is why this one is a bean rather than static.
 */
@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ImageMapper imageMapper;

    private final ExternalMerchantStoreService merchantStoreService;

    /**
     * What a cart line or a merchandising strip shows: copy in one language, images, the box.
     */
    public ReadableMinimalProduct toMinimal(Product product, LanguageCode language) {
        return fill(new ReadableMinimalProduct(), product, language);
    }

    /**
     * The listing and product-page shape: minimal plus brand, type and categories.
     */
    public ReadableProduct toReadable(Product product, LanguageCode language) {
        ReadableProduct readable = fill(new ReadableProduct(), product, language);
        readable.setCreationDate(product.getAuditSection().getDateCreated());
        if (product.getManufacturer() != null) {
            readable.setManufacturer(ManufacturerMapper.toReadable(product.getManufacturer(), language, false));
        }
        if (product.getType() != null) {
            readable.setType(ProductTypeMapper.toReadable(product.getType(), language, false));
        }
        readable.setCategories(product.getCategories().stream()
                .map(c -> CategoryMapper.toReadable(c, language, false)).toList());
        return readable;
    }

    /**
     * The console's editable view: every language, and the related records in full.
     */
    public ReadableProductDefinition toDefinition(Product product, LanguageCode language) {
        ReadableProductDefinition definition = new ReadableProductDefinition();
        definition.setId(product.getId());
        product.defaultVariant().ifPresent(variant -> {
            definition.setSku(variant.getSku());
            definition.setIdentifier(variant.getSku());
        });
        definition.setVisible(product.isAvailable());
        definition.setShipeable(product.isProductShipeable());
        definition.setVirtual(product.isProductVirtual());
        definition.setDateAvailable(product.getDateAvailable());
        definition.setSortOrder(product.getSortOrder());
        definition.setProductSpecifications(specification(product));
        if (product.getManufacturer() != null) {
            definition.setManufacturer(ManufacturerMapper.toReadable(product.getManufacturer(), language, true));
        }
        if (product.getType() != null) {
            definition.setType(ProductTypeMapper.toReadable(product.getType(), language, true));
        }
        definition.setCategories(product.getCategories().stream()
                .map(c -> CategoryMapper.toReadable(c, language, true)).toList());
        product.description(language).map(ProductMapper::description).ifPresent(definition::setDescription);
        definition.setDescriptions(product.getDescriptions().stream().map(ProductMapper::description).toList());
        definition.setImages(imageMapper.toReadable(product));
        return definition;
    }

    /**
     * Copies the editable fields of the body onto the entity. Relations (brand, type, categories) are resolved by
     * the service; descriptions are merged by language so ids survive an edit.
     */
    public static void apply(PersistableProductDefinition source, Product target) {
        target.setAvailable(source.isVisible());
        target.setProductShipeable(source.isShipeable());
        target.setProductVirtual(source.isVirtual());
        target.setSortOrder(source.getSortOrder());
        if (source.getDateAvailable() != null) {
            target.setDateAvailable(source.getDateAvailable());
        }
        ProductSpecification spec = source.getProductSpecifications();
        if (spec != null) {
            target.setHeight(spec.getHeight());
            target.setLength(spec.getLength());
            target.setWidth(spec.getWidth());
            target.setWeight(spec.getWeight());
        }
        Map<LanguageCode, ProductDescription> existing = new HashMap<>();
        target.getDescriptions().forEach(d -> existing.put(d.getLanguageCode(), d));
        target.getDescriptions().clear();
        for (com.asrevo.cvhome.catalog.model.product.ProductDescription d : source.getDescriptions()) {
            ProductDescription entity = existing.getOrDefault(d.getLanguage(), new ProductDescription(target));
            entity.setLanguageCode(d.getLanguage());
            entity.setName(d.getName());
            entity.setTitle(d.getTitle());
            entity.setMetaTitle(d.getTitle());
            entity.setDescription(d.getDescription());
            entity.setHighlight(d.getHighlights());
            entity.setSeUrl(d.getFriendlyUrl());
            entity.setMetaKeywords(d.getKeyWords());
            entity.setMetaDescription(d.getMetaDescription());
            target.getDescriptions().add(entity);
        }
    }

    private <T extends ReadableMinimalProduct> T fill(T readable, Product product, LanguageCode language) {
        readable.setId(product.getId());
        // The default variant's sku and the variant count come off the batched variants collection: a page of
        // products loads them in one IN query (@BatchSize), a single product in one small query.
        product.defaultVariant().ifPresent(variant -> readable.setSku(variant.getSku()));
        readable.setVariantCount(product.getVariants().size());
        readable.setAvailable(product.isAvailable());
        readable.setProductShipeable(product.isProductShipeable());
        readable.setProductVirtual(product.isProductVirtual());
        readable.setSortOrder(product.getSortOrder());
        readable.setDateAvailable(product.getDateAvailable());
        readable.setProductSpecifications(specification(product));
        product.description(language).map(ProductMapper::description).ifPresent(readable::setDescription);
        readable.setImages(imageMapper.toReadable(product));
        product.defaultImage().map(i -> imageMapper.toReadable(product, i)).ifPresent(readable::setImage);
        return readable;
    }

    private ProductSpecification specification(Product product) {
        ProductSpecification spec = new ProductSpecification();
        spec.setHeight(product.getHeight());
        spec.setLength(product.getLength());
        spec.setWidth(product.getWidth());
        spec.setWeight(product.getWeight());
        ReadableMerchantStore store = merchantStoreService.getStore(product.getStore());
        if (store.getDimension() != null) {
            spec.setDimensionUnitOfMeasure(DimensionUnitOfMeasure.valueOf(store.getDimension().name().toLowerCase()));
        }
        if (store.getWeight() != null) {
            spec.setWeightUnitOfMeasure(WeightUnitOfMeasure.valueOf(store.getWeight().name().toLowerCase()));
        }
        return spec;
    }

    private static com.asrevo.cvhome.catalog.model.product.ProductDescription description(ProductDescription d) {
        var readable = new com.asrevo.cvhome.catalog.model.product.ProductDescription();
        readable.setId(d.getId());
        readable.setLanguage(d.getLanguageCode());
        readable.setName(d.getName());
        readable.setTitle(d.getTitle() == null || d.getTitle().isBlank() ? d.getName() : d.getTitle());
        readable.setDescription(d.getDescription());
        readable.setHighlights(d.getHighlight());
        readable.setFriendlyUrl(d.getSeUrl());
        readable.setKeyWords(d.getMetaKeywords());
        readable.setMetaDescription(d.getMetaDescription());
        return readable;
    }
}
