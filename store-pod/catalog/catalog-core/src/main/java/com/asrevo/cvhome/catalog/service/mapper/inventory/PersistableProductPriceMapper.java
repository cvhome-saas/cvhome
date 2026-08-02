package com.asrevo.cvhome.catalog.service.mapper.inventory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPriceDescription;
import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.availability.ProductAvailabilityService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

@Component
public class PersistableProductPriceMapper implements Mapper<PersistableProductPrice, ProductPrice> {

    private final ProductService productService;

    private final ProductAvailabilityService productAvailabilityService;

    public PersistableProductPriceMapper(ProductService productService,
                                         ProductAvailabilityService productAvailabilityService) {
        this.productService = productService;
        this.productAvailabilityService = productAvailabilityService;
    }

    @Override
    public ProductPrice convert(PersistableProductPrice source, StoreMerchantId store, LanguageCode language) {
        return merge(source, new ProductPrice(), store, language);
    }

    @Override
    public ProductPrice merge(PersistableProductPrice source, ProductPrice destination, StoreMerchantId store,
                              LanguageCode language) {

        try {
            if (destination == null) {
                destination = new ProductPrice();
            }

            destination.setId(source.getId());

            ProductAvailability availability = null;

            if (isPositive(source.getProductAvailabilityId())) {
                Optional<ProductAvailability> avail = productAvailabilityService
                        .getById(source.getProductAvailabilityId(), store);
                if (avail.isEmpty()) {
                    throw new ConversionRuntimeException(
                            "Product availability with id [%s] was not found".formatted(source.getProductAvailabilityId()));
                }
                availability = avail.get();

            } else {

                availability = findAvailabilityByAllRegionSku(source, store);
                destination = resolveExistingDefaultPrice(source, availability, destination);
            }

            if (availability == null) {

                Product product = productService.getBySku(source.getSku(), store, language);
                if (product == null) {
                    throw new ConversionRuntimeException(
                            "Product with sku [%s] not found for Store [%s]".formatted(source.getSku(), store));
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
                LocalDate startDate = source.getDiscountStartDate();

                destination.setProductPriceSpecialStartDate(startDate);
            }
            if (source.getDiscountEndDate() != null) {
                LocalDate endDate = source.getDiscountEndDate();

                destination.setProductPriceSpecialEndDate(endDate);
            }
            availability.getPrices().add(destination);
            destination.setProductAvailability(availability);
            destination.setDescriptions(this.getProductPriceDescriptions(destination, source.getDescriptions()));

            destination.setDefaultPrice(source.isDefaultPrice());

        } catch (Exception e) {

            throw new ConversionRuntimeException(e);
        }
        return destination;
    }

    private ProductAvailability findAvailabilityByAllRegionSku(PersistableProductPrice source, StoreMerchantId store) {
        List<ProductAvailability> existing = productAvailabilityService.getBySku(source.getSku(), store);
        if (CollectionUtils.isEmpty(existing)) {
            return null;
        }
        return existing.stream()
                .filter(a -> a.getRegion() != null && a.getRegion().equals(Constants.ALL_REGIONS))
                .findAny()
                .orElse(null);
    }

    private ProductPrice resolveExistingDefaultPrice(PersistableProductPrice source, ProductAvailability availability,
                                                     ProductPrice destination) {
        if (availability == null || !source.isDefaultPrice()) {
            return destination;
        }
        Optional<ProductPrice> defaultPrice = availability.getPrices()
                .stream()
                .filter(ProductPrice::isDefaultPrice)
                .findAny();
        return defaultPrice.orElse(destination);
    }

    private Set<ProductPriceDescription> getProductPriceDescriptions(ProductPrice price,
                                                                     List<com.asrevo.cvhome.catalog.model.product.ProductPriceDescription> descriptions) {
        if (CollectionUtils.isEmpty(descriptions)) {
            return Collections.emptySet();
        }
        Set<ProductPriceDescription> descs = new HashSet<>();
        for (com.asrevo.cvhome.catalog.model.product.ProductPriceDescription desc : descriptions) {
            ProductPriceDescription description;
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
            com.asrevo.cvhome.catalog.model.product.ProductPriceDescription desc) {
        ProductPriceDescription target = new ProductPriceDescription();
        target.setDescription(desc.getDescription());
        target.setName(desc.getName());
        target.setTitle(desc.getTitle());
        target.setId(null);
        if (isPositive(desc.getId())) {
            target.setId(desc.getId());
        }
        target.setLanguageCode(desc.getLanguage());
        return target;
    }

}
