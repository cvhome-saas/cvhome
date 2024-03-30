package com.asrevo.cvhome.store.service.mapper.inventory;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.availability.ProductAvailabilityService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.ProductPriceDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.catalog.product.PersistableProductPrice;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.utils.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

@Component
public class PersistableProductPriceMapper implements Mapper<PersistableProductPrice, ProductPrice> {

    @Autowired
    private LanguageService languageService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductAvailabilityService productAvailabilityService;

    @Override
    public ProductPrice convert(PersistableProductPrice source, MerchantStore store, Language language) {
        return merge(source, new ProductPrice(), store, language);
    }

    @Override
    public ProductPrice merge(PersistableProductPrice source, ProductPrice destination, MerchantStore store,
                              Language language) {

        Assert.notNull(source, "PersistableProductPrice cannot be null");
        Assert.notNull(source.getSku(), "Product sku cannot be null");

        try {
            if (destination == null) {
                destination = new ProductPrice();
            }

            destination.setId(source.getId());

            /**
             * Get product availability and verify the existing br-pa-1.0.0
             *
             * Cannot have multiple default price for the same product availability Default
             * price can be edited but cannot create new default price
             */

            ProductAvailability availability = null;

            if (isPositive(source.getProductAvailabilityId())) {
                Optional<ProductAvailability> avail = productAvailabilityService
                        .getById(source.getProductAvailabilityId(), store);
                if (avail.isEmpty()) {
                    throw new ConversionRuntimeException(
                            "Product availability with id [" + source.getProductAvailabilityId() + "] was not found");
                }
                availability = avail.get();

            } else {

                // get an existing product availability
                List<ProductAvailability> existing = productAvailabilityService.getBySku(source.getSku(), store);

                if (!CollectionUtils.isEmpty(existing)) {
                    // find default availability
                    Optional<ProductAvailability> avail = existing.stream()
                            .filter(a -> a.getRegion() != null && a.getRegion().equals(Constants.ALL_REGIONS))
                            .findAny();
                    if (avail.isPresent()) {
                        availability = avail.get();

                        // if default price exist for sku exit
                        if (source.isDefaultPrice()) {
                            Optional<ProductPrice> defaultPrice = availability.getPrices().stream()
                                    .filter(p -> p.isDefaultPrice()).findAny();
                            if (defaultPrice.isPresent()) {
                                //throw new ConversionRuntimeException(
                                //		"Default Price already exist for product with sku [" + source.getSku() + "]");
                                destination = defaultPrice.get();
                            }
                        }
                    }
                }

            }

            if (availability == null) {

                Product product = productService.getBySku(source.getSku(),
                        store, language);
                if (product == null) {
                    throw new ConversionRuntimeException("Product with sku [" + source.getSku()
                            + "] not found for MerchantStore [" + store.getCode() + "]");
                }

                availability = new ProductAvailability();
                availability.setProduct(product);
                availability.setRegion(Constants.ALL_REGIONS);
            }

            destination.setProductAvailability(availability);
            destination.setDefaultPrice(source.isDefaultPrice());
            destination.setProductPriceAmount(source.getPrice());
            destination.setCode(source.getCode());
            destination.setProductPriceSpecialAmount(source.getDiscountedPrice());
            if (source.getDiscountStartDate() != null) {
                Date startDate = DateUtil.getDate(source.getDiscountStartDate());

                destination.setProductPriceSpecialStartDate(startDate);
            }
            if (source.getDiscountEndDate() != null) {
                Date endDate = DateUtil.getDate(source.getDiscountEndDate());

                destination.setProductPriceSpecialEndDate(endDate);
            }
            availability.getPrices().add(destination);
            destination.setProductAvailability(availability);
            destination.setDescriptions(this.getProductPriceDescriptions(destination, source.getDescriptions(), store));


            destination.setDefaultPrice(source.isDefaultPrice());

        } catch (Exception e) {

            throw new ConversionRuntimeException(e);
        }
        return destination;
    }

    private Set<ProductPriceDescription> getProductPriceDescriptions(ProductPrice price,
                                                                     List<com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription> descriptions,
                                                                     MerchantStore store) {
        if (CollectionUtils.isEmpty(descriptions)) {
            return Collections.emptySet();
        }
        Set<ProductPriceDescription> descs = new HashSet<ProductPriceDescription>();
        for (com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription desc : descriptions) {
            ProductPriceDescription description = null;
            if (CollectionUtils.isNotEmpty(price.getDescriptions())) {
                for (ProductPriceDescription d : price.getDescriptions()) {
                    if (isPositive(desc.getId()) && desc.getId().equals(d.getId())) {
                        desc.setId(d.getId());
                    }
                }
            }
            description = getDescription(desc);
            description.setProductPrice(price);
            descs.add(description);
        }
        return descs;
    }

    private ProductPriceDescription getDescription(
            com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription desc) {
        ProductPriceDescription target = new ProductPriceDescription();
        target.setDescription(desc.getDescription());
        target.setName(desc.getName());
        target.setTitle(desc.getTitle());
        target.setId(null);
        if (isPositive(desc.getId())) {
            target.setId(desc.getId());
        }
        Language lang = getLanguage(desc);
        target.setLanguage(lang);
        return target;

    }

    private Language getLanguage(com.asrevo.cvhome.store.core.model.catalog.product.ProductPriceDescription desc) {
        try {
            return Optional.ofNullable(languageService.getByCode(desc.getLanguage()))
                    .orElseThrow(() -> new ConversionRuntimeException(
                            "Language is null for code " + desc.getLanguage() + " use language ISO code [en, fr ...]"));
        } catch (ServiceException e) {
            throw new ConversionRuntimeException(e);
        }
    }

}
