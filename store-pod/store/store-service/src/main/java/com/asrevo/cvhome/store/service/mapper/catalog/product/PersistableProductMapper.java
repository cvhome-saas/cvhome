package com.asrevo.cvhome.store.service.mapper.catalog.product;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.description.ProductDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.PersistableImage;
import com.asrevo.cvhome.store.core.model.catalog.product.product.PersistableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.store.core.services.catalog.category.CategoryService;
import com.asrevo.cvhome.store.core.services.catalog.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.store.core.services.catalog.product.type.ProductTypeService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.utils.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Transforms a fully configured PersistableProduct
 * to a Product with inventory and Variants if any
 *
 * @author carlsamson
 */


@Component
public class PersistableProductMapper implements Mapper<PersistableProduct, Product> {


    private final PersistableProductAvailabilityMapper persistableProductAvailabilityMapper;

    private final PersistableProductVariantMapper persistableProductVariantMapper;


    private final CategoryService categoryService;
    private final LanguageService languageService;


    private final ManufacturerService manufacturerService;

    private final ProductTypeService productTypeService;

    public PersistableProductMapper(PersistableProductAvailabilityMapper persistableProductAvailabilityMapper, PersistableProductVariantMapper persistableProductVariantMapper, CategoryService categoryService, LanguageService languageService, ManufacturerService manufacturerService, ProductTypeService productTypeService) {
        this.persistableProductAvailabilityMapper = persistableProductAvailabilityMapper;
        this.persistableProductVariantMapper = persistableProductVariantMapper;
        this.categoryService = categoryService;
        this.languageService = languageService;
        this.manufacturerService = manufacturerService;
        this.productTypeService = productTypeService;
    }

    @Override
    public Product convert(PersistableProduct source, MerchantStore store, Language language) {
        Product product = new Product();
        return this.merge(source, product, store, language);
    }

    @Override
    public Product merge(PersistableProduct source, Product destination, MerchantStore store, Language language) {


        Assert.notNull(destination, "Product must not be null");

        try {

            //core properties
            destination.setSku(source.getSku());

            destination.setAvailable(source.isVisible());
            destination.setDateAvailable(new Date());

            destination.setRefSku(source.getRefSku());


            if (source.getId() != null && source.getId().longValue() == 0) {
                destination.setId(null);
            } else {
                destination.setId(source.getId());
            }


            /**
             * SPEIFICATIONS
             */
            if (source.getProductSpecifications() != null) {
                destination.setProductHeight(source.getProductSpecifications().getHeight());
                destination.setProductLength(source.getProductSpecifications().getLength());
                destination.setProductWeight(source.getProductSpecifications().getWeight());
                destination.setProductWidth(source.getProductSpecifications().getWidth());

                /**
                 * BRANDING
                 */

                if (source.getProductSpecifications().getManufacturer() != null) {

                    Manufacturer manufacturer = manufacturerService.getByCode(store, source.getProductSpecifications().getManufacturer());
                    if (manufacturer == null) {
                        throw new ConversionException("Manufacturer [" + source.getProductSpecifications().getManufacturer() + "] does not exist");
                    }
                    destination.setManufacturer(manufacturer);
                }
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

            /**
             * descriptions
             */
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

            if (!descriptions.isEmpty()) {
                destination.setDescriptions(descriptions);
            }

            destination.setSortOrder(source.getSortOrder());
            destination.setProductVirtual(source.isProductVirtual());
            destination.setProductShipeable(source.isProductShipeable());
            if (source.getRating() != null) {
                destination.setProductReviewAvg(new BigDecimal(source.getRating()));
            }
            destination.setProductReviewCount(source.getRatingCount());


            /**
             * Category
             */

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

            /**
             * Variants
             */
            if (!CollectionUtils.isEmpty(source.getVariants())) {
                Set<ProductVariant> variants = source.getVariants().stream().map(v -> this.variant(destination, v, store, language)).collect(Collectors.toSet());

                destination.setVariants(variants);
            }

            /**
             * Default inventory
             */

            if (source.getInventory() != null) {
                ProductAvailability productAvailability = persistableProductAvailabilityMapper.convert(source.getInventory(), store, language);
                productAvailability.setProduct(destination);
                destination.getAvailabilities().add(productAvailability);
            } else {
                //need an inventory to create a Product
                if (!CollectionUtils.isEmpty(destination.getVariants())) {
                    ProductAvailability defaultAvailability = null;
                    for (ProductVariant variant : destination.getVariants()) {
                        defaultAvailability = this.defaultAvailability(new ArrayList<>(variant.getAvailabilities()));
                        if (defaultAvailability != null) {
                            break;
                        }
                    }

                    defaultAvailability.setProduct(destination);
                    destination.getAvailabilities().add(defaultAvailability);

                }
            }

            //images
            if (!CollectionUtils.isEmpty(source.getImages())) {
                for (PersistableImage img : source.getImages()) {
                    ProductImage productImage = new ProductImage();
                    productImage.setImageType(img.getImageType());
                    productImage.setDefaultImage(img.isDefaultImage());
                    if (img.getImageType() == 1) {//external url
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


            return destination;

        } catch (Exception e) {
            throw new ConversionRuntimeException("Error converting product mapper", e);
        }


    }

    private ProductVariant variant(Product product, PersistableProductVariant variant, MerchantStore store, Language language) {
        ProductVariant var = persistableProductVariantMapper.convert(variant, store, language);
        var.setProduct(product);
        return var;
    }

    private ProductAvailability defaultAvailability(List<ProductAvailability> availabilityList) {
        return availabilityList.stream().filter(a -> a.getRegion() != null && a.getRegion().equals(Constants.ALL_REGIONS)).findFirst().get();
    }


}
