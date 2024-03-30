package com.asrevo.cvhome.store.service.mapper.catalog.product;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.services.catalog.category.CategoryService;
import com.asrevo.cvhome.store.core.services.catalog.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.store.core.services.catalog.product.type.ProductTypeService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.description.ProductDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPriceDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.service.mapper.catalog.PersistableProductAttributeMapper;
import com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceEntity;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.utils.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;

@Component
public class PersistableProductDefinitionMapper implements Mapper<PersistableProductDefinition, Product> {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private PersistableProductAttributeMapper persistableProductAttributeMapper;

    @Autowired
    private ProductTypeService productTypeService;

    @Autowired
    private ManufacturerService manufacturerService;

    @Override
    public Product convert(PersistableProductDefinition source, MerchantStore store, Language language) {
        Product product = new Product();
        return this.merge(source, product, store, language);
    }

    @Override
    public Product merge(PersistableProductDefinition source, Product destination, MerchantStore store,
                         Language language) {


        Assert.notNull(destination, "Product must not be null");

        try {

            //core properties

            if (StringUtils.isBlank(source.getIdentifier())) {
                destination.setSku(source.getSku());
            } else {
                destination.setSku(source.getIdentifier());
            }
            destination.setAvailable(source.isVisible());
            destination.setDateAvailable(new Date());

            destination.setRefSku(source.getIdentifier());


            if (source.getId() != null && source.getId().longValue() == 0) {
                destination.setId(null);
            } else {
                destination.setId(source.getId());
            }

            //MANUFACTURER
            if (!StringUtils.isBlank(source.getManufacturer())) {
                Manufacturer manufacturer = manufacturerService.getByCode(store, source.getManufacturer());
                if (manufacturer == null) {
                    throw new ConversionException("Manufacturer [" + source.getManufacturer() + "] does not exist");
                }
                destination.setManufacturer(manufacturer);
            }


            //PRODUCT TYPE
            if (!StringUtils.isBlank(source.getType())) {
                ProductType type = productTypeService.getByCode(source.getType(), store, language);
                if (type == null) {
                    throw new ConversionException("Product type [" + source.getType() + "] does not exist");
                }

                destination.setType(type);
            }


            if (!StringUtils.isBlank(source.getDateAvailable())) {
                destination.setDateAvailable(DateUtil.getDate(source.getDateAvailable()));
            }


            destination.setMerchantStore(store);

            List<Language> languages = new ArrayList<Language>();
            Set<ProductDescription> descriptions = new HashSet<ProductDescription>();
            if (!CollectionUtils.isEmpty(source.getDescriptions())) {
                for (com.asrevo.cvhome.store.core.model.catalog.product.ProductDescription description : source.getDescriptions()) {

                    ProductDescription productDescription = new ProductDescription();
                    Language lang = languageService.getByCode(description.getLanguage());
                    if (lang == null) {
                        throw new ConversionException("Language code " + description.getLanguage() + " is invalid, use ISO code (en, fr ...)");
                    }
                    if (!CollectionUtils.isEmpty(destination.getDescriptions())) {
                        for (ProductDescription desc : destination.getDescriptions()) {
                            if (desc.getLanguage().getCode().equals(description.getLanguage())) {
                                productDescription = desc;
                                break;
                            }
                        }
                    }

                    productDescription.setProduct(destination);
                    productDescription.setDescription(description.getDescription());

                    productDescription.setProductHighlight(description.getHighlights());

                    productDescription.setName(description.getName());
                    productDescription.setSeUrl(description.getFriendlyUrl());
                    productDescription.setMetatagKeywords(description.getKeyWords());
                    productDescription.setMetatagDescription(description.getMetaDescription());
                    productDescription.setTitle(description.getTitle());

                    languages.add(lang);
                    productDescription.setLanguage(lang);
                    descriptions.add(productDescription);
                }
            }

            if (descriptions.size() > 0) {
                destination.setDescriptions(descriptions);
            }

            /**
             * Product definition
             */
            ProductAvailability productAvailability = null;
            ProductPrice defaultPrice = null;
            if (!CollectionUtils.isEmpty(destination.getAvailabilities())) {
                for (ProductAvailability avail : destination.getAvailabilities()) {
                    Set<ProductPrice> prices = avail.getPrices();
                    for (ProductPrice p : prices) {
                        if (p.isDefaultPrice()) {
                            if (productAvailability == null) {
                                productAvailability = avail;
                                defaultPrice = p;
                                productAvailability.setProductQuantity(source.getQuantity());
                                productAvailability.setProductStatus(source.isCanBePurchased());
                                p.setProductPriceAmount(source.getPrice());
                                break;
                            }
                        }
                    }
                }
            }

            if (productAvailability == null) { //create with default values
                productAvailability = new ProductAvailability(destination, store);
                destination.getAvailabilities().add(productAvailability);

                productAvailability.setProductQuantity(source.getQuantity());
                productAvailability.setProductQuantityOrderMin(1);
                productAvailability.setProductQuantityOrderMax(1);
                productAvailability.setRegion(Constants.ALL_REGIONS);
                productAvailability.setAvailable(Boolean.valueOf(destination.isAvailable()));
                productAvailability.setProductStatus(source.isCanBePurchased());
            }


            if (defaultPrice == null) {

                BigDecimal defaultPriceAmount = new BigDecimal(0);
                if (source.getPrice() != null) {
                    defaultPriceAmount = source.getPrice();
                }

                defaultPrice = new ProductPrice();
                defaultPrice.setDefaultPrice(true);
                defaultPrice.setProductPriceAmount(defaultPriceAmount);
                defaultPrice.setCode(ProductPriceEntity.DEFAULT_PRICE_CODE);
                defaultPrice.setProductAvailability(productAvailability);
                productAvailability.getPrices().add(defaultPrice);
                for (Language lang : languages) {

                    ProductPriceDescription ppd = new ProductPriceDescription();
                    ppd.setProductPrice(defaultPrice);
                    ppd.setLanguage(lang);
                    ppd.setName(Constants.DEFAULT_PRICE_DESCRIPTION);
                    defaultPrice.getDescriptions().add(ppd);
                }
            }

            if (source.getProductSpecifications() != null) {
                destination.setProductHeight(source.getProductSpecifications().getHeight());
                destination.setProductLength(source.getProductSpecifications().getLength());
                destination.setProductWeight(source.getProductSpecifications().getWeight());
                destination.setProductWidth(source.getProductSpecifications().getWidth());


                if (source.getProductSpecifications().getManufacturer() != null) {

                    Manufacturer manuf = null;
                    if (!StringUtils.isBlank(source.getProductSpecifications().getManufacturer())) {
                        manuf = manufacturerService.getByCode(store, source.getProductSpecifications().getManufacturer());
                    }

                    if (manuf == null) {
                        throw new ConversionException("Invalid manufacturer id");
                    }
                    if (manuf != null) {
                        if (manuf.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                            throw new ConversionException("Invalid manufacturer id");
                        }
                        destination.setManufacturer(manuf);
                    }
                }

            }
            destination.setSortOrder(source.getSortOrder());
            destination.setProductVirtual(source.isVirtual());
            destination.setProductShipeable(source.isShipeable());


            //attributes
            if (source.getProperties() != null) {
                for (PersistableProductAttribute attr : source.getProperties()) {
                    ProductAttribute attribute = persistableProductAttributeMapper.convert(attr, store, language);

                    attribute.setProduct(destination);
                    destination.getAttributes().add(attribute);

                }
            }


            //categories
            if (!CollectionUtils.isEmpty(source.getCategories())) {
                for (com.asrevo.cvhome.store.core.model.catalog.category.Category categ : source.getCategories()) {

                    Category c = null;
                    if (!StringUtils.isBlank(categ.getCode())) {
                        c = categoryService.getByCode(store, categ.getCode());
                    } else {
                        Assert.notNull(categ.getId(), "Category id nust not be null");
                        c = categoryService.getById(categ.getId(), store.getId());
                    }

                    if (c == null) {
                        if (!StringUtils.isBlank(categ.getCode())) {
                            throw new ConversionException("Category code " + categ.getCode() + " does not exist");
                        } else {
                            throw new ConversionException("Category id " + categ.getId() + " does not exist");

                        }
                    }
                    if (c.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                        throw new ConversionException("Invalid category id");
                    }
                    destination.getCategories().add(c);
                }
            }
            return destination;

        } catch (Exception e) {
            throw new ConversionRuntimeException("Error converting product mapper", e);
        }
    }

}
