package com.asrevo.cvhome.inventory.service.mapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.inventory.entity.ProductPrice;
import com.asrevo.cvhome.inventory.entity.ProductPriceDescription;
import com.asrevo.cvhome.inventory.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.SkuReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.model.price.PersistableProductPrice;
import com.asrevo.cvhome.inventory.services.availability.ProductAvailabilityService;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

@Component
public class PersistableProductPriceMapper implements Mapper<PersistableProductPrice, ProductPrice> {

    private final ProductAvailabilityService productAvailabilityService;

    public PersistableProductPriceMapper(ProductAvailabilityService productAvailabilityService) {
        this.productAvailabilityService = productAvailabilityService;
    }

    @Override
    public ProductPrice convert(PersistableProductPrice source, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            SkuReferenceUnresolvableException {
        return merge(source, new ProductPrice(), store, language);
    }

    @Override
    public ProductPrice merge(PersistableProductPrice source, ProductPrice destination, StoreMerchantId store,
                              LanguageCode language)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            SkuReferenceUnresolvableException {

        try {
            if (destination == null) {
                destination = new ProductPrice();
            }

            destination.setId(source.getId());

            ProductAvailability availability;

            if (isPositive(source.getProductAvailabilityId())) {
                Optional<ProductAvailability> avail = productAvailabilityService
                        .getById(source.getProductAvailabilityId(), store);
                if (avail.isEmpty()) {
                    throw InventoryReferenceUnresolvableException.of(source.getProductAvailabilityId(), store);
                }
                availability = avail.get();

            } else {

                availability = findAvailabilityByAllRegionSku(source, store);
                destination = resolveExistingDefaultPrice(source, availability, destination);
            }

            if (availability == null) {
                // A price cannot exist without the availability row it hangs off; the catalog's product is no longer
                // reachable to create one implicitly.
                throw SkuReferenceUnresolvableException.of(source.getSku(), store);
            }

            destination.setProductAvailability(availability);
            destination.setStoreMerchantId(store);
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

        } catch (ConversionException e) {
            // Already names the unresolvable reference; re-wrapping would bury it.
            throw e;
        } catch (Exception e) {
            throw ProductPriceNotConvertibleException.of(e);
        }
        return destination;
    }

    private ProductAvailability findAvailabilityByAllRegionSku(PersistableProductPrice source, StoreMerchantId store) {
        List<ProductAvailability> existing = productAvailabilityService.getBySku(source.getSku(), store);
        if (existing == null || existing.isEmpty()) {
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
            List<com.asrevo.cvhome.inventory.model.price.ProductPriceDescription> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return Collections.emptySet();
        }
        Set<ProductPriceDescription> descs = new HashSet<>();
        for (com.asrevo.cvhome.inventory.model.price.ProductPriceDescription desc : descriptions) {
            ProductPriceDescription description;
            if (price.getDescriptions() != null && !price.getDescriptions().isEmpty()) {
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
            com.asrevo.cvhome.inventory.model.price.ProductPriceDescription desc) {
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
