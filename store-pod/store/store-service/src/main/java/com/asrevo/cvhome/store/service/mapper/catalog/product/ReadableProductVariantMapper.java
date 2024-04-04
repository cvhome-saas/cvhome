package com.asrevo.cvhome.store.service.mapper.catalog.product;


import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantImage;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableProductVariationMapper;
import com.asrevo.cvhome.store.service.mapper.inventory.ReadableInventoryMapper;
import com.asrevo.cvhome.store.utils.DateUtil;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReadableProductVariantMapper implements Mapper<ProductVariant, ReadableProductVariant> {


    private final ReadableProductVariationMapper readableProductVariationMapper;

    private final ReadableInventoryMapper readableInventoryMapper;

    private final ImageFilePath imagUtils;

    public ReadableProductVariantMapper(ReadableProductVariationMapper readableProductVariationMapper, ReadableInventoryMapper readableInventoryMapper, @Qualifier("img") ImageFilePath imagUtils) {
        this.readableProductVariationMapper = readableProductVariationMapper;
        this.readableInventoryMapper = readableInventoryMapper;
        this.imagUtils = imagUtils;
    }

    @Override
    public ReadableProductVariant convert(ProductVariant source, MerchantStore store, Language language) {
        ReadableProductVariant readableproductVariant = new ReadableProductVariant();
        return this.merge(source, readableproductVariant, store, language);
    }

    @Override
    public ReadableProductVariant merge(ProductVariant source, ReadableProductVariant destination,
                                        MerchantStore store, Language language) {

        Assert.notNull(source, "Product instance cannot be null");
        Assert.notNull(source.getProduct(), "Product cannot be null");

        if (destination == null) {
            destination = new ReadableProductVariant();
        }

        destination.setSortOrder(source.getSortOrder() != null ? source.getSortOrder().intValue() : 0);
        destination.setAvailable(source.isAvailable());
        destination.setDateAvailable(DateUtil.formatDate(source.getDateAvailable()));
        destination.setId(source.getId());
        destination.setDefaultSelection(source.isDefaultSelection());
        destination.setProductId(source.getProduct().getId());
        destination.setSku(source.getSku());
        destination.setSortOrder(source.getSortOrder());
        destination.setCode(source.getCode());

        //get product
        Product baseProduct = source.getProduct();
        if (baseProduct == null) {
            throw new ResourceNotFoundException("Product instances do not include the parent product [" + destination.getSku() + "]");
        }

        destination.setProductShipeable(baseProduct.isProductShipeable());

        //destination.setStore(null);
        destination.setStore(store.getCode());
        destination.setVariation(readableProductVariationMapper.convert(source.getVariation(), store, language));
        if (source.getVariationValue() != null) {
            destination.setVariationValue(readableProductVariationMapper.convert(source.getVariationValue(), store, language));
        }

        if (source.getProductVariantGroup() != null) {
            Set<String> nameSet = new HashSet<>();
            List<ReadableImage> instanceImages = source.getProductVariantGroup().getImages().stream().map(i -> this.image(i, store, language))
                    .filter(e -> nameSet.add(e.getImageUrl()))
                    .collect(Collectors.toList());
            destination.setImages(instanceImages);
        }

        if (!CollectionUtils.isEmpty(source.getAvailabilities())) {
            List<ReadableInventory> inventories = source.getAvailabilities().stream().map(i -> readableInventoryMapper.convert(i, store, language)).collect(Collectors.toList());
            destination.setInventory(inventories);
        }

        return destination;
    }

    private ReadableImage image(ProductVariantImage instanceImage, MerchantStore store, Language language) {
        ReadableImage img = new ReadableImage();
        img.setDefaultImage(instanceImage.isDefaultImage());
        img.setId(instanceImage.getId());
        img.setImageName(instanceImage.getProductImage());
        img.setImageUrl(imagUtils.buildCustomTypeImageUtils(store, img.getImageName(), FileContentType.VARIANT));
        return img;
    }


}
