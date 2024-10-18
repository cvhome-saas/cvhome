package com.asrevo.cvhome.store.service.mapper.catalog.product;

import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantImage;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup.ReadableProductVariantGroup;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ReadableProductVariantGroupMapper
        implements Mapper<ProductVariantGroup, ReadableProductVariantGroup> {

    private final ReadableProductVariantMapper readableProductVariantMapper;

    private final ImageFilePath imageUtils;

    public ReadableProductVariantGroupMapper(
            ReadableProductVariantMapper readableProductVariantMapper, ImageFilePath imageUtils) {
        this.readableProductVariantMapper = readableProductVariantMapper;
        this.imageUtils = imageUtils;
    }

    @Override
    public ReadableProductVariantGroup convert(
            ProductVariantGroup source, MerchantStore store, Language language) {
        Assert.notNull(source, "productVariantGroup cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        return this.merge(source, new ReadableProductVariantGroup(), store, language);
    }

    @Override
    public ReadableProductVariantGroup merge(
            ProductVariantGroup source,
            ReadableProductVariantGroup destination,
            MerchantStore store,
            Language language) {
        Assert.notNull(source, "productVariantGroup cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        if (destination == null) {
            destination = new ReadableProductVariantGroup();
        }

        destination.setId(source.getId());

        Set<ProductVariant> instances = source.getProductVariants();
        destination.setProductVariants(
                instances.stream()
                        .map(i -> this.instance(i, store, language))
                        .collect(Collectors.toList()));

        // image id should be unique in the list

        Map<Long, ReadableImage> finalList = new HashMap<>();

        List<ReadableImage> originalList =
                source.getImages().stream()
                        .map(i -> this.image(finalList, i, store, language))
                        .toList();

        destination.setImages(new ArrayList<>(finalList.values()));

        return destination;
    }

    private ReadableProductVariant instance(
            ProductVariant instance, MerchantStore store, Language language) {

        return readableProductVariantMapper.convert(instance, store, language);
    }

    private ReadableImage image(
            Map<Long, ReadableImage> finalList,
            ProductVariantImage img,
            MerchantStore store,
            Language language) {
        ReadableImage readable = null;
        if (!finalList.containsKey(img.getId())) {
            readable = new ReadableImage();
            readable.setId(img.getId());
            readable.setImageName(img.getProductImage());
            readable.setImageUrl(
                    imageUtils.buildCustomTypeImageUtils(
                            store, img.getProductImage(), FileContentType.VARIANT));
            readable.setDefaultImage(img.isDefaultImage());
            finalList.put(img.getId(), readable);
        }
        return readable;
    }
}
