package com.asrevo.cvhome.catalog.service.mapper.catalog.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.description.ProductDescription;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPriceDescription;
import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductAttributeNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductOptionReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductOptionValueReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductAttributeMapper;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.catalog.services.product.type.ProductTypeService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;

@Component
public class PersistableProductDefinitionMapper implements Mapper<PersistableProductDefinition, Product> {

    private final CategoryService categoryService;

    private final PersistableProductAttributeMapper persistableProductAttributeMapper;

    private final ProductTypeService productTypeService;

    private final ManufacturerService manufacturerService;

    public PersistableProductDefinitionMapper(CategoryService categoryService,
                                              PersistableProductAttributeMapper persistableProductAttributeMapper,
                                              ProductTypeService productTypeService,
                                              ManufacturerService manufacturerService) {
        this.categoryService = categoryService;
        this.persistableProductAttributeMapper = persistableProductAttributeMapper;
        this.productTypeService = productTypeService;
        this.manufacturerService = manufacturerService;
    }

    @Override
    public Product convert(PersistableProductDefinition source, StoreMerchantId store, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException {
        Product product = new Product();
        return this.merge(source, product, store, language);
    }

    @Override
    public Product merge(PersistableProductDefinition source, Product destination, StoreMerchantId store,
                         LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException {

        try {

            // core properties

            if (StringUtils.isBlank(source.getIdentifier())) {
                destination.setSku(source.getSku());
            } else {
                destination.setSku(source.getIdentifier());
            }
            destination.setAvailable(source.isVisible());
            destination.setDateAvailable(Instant.now());

            destination.setRefSku(source.getIdentifier());

            if (source.getId() != null && source.getId() == 0) {
                destination.setId(null);
            } else {
                destination.setId(source.getId());
            }

            applyManufacturer(source, destination, store);
            applyType(source, destination, store, language);

            if (Objects.nonNull(source.getDateAvailable())) {
                destination.setDateAvailable(source.getDateAvailable());
            }

            destination.setStore(store);

            List<LanguageCode> languages = applyDescriptions(source, destination);

            applyAvailabilityAndPrice(source, destination, store, languages);

            applySpecifications(source, destination, store);

            destination.setSortOrder(source.getSortOrder());
            destination.setProductVirtual(source.isVirtual());
            destination.setProductShipeable(source.isShipeable());

            applyAttributes(source, destination, store, language);
            applyCategories(source, destination, store);

            return destination;

        } catch (ConversionException e) {
            // Already names its condition and its offending field; re-wrapping would bury both.
            throw e;
        } catch (Exception e) {
            throw ProductNotConvertibleException.of(e);
        }
    }

    private void applyManufacturer(PersistableProductDefinition source, Product destination, StoreMerchantId store)
            throws ManufacturerReferenceUnresolvableException {
        if (StringUtils.isBlank(source.getManufacturer())) {
            return;
        }
        Manufacturer manufacturer = manufacturerService.getByCode(store, source.getManufacturer());
        if (manufacturer == null) {
            throw ManufacturerReferenceUnresolvableException.of(source.getManufacturer(), store);
        }
        destination.setManufacturer(manufacturer);
    }

    private void applyType(PersistableProductDefinition source, Product destination, StoreMerchantId store,
                           LanguageCode language) throws ProductTypeReferenceUnresolvableException {
        if (StringUtils.isBlank(source.getType())) {
            return;
        }
        ProductType type = productTypeService.getByCode(source.getType(), store, language);
        if (type == null) {
            throw ProductTypeReferenceUnresolvableException.of(source.getType(), store);
        }

        destination.setType(type);
    }

    private List<LanguageCode> applyDescriptions(PersistableProductDefinition source, Product destination) {
        List<LanguageCode> languages = new ArrayList<>();
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

                languages.add(description.getLanguage());
                productDescription.setLanguageCode(description.getLanguage());
                descriptions.add(productDescription);
            }
        }

        if (!descriptions.isEmpty()) {
            destination.setDescriptions(descriptions);
        }
        return languages;
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

    private void applyAvailabilityAndPrice(PersistableProductDefinition source, Product destination, StoreMerchantId store,
                                           List<LanguageCode> languages) {
        AvailabilityAndPrice resolved = resolveExistingAvailabilityAndPrice(destination, source);
        ProductAvailability productAvailability = resolved.availability();
        ProductPrice defaultPrice = resolved.price();

        if (productAvailability == null) { // create with default values
            productAvailability = new ProductAvailability(destination, store);
            destination.getAvailabilities().add(productAvailability);

            productAvailability.setProductQuantity(source.getQuantity());
            productAvailability.setProductQuantityOrderMin(1);
            productAvailability.setProductQuantityOrderMax(1);
            productAvailability.setRegion(Constants.ALL_REGIONS);
            productAvailability.setAvailable(destination.isAvailable());
            productAvailability.setProductStatus(source.isCanBePurchased());
        }

        if (defaultPrice == null) {
            createDefaultPrice(source, productAvailability, languages);
        }
    }

    private AvailabilityAndPrice resolveExistingAvailabilityAndPrice(Product destination,
                                                                     PersistableProductDefinition source) {
        if (CollectionUtils.isEmpty(destination.getAvailabilities())) {
            return new AvailabilityAndPrice(null, null);
        }
        for (ProductAvailability avail : destination.getAvailabilities()) {
            for (ProductPrice p : avail.getPrices()) {
                if (p.isDefaultPrice()) {
                    avail.setProductQuantity(source.getQuantity());
                    avail.setProductStatus(source.isCanBePurchased());
                    p.setProductPriceAmount(source.getPrice());
                    return new AvailabilityAndPrice(avail, p);
                }
            }
        }
        return new AvailabilityAndPrice(null, null);
    }

    private void createDefaultPrice(PersistableProductDefinition source, ProductAvailability productAvailability,
                                    List<LanguageCode> languages) {
        BigDecimal defaultPriceAmount = new BigDecimal(0);
        if (source.getPrice() != null) {
            defaultPriceAmount = source.getPrice();
        }

        ProductPrice defaultPrice = new ProductPrice();
        defaultPrice.setDefaultPrice(true);
        defaultPrice.setProductPriceAmount(defaultPriceAmount);
        defaultPrice.setCode(Constants.DEFAULT_PRICE_CODE);
        defaultPrice.setProductAvailability(productAvailability);
        productAvailability.getPrices().add(defaultPrice);
        for (LanguageCode lang : languages) {
            ProductPriceDescription ppd = new ProductPriceDescription();
            ppd.setProductPrice(defaultPrice);
            ppd.setLanguageCode(lang);
            ppd.setName(Constants.DEFAULT_PRICE_DESCRIPTION);
            defaultPrice.getDescriptions().add(ppd);
        }
    }

    private void applySpecifications(PersistableProductDefinition source, Product destination, StoreMerchantId store)
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

        Manufacturer manuf = null;
        if (!StringUtils.isBlank(source.getProductSpecifications().getManufacturer())) {
            manuf = manufacturerService.getByCode(store, source.getProductSpecifications().getManufacturer());
        }

        if (manuf == null || !Objects.equals(manuf.getStoreMerchantId(), store)) {
            throw ManufacturerReferenceUnresolvableException.of(
                    source.getProductSpecifications().getManufacturer(), store);
        }
        destination.setManufacturer(manuf);
    }

    private void applyAttributes(PersistableProductDefinition source, Product destination, StoreMerchantId store,
                                 LanguageCode language)
            throws ProductOptionReferenceUnresolvableException, ProductOptionValueReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductAttributeNotConvertibleException,
            ProductOptionNotConvertibleException {
        if (source.getProperties() == null) {
            return;
        }
        for (PersistableProductAttribute attr : source.getProperties()) {
            ProductAttribute attribute = persistableProductAttributeMapper.convert(attr, store, language);

            attribute.setProduct(destination);
            destination.getAttributes().add(attribute);
        }
    }

    private void applyCategories(PersistableProductDefinition source, Product destination, StoreMerchantId store)
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

    private record AvailabilityAndPrice(ProductAvailability availability, ProductPrice price) {
    }

}
