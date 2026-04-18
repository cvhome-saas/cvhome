package com.asrevo.cvhome.catalog.service.mapper.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPrice;
import com.asrevo.cvhome.catalog.entity.product.price.ProductPriceDescription;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;
import com.asrevo.cvhome.catalog.model.product.inventory.PersistableInventory;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.DateUtil;

import static com.asrevo.cvhome.store.utils.NumberUtils.isPositive;

@Component
public class PersistableInventoryMapper implements Mapper<PersistableInventory, ProductAvailability> {

    private final ProductVariantService productVariantService;

    private final ProductService productService;

    public PersistableInventoryMapper(ProductVariantService productVariantService, ProductService productService) {
        this.productVariantService = productVariantService;
        this.productService = productService;
    }

    @Override
    public ProductAvailability convert(PersistableInventory source, StoreMerchantId store, LanguageCode language) {
        ProductAvailability availability = new ProductAvailability();
        availability.setStoreMerchantId(store);
        return merge(source, availability, store, language);
    }

    @Override
    public ProductAvailability merge(PersistableInventory source, ProductAvailability destination,
                                     StoreMerchantId store, LanguageCode language) {
        Assert.notNull(destination, "Product availability cannot be null");

        try {
            Product product = null;
            if (source.getProductId() != null && source.getProductId() > 0) {
                product = productService.findOne(source.getProductId(), store);
                if (product == null) {
                    throw new ResourceNotFoundException(
                            "Product with id [" + source.getProductId() + "] not found for store [" + store + "]");
                }
                destination.setProduct(product);
            }

            Set<ProductAvailability> existingAvailability = Optional.ofNullable(product)
                    .map(Product::getAvailabilities)
                    .orElse(Set.of());
            ProductAvailability existing;
            // determine product availability to be used
            if (source.getId() != null && source.getId() > 0) {
                existing = destination;
            } else {
                existing = existingAvailability.stream()
                        .filter(a -> (source.getProductId() != null
                                && (a.getProduct().getId().longValue() == source.getProductId().longValue())
                                && a.getStoreMerchantId().equals(store)
                                && (source.getVariant() == null && a.getProductVariant() == null)
                                || (a.getProductVariant() != null && source.getVariant() != null
                                && a.getProductVariant().getId().longValue() == source.getVariant().longValue())
                                && (source.getRegionVariant() == null && a.getRegionVariant() == null)
                                || (a.getRegionVariant() != null && source.getRegionVariant() != null
                                && a.getRegionVariant().equals(source.getRegionVariant()))))
                        .findAny()
                        .orElse(null);
            }
            if (existing != null) {
                if (!existing.getStoreMerchantId().equals(store)) {
                    throw new ResourceNotFoundException(
                            "Product Inventory with id [" + source.getId() + "] not found for store [" + store + "]");
                }
                destination = existing;
            }

            destination.setProductQuantity(source.getQuantity());
            destination.setProductQuantityOrderMin(source.getProductQuantityOrderMax());
            destination.setProductQuantityOrderMax(source.getProductQuantityOrderMin());
            destination.setAvailable(source.isAvailable());
            destination.setOwner(source.getOwner());

            String region = getRegion(source);
            destination.setRegion(region);

            destination.setRegionVariant(source.getRegionVariant());
            if (StringUtils.isNotBlank(source.getDateAvailable())) {
                destination.setProductDateAvailable(DateUtil.getDate(source.getDateAvailable()));
            }

            if (source.getVariant() != null && source.getVariant() > 0) {
                Optional<ProductVariant> instance = productVariantService.getById(source.getVariant(), store);
                if (instance.isEmpty()) {
                    throw new ResourceNotFoundException(
                            "productVariant with id [" + source.getVariant() + "] not found for store [" + store + "]");
                }
                destination.setSku(instance.get().getSku());
                destination.setProductVariant(instance.get());
            }

            // merge with existing or replace
            List<ProductPrice> prices = new ArrayList<>();
            for (PersistableProductPrice priceEntity : source.getPrices()) {

                ProductPrice price = null;

                if (destination.getPrices() != null) {
                    for (ProductPrice pp : destination.getPrices()) {
                        if (isPositive(priceEntity.getId())
                                && priceEntity.getId().longValue() == pp.getId().longValue()) {
                            price = pp;
                            prices.add(pp);
                        } else if (pp.isDefaultPrice() && priceEntity.isDefaultPrice()) {
                            if (price == null) {
                                price = pp;
                            } else {
                                prices.add(pp);
                            }
                        } else {
                            prices.add(pp);
                        }
                    }
                }

                if (price == null) {
                    price = new ProductPrice();
                }

                prices.add(price);

                price.setProductAvailability(destination);
                price.setDefaultPrice(priceEntity.isDefaultPrice());
                price.setProductPriceAmount(priceEntity.getPrice());
                price.setDefaultPrice(priceEntity.isDefaultPrice());
                price.setCode(priceEntity.getCode());
                price.setProductPriceSpecialAmount(priceEntity.getDiscountedPrice());

                if (Objects.nonNull(priceEntity.getDiscountStartDate())) {
                    Date startDate = DateUtil.getDate(priceEntity.getDiscountStartDate());
                    price.setProductPriceSpecialStartDate(startDate);
                }
                if (Objects.nonNull(priceEntity.getDiscountEndDate())) {
                    Date endDate = DateUtil.getDate(priceEntity.getDiscountEndDate());
                    price.setProductPriceSpecialEndDate(endDate);
                }

                Set<ProductPriceDescription> descs = getProductPriceDescriptions(price, priceEntity.getDescriptions());
                price.setDescriptions(descs);

                destination.setPrices(new HashSet<>(prices));
            }

            return destination;

        } catch (ResourceNotFoundException rne) {
            throw new ConversionRuntimeException(rne.getErrorCode(), rne.getErrorMessage(), rne);
        } catch (Exception e) {
            throw new ConversionRuntimeException(e);
        }
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

    private String getRegion(PersistableInventory source) {
        return Optional.ofNullable(source.getRegion()).filter(StringUtils::isNotBlank).orElse(Constants.ALL_REGIONS);
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
