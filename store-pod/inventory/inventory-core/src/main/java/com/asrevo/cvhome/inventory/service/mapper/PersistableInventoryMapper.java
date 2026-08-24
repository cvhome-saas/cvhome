package com.asrevo.cvhome.inventory.service.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.inventory.entity.ProductPrice;
import com.asrevo.cvhome.inventory.entity.ProductPriceDescription;
import com.asrevo.cvhome.inventory.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.model.inventory.PersistableInventory;
import com.asrevo.cvhome.inventory.model.price.PersistableProductPrice;
import com.asrevo.cvhome.inventory.repositories.ProductAvailabilityRepository;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

/**
 * Persists an inventory record keyed by sku. The former product/variant resolution against catalog entities is gone
 * with the split — the sku and productId on the payload are stored as given, and an existing row for the same sku is
 * merged rather than duplicated.
 */
@Component
public class PersistableInventoryMapper implements Mapper<PersistableInventory, ProductAvailability> {

    private final ProductAvailabilityRepository productAvailabilityRepository;

    public PersistableInventoryMapper(ProductAvailabilityRepository productAvailabilityRepository) {
        this.productAvailabilityRepository = productAvailabilityRepository;
    }

    @Override
    public ProductAvailability convert(PersistableInventory source, StoreMerchantId store, LanguageCode language)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException {
        ProductAvailability availability = new ProductAvailability();
        availability.setStoreMerchantId(store);
        return merge(source, availability, store, language);
    }

    @Override
    public ProductAvailability merge(PersistableInventory source, ProductAvailability destination,
                                     StoreMerchantId store, LanguageCode language)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException {
        try {
            destination = applyExistingAvailability(source, destination, store);

            destination.setStoreMerchantId(store);
            destination.setSku(source.getSku());
            destination.setProductId(source.getProductId());
            destination.setProductQuantity(source.getQuantity());
            destination.setProductQuantityOrderMin(source.getProductQuantityOrderMax());
            destination.setProductQuantityOrderMax(source.getProductQuantityOrderMin());
            destination.setAvailable(source.isAvailable());
            destination.setOwner(source.getOwner());

            String region = getRegion(source);
            destination.setRegion(region);

            destination.setRegionVariant(source.getRegionVariant());
            if (Objects.nonNull(source.getDateAvailable())) {
                destination.setProductDateAvailable(source.getDateAvailable());
            }

            mergePrices(source, destination);

            return destination;

        } catch (ConversionException e) {
            // Already names which reference in the payload failed to resolve; re-wrapping would bury it.
            throw e;
        } catch (Exception e) {
            throw InventoryNotConvertibleException.of(e);
        }
    }

    private ProductAvailability applyExistingAvailability(PersistableInventory source,
                                                          ProductAvailability destination, StoreMerchantId store)
            throws InventoryReferenceUnresolvableException {
        ProductAvailability existing = findExistingAvailability(source, destination, store);
        if (existing == null) {
            return destination;
        }
        if (!existing.getStoreMerchantId().equals(store)) {
            throw InventoryReferenceUnresolvableException.of(source.getId(), store);
        }
        return existing;
    }

    private ProductAvailability findExistingAvailability(PersistableInventory source, ProductAvailability destination,
                                                         StoreMerchantId store) {
        if (source.getId() != null && source.getId() > 0) {
            return destination;
        }
        if (StringUtils.isBlank(source.getSku())) {
            return null;
        }
        return productAvailabilityRepository.findBySkus(List.of(source.getSku()), store).stream()
                .filter(a -> matchesExistingAvailability(source, a))
                .findAny()
                .orElse(null);
    }

    private boolean matchesExistingAvailability(PersistableInventory source, ProductAvailability a) {
        return bothRegionVariantsNull(source, a) || matchesByRegionVariant(source, a);
    }

    private boolean bothRegionVariantsNull(PersistableInventory source, ProductAvailability a) {
        return source.getRegionVariant() == null && a.getRegionVariant() == null;
    }

    private boolean matchesByRegionVariant(PersistableInventory source, ProductAvailability a) {
        return a.getRegionVariant() != null && source.getRegionVariant() != null
                && a.getRegionVariant().equals(source.getRegionVariant());
    }

    private void mergePrices(PersistableInventory source, ProductAvailability destination) {
        List<ProductPrice> prices = new ArrayList<>();
        for (PersistableProductPrice priceEntity : source.getPrices()) {

            ProductPrice price = null;

            if (destination.getPrices() != null) {
                for (ProductPrice pp : destination.getPrices()) {
                    price = mergeExistingPrice(priceEntity, pp, price, prices);
                }
            }

            if (price == null) {
                price = new ProductPrice();
            }

            prices.add(price);

            price.setProductAvailability(destination);
            price.setStoreMerchantId(destination.getStoreMerchantId());
            price.setDefaultPrice(priceEntity.isDefaultPrice());
            price.setProductPriceAmount(priceEntity.getPrice());
            price.setCode(priceEntity.getCode());
            price.setProductPriceSpecialAmount(priceEntity.getDiscountedPrice());

            if (Objects.nonNull(priceEntity.getDiscountStartDate())) {
                LocalDate startDate = priceEntity.getDiscountStartDate();
                price.setProductPriceSpecialStartDate(startDate);
            }
            if (Objects.nonNull(priceEntity.getDiscountEndDate())) {
                LocalDate endDate = priceEntity.getDiscountEndDate();
                price.setProductPriceSpecialEndDate(endDate);
            }

            Set<ProductPriceDescription> descs = getProductPriceDescriptions(price, priceEntity.getDescriptions());
            price.setDescriptions(descs);

            destination.setPrices(new HashSet<>(prices));
        }
    }

    private ProductPrice mergeExistingPrice(PersistableProductPrice priceEntity, ProductPrice pp, ProductPrice price,
                                            List<ProductPrice> prices) {
        if (isPositive(priceEntity.getId()) && priceEntity.getId().longValue() == pp.getId().longValue()) {
            prices.add(pp);
            return pp;
        }
        if (pp.isDefaultPrice() && priceEntity.isDefaultPrice() && price == null) {
            return pp;
        }
        prices.add(pp);
        return price;
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

    private String getRegion(PersistableInventory source) {
        return Optional.ofNullable(source.getRegion()).filter(StringUtils::isNotBlank).orElse(Constants.ALL_REGIONS);
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
