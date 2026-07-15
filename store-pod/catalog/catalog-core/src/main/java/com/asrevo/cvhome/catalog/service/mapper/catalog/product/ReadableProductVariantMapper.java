package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductVariationMapper;
import com.asrevo.cvhome.catalog.service.mapper.inventory.ReadableInventoryMapper;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;

@Component
public class ReadableProductVariantMapper implements Mapper<ProductVariant, ReadableProductVariant> {

    private final ReadableProductVariationMapper readableProductVariationMapper;

    private final ReadableInventoryMapper readableInventoryMapper;

    private final ImageFilePath imagUtils;

    public ReadableProductVariantMapper(ReadableProductVariationMapper readableProductVariationMapper,
                                        ReadableInventoryMapper readableInventoryMapper, ImageFilePath imagUtils) {
        this.readableProductVariationMapper = readableProductVariationMapper;
        this.readableInventoryMapper = readableInventoryMapper;
        this.imagUtils = imagUtils;
    }

    @Override
    public ReadableProductVariant convert(ProductVariant source, StoreMerchantId store, LanguageCode language) {
        ReadableProductVariant readableproductVariant = new ReadableProductVariant();
        return this.merge(source, readableproductVariant, store, language);
    }

    @Override
    public ReadableProductVariant merge(ProductVariant source, ReadableProductVariant destination,
                                        StoreMerchantId store, LanguageCode language) {

        if (destination == null) {
            destination = new ReadableProductVariant();
        }

        destination.setSortOrder(source.getSortOrder() != null ? source.getSortOrder() : 0);
        destination.setAvailable(source.isAvailable());
        destination.setDateAvailable(source.getDateAvailable());
        destination.setId(source.getId());
        destination.setDefaultSelection(source.isDefaultSelection());
        destination.setProductId(source.getProduct().getId());
        destination.setSku(source.getSku());
        destination.setSortOrder(source.getSortOrder());
        destination.setCode(source.getCode());

        // get product
        Product baseProduct = source.getProduct();
        if (baseProduct == null) {
            throw new ResourceNotFoundException(
                    "Product instances do not include the parent product [" + destination.getSku() + "]");
        }

        destination.setProductShipeable(baseProduct.isProductShipeable());

        destination.setVariation(readableProductVariationMapper.convert(source.getVariation(), store, language));
        if (source.getVariationValue() != null) {
            destination
                    .setVariationValue(readableProductVariationMapper.convert(source.getVariationValue(), store, language));
        }

        if (source.getProductVariantGroup() != null) {
            Set<String> nameSet = new HashSet<>();
            List<ReadableImage> instanceImages = source.getProductVariantGroup()
                    .getImages()
                    .stream()
                    .map(i -> this.image(i, store))
                    .filter(e -> nameSet.add(e.getImageUrl()))
                    .toList();
            destination.setImages(instanceImages);
        }

        if (!CollectionUtils.isEmpty(source.getAvailabilities())) {
            List<ReadableInventory> inventories = source.getAvailabilities()
                    .stream()
                    .map(i -> readableInventoryMapper.convert(i, store, language))
                    .toList();
            destination.setInventory(inventories);
        }

        return destination;
    }

    private ReadableImage image(ProductVariantImage instanceImage, StoreMerchantId store) {
        ReadableImage img = new ReadableImage();
        img.setDefaultImage(instanceImage.isDefaultImage());
        img.setId(instanceImage.getId());
        img.setImageName(instanceImage.getProductImage());
        img.setImageUrl(imagUtils.buildCustomTypeImageUtils(store, img.getImageName(), FileContentType.VARIANT));
        return img;
    }

}
