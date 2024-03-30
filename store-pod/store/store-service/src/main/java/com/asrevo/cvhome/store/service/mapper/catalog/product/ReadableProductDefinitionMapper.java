package com.asrevo.cvhome.store.service.mapper.catalog.product;

import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.description.ProductDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.catalog.product.product.ProductSpecification;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.ReadableProductDefinitionFull;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import com.asrevo.cvhome.store.core.model.references.DimensionUnitOfMeasure;
import com.asrevo.cvhome.store.core.model.references.WeightUnitOfMeasure;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableCategoryMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableManufacturerMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableProductTypeMapper;
import com.asrevo.cvhome.store.service.mapper.inventory.ReadableInventoryMapper;
import com.asrevo.cvhome.store.utils.DateUtil;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReadableProductDefinitionMapper implements Mapper<Product, ReadableProductDefinition> {

    @Autowired
    private ReadableCategoryMapper readableCategoryMapper;

    @Autowired
    private ReadableProductTypeMapper readableProductTypeMapper;

    @Autowired
    private ReadableManufacturerMapper readableManufacturerMapper;

    @Autowired
    private ReadableInventoryMapper readableInventoryMapper;

    @Autowired
    @Qualifier("img")
    private ImageFilePath imageUtils;

    @Override
    public ReadableProductDefinition convert(Product source, MerchantStore store, Language language) {
        ReadableProductDefinition target = new ReadableProductDefinition();
        return this.merge(source, target, store, language);
    }

    @Override
    public ReadableProductDefinition merge(Product source, ReadableProductDefinition destination, MerchantStore store,
                                           Language language) {
        Assert.notNull(source, "Product cannot be null");
        Assert.notNull(destination, "Product destination cannot be null");

        ReadableProductDefinition returnDestination = destination;
        if (language == null) {
            returnDestination = new ReadableProductDefinitionFull();
        }

        List<com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription> fulldescriptions = new ArrayList<com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription>();

        returnDestination.setIdentifier(source.getSku());
        returnDestination.setId(source.getId());
        returnDestination.setVisible(source.isAvailable());
        returnDestination.setDateAvailable(DateUtil.formatDate(source.getDateAvailable()));
        returnDestination.setSku(source.getSku());
        ProductDescription description = null;
        if (source.getDescriptions() != null && source.getDescriptions().size() > 0) {
            for (ProductDescription desc : source.getDescriptions()) {
                if (language != null && desc.getLanguage() != null
                        && desc.getLanguage().getId().intValue() == language.getId().intValue()) {
                    description = desc;
                    break;
                } else {
                    fulldescriptions.add(populateDescription(desc));
                }
            }
        }

/*		if (source.getProductReviewAvg() != null) {
			double avg = source.getProductReviewAvg().doubleValue();
			double rating = Math.round(avg * 2) / 2.0f;
			returnDestination.setRating(rating);
		}

		if (source.getProductReviewCount() != null) {
			returnDestination.setRatingCount(source.getProductReviewCount().intValue());
		}*/

        if (description != null) {
            com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription tragetDescription = populateDescription(
                    description);
            returnDestination.setDescription(tragetDescription);

        }

        if (source.getManufacturer() != null) {
            ReadableManufacturer manufacturer = readableManufacturerMapper.convert(source.getManufacturer(), store,
                    language);
            returnDestination.setManufacturer(manufacturer);
        }

        if (!CollectionUtils.isEmpty(source.getCategories())) {
            List<ReadableCategory> categoryList = new ArrayList<ReadableCategory>();
            for (Category category : source.getCategories()) {
                ReadableCategory readableCategory = readableCategoryMapper.convert(category, store, language);
                categoryList.add(readableCategory);

            }
            returnDestination.setCategories(categoryList);
        }


        ProductSpecification specifications = new ProductSpecification();
        specifications.setHeight(source.getProductHeight());
        specifications.setLength(source.getProductLength());
        specifications.setWeight(source.getProductWeight());
        specifications.setWidth(source.getProductWidth());
        if (!StringUtils.isBlank(store.getSeizeunitcode())) {
            specifications.setDimensionUnitOfMeasure(DimensionUnitOfMeasure.valueOf(store.getSeizeunitcode().toLowerCase()));
        }
        if (!StringUtils.isBlank(store.getWeightunitcode())) {
            specifications.setWeightUnitOfMeasure(WeightUnitOfMeasure.valueOf(store.getWeightunitcode().toLowerCase()));
        }
        returnDestination.setProductSpecifications(specifications);

        if (source.getType() != null) {
            ReadableProductType readableType = readableProductTypeMapper.convert(source.getType(), store, language);
            returnDestination.setType(readableType);
        }

        returnDestination.setSortOrder(source.getSortOrder());

        //images
        Set<ProductImage> images = source.getImages();
        if (CollectionUtils.isNotEmpty(images)) {

            List<ReadableImage> imageList = images.stream().map(i -> this.convertImage(source, i, store)).collect(Collectors.toList());
            returnDestination.setImages(imageList);
        }

        //quantity
        ProductAvailability availability = null;
        for (ProductAvailability a : source.getAvailabilities()) {
            availability = a;
            if (a.getProductVariant() != null) {
                continue;
            }
        }

        if (availability != null) {
            returnDestination.setCanBePurchased(availability.isProductStatus());
            ReadableInventory inventory = readableInventoryMapper.convert(availability, store, language);
            returnDestination.setInventory(inventory);
        }


        if (returnDestination instanceof ReadableProductDefinitionFull) {
            ((ReadableProductDefinitionFull) returnDestination).setDescriptions(fulldescriptions);
        }


        return returnDestination;
    }

    private ReadableImage convertImage(Product product, ProductImage image, MerchantStore store) {
        ReadableImage prdImage = new ReadableImage();
        prdImage.setImageName(image.getProductImage());
        prdImage.setDefaultImage(image.isDefaultImage());

        StringBuilder imgPath = new StringBuilder();
        imgPath.append(imageUtils.getContextPath()).append(imageUtils.buildProductImageUtils(store, product.getSku(), image.getProductImage()));

        prdImage.setImageUrl(imgPath.toString());
        prdImage.setId(image.getId());
        prdImage.setImageType(image.getImageType());
        if (image.getProductImageUrl() != null) {
            prdImage.setExternalUrl(image.getProductImageUrl());
        }
        if (image.getImageType() == 1 && image.getProductImageUrl() != null) {//video
            prdImage.setVideoUrl(image.getProductImageUrl());
        }

        if (prdImage.isDefaultImage()) {
            prdImage.setDefaultImage(true);
        }

        return prdImage;
    }

    private com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription populateDescription(ProductDescription description) {
        if (description == null) {
            return null;
        }

        com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription tragetDescription = new com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription();
        tragetDescription.setFriendlyUrl(description.getSeUrl());
        tragetDescription.setName(description.getName());
        tragetDescription.setId(description.getId());
        if (!StringUtils.isBlank(description.getMetatagTitle())) {
            tragetDescription.setTitle(description.getMetatagTitle());
        } else {
            tragetDescription.setTitle(description.getName());
        }
        tragetDescription.setMetaDescription(description.getMetatagDescription());
        tragetDescription.setDescription(description.getDescription());
        tragetDescription.setHighlights(description.getProductHighlight());
        tragetDescription.setLanguage(description.getLanguage().getCode());
        tragetDescription.setKeyWords(description.getMetatagKeywords());

        if (description.getLanguage() != null) {
            tragetDescription.setLanguage(description.getLanguage().getCode());
        }
        return tragetDescription;
    }

}
