package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.PersistableImage;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProduct;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.catalog.services.product.type.ProductTypeService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.store.core.mapper.Mapper;

/**
 * Transforms a fully configured PersistableProduct to a Product. Inventory and variants left with the
 * catalog/inventory split.
 *
 * @author carlsamson
 */
@Component
public class PersistableProductMapper implements Mapper<PersistableProduct, Product> {

    private final CategoryService categoryService;

    private final ManufacturerService manufacturerService;

    private final ProductTypeService productTypeService;

    public PersistableProductMapper(CategoryService categoryService,
                                    ManufacturerService manufacturerService, ProductTypeService productTypeService) {
        this.categoryService = categoryService;
        this.manufacturerService = manufacturerService;
        this.productTypeService = productTypeService;
    }

    @Override
    public Product convert(PersistableProduct source, StoreMerchantId store, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException {
        Product product = new Product();
        return this.merge(source, product, store, language);
    }

    @Override
    public Product merge(PersistableProduct source, Product destination, StoreMerchantId store, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException {
        try {

            // core properties
            destination.setSku(source.getSku());

            destination.setAvailable(source.isVisible());
            destination.setDateAvailable(Instant.now());

            destination.setRefSku(source.getRefSku());

            if (source.getId() != null && source.getId() == 0) {
                destination.setId(null);
            } else {
                destination.setId(source.getId());
            }

            applySpecifications(source, destination, store);
            applyType(source, destination, store, language);

            if (Objects.nonNull(source.getDateAvailable())) {
                destination.setDateAvailable(source.getDateAvailable());
            }

            destination.setStore(store);

            applyDescriptions(source, destination);

            destination.setSortOrder(source.getSortOrder());
            destination.setProductVirtual(source.isProductVirtual());
            destination.setProductShipeable(source.isProductShipeable());
            if (source.getRating() != null) {
                destination.setProductReviewAvg(BigDecimal.valueOf(source.getRating()));
            }
            destination.setProductReviewCount(source.getRatingCount());

            applyCategories(source, destination, store);
            applyImages(source, destination);

            return destination;

        } catch (ConversionException e) {
            // Already names its condition and its offending field; re-wrapping would bury both.
            throw e;
        } catch (Exception e) {
            throw ProductNotConvertibleException.of(e);
        }
    }

    private void applySpecifications(PersistableProduct source, Product destination, StoreMerchantId store)
            throws ManufacturerReferenceUnresolvableException {
        if (source.getProductSpecifications() == null) {
            return;
        }
        destination.setProductHeight(source.getProductSpecifications().getHeight());
        destination.setProductLength(source.getProductSpecifications().getLength());
        destination.setProductWeight(source.getProductSpecifications().getWeight());
        destination.setProductWidth(source.getProductSpecifications().getWidth());

        if (source.getProductSpecifications().getManufacturer() == null) {
            return;
        }

        Manufacturer manufacturer = manufacturerService.getByCode(store,
                source.getProductSpecifications().getManufacturer());
        if (manufacturer == null) {
            throw ManufacturerReferenceUnresolvableException.of(
                    source.getProductSpecifications().getManufacturer(), store);
        }
        destination.setManufacturer(manufacturer);
    }

    private void applyType(PersistableProduct source, Product destination, StoreMerchantId store, LanguageCode language)
            throws ProductTypeReferenceUnresolvableException {
        if (StringUtils.isBlank(source.getType())) {
            return;
        }
        ProductType type = productTypeService.getByCode(source.getType(), store, language);
        if (type == null) {
            throw ProductTypeReferenceUnresolvableException.of(source.getType(), store);
        }
        destination.setType(type);
    }

    private void applyDescriptions(PersistableProduct source, Product destination) {
        Set<ProductDescription> descriptions = new HashSet<>();
        if (!CollectionUtils.isEmpty(source.getDescriptions())) {
            for (com.asrevo.cvhome.catalog.model.product.ProductDescription description : source.getDescriptions()) {
                ProductDescription productDescription = resolveProductDescription(destination, description);

                productDescription.setProduct(destination);
                productDescription.setDescription(description.getDescription());

                productDescription.setProductHighlight(description.getHighlights());

                productDescription.setName(description.getName());
                productDescription.setSeUrl(description.getFriendlyUrl());
                productDescription.setMetatagKeywords(description.getKeyWords());
                productDescription.setMetatagDescription(description.getMetaDescription());
                productDescription.setTitle(description.getTitle());

                productDescription.setLanguageCode(description.getLanguage());
                descriptions.add(productDescription);
            }
        }

        if (!descriptions.isEmpty()) {
            destination.setDescriptions(descriptions);
        }
    }

    private ProductDescription resolveProductDescription(Product destination,
                                                         com.asrevo.cvhome.catalog.model.product.ProductDescription description) {
        if (CollectionUtils.isEmpty(destination.getDescriptions())) {
            return new ProductDescription();
        }
        for (ProductDescription desc : destination.getDescriptions()) {
            if (desc.getLanguageCode().equals(description.getLanguage())) {
                return desc;
            }
        }
        return new ProductDescription();
    }

    private void applyCategories(PersistableProduct source, Product destination, StoreMerchantId store)
            throws CategoryReferenceUnresolvableException {
        if (CollectionUtils.isEmpty(source.getCategories())) {
            return;
        }
        for (com.asrevo.cvhome.catalog.model.category.Category categ : source.getCategories()) {
            Category c = resolveCategory(categ, store);
            if (!Objects.equals(c.getStoreMerchantId(), store)) {
                throw CategoryReferenceUnresolvableException.of(categ.getId(), store);
            }
            destination.getCategories().add(c);
        }
    }

    private Category resolveCategory(com.asrevo.cvhome.catalog.model.category.Category categ, StoreMerchantId store)
            throws CategoryReferenceUnresolvableException {
        boolean hasCode = !StringUtils.isBlank(categ.getCode());
        Category c = hasCode ? categoryService.getByCode(store, categ.getCode()) : categoryService.getById(categ.getId(), store);

        if (c != null) {
            return c;
        }
        throw CategoryReferenceUnresolvableException.of(hasCode ? categ.getCode() : categ.getId(), store);
    }

    private void applyImages(PersistableProduct source, Product destination) {
        if (CollectionUtils.isEmpty(source.getImages())) {
            return;
        }
        for (PersistableImage img : source.getImages()) {
            ProductImage productImage = new ProductImage();
            productImage.setImageType(img.getImageType());
            productImage.setDefaultImage(img.isDefaultImage());
            if (img.getImageType() == 1) { // external url
                productImage.setProductImageUrl(img.getImageUrl());
                productImage.setImage(new ByteArrayInputStream(new byte[0]));
            } else {
                ByteArrayInputStream in = new ByteArrayInputStream(img.getBytes());
                productImage.setImage(in);
            }
            productImage.setProduct(destination);
            productImage.setProductImage(img.getName());

            destination.getImages().add(productImage);
        }
    }

}
