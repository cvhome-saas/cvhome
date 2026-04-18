package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variantGroup.ReadableProductVariantGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;

@Component
public class ReadableProductVariantGroupMapper implements Mapper<ProductVariantGroup, ReadableProductVariantGroup> {

    private final ReadableProductVariantMapper readableProductVariantMapper;

    private final ImageFilePath imageUtils;

    public ReadableProductVariantGroupMapper(ReadableProductVariantMapper readableProductVariantMapper,
                                             ImageFilePath imageUtils) {
        this.readableProductVariantMapper = readableProductVariantMapper;
        this.imageUtils = imageUtils;
    }

    @Override
    public ReadableProductVariantGroup convert(ProductVariantGroup source, StoreMerchantId store,
                                               LanguageCode language) {
        Assert.notNull(source, "productVariantGroup cannot be null");
        Assert.notNull(store, "store cannot be null");
        Assert.notNull(language, "Language cannot be null");
        return this.merge(source, new ReadableProductVariantGroup(), store, language);
    }

    @Override
    public ReadableProductVariantGroup merge(ProductVariantGroup source, ReadableProductVariantGroup destination,
                                             StoreMerchantId store, LanguageCode language) {
        Assert.notNull(source, "productVariantGroup cannot be null");
        Assert.notNull(store, "store cannot be null");
        Assert.notNull(language, "Language cannot be null");
        if (destination == null) {
            destination = new ReadableProductVariantGroup();
        }

        destination.setId(source.getId());

        Set<ProductVariant> instances = source.getProductVariants();
        destination.setProductVariants(
                instances.stream().map(i -> this.instance(i, store, language)).collect(Collectors.toList()));

        // image id should be unique in the list

        Map<Long, ReadableImage> finalList = new HashMap<>();

        List<ReadableImage> originalList = source.getImages()
                .stream()
                .map(i -> this.image(finalList, i, store, language))
                .toList();

        destination.setImages(new ArrayList<>(finalList.values()));

        return destination;
    }

    private ReadableProductVariant instance(ProductVariant instance, StoreMerchantId store, LanguageCode language) {

        return readableProductVariantMapper.convert(instance, store, language);
    }

    private ReadableImage image(Map<Long, ReadableImage> finalList, ProductVariantImage img, StoreMerchantId store,
                                LanguageCode language) {
        ReadableImage readable = null;
        if (!finalList.containsKey(img.getId())) {
            readable = new ReadableImage();
            readable.setId(img.getId());
            readable.setImageName(img.getProductImage());
            readable.setImageUrl(
                    imageUtils.buildCustomTypeImageUtils(store, img.getProductImage(), FileContentType.VARIANT));
            readable.setDefaultImage(img.isDefaultImage());
            finalList.put(img.getId(), readable);
        }
        return readable;
    }

}
