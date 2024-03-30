package com.asrevo.cvhome.store.core.services.catalog.inventory;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.inventory.ProductInventory;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Set;


@Service("inventoryService")
public class ProductInventoryServiceImpl implements ProductInventoryService {


    @Autowired
    private PricingService pricingService;

    @Override
    public ProductInventory inventory(Product product) throws ServiceException {
        Assert.notNull(product.getAvailabilities(), "availabilities can't be null");

        ProductAvailability availability = defaultAvailability(product.getAvailabilities());
        FinalPrice finalPrice = pricingService.calculateProductPrice(product);

        ProductInventory inventory = inventory(availability, finalPrice);
        inventory.setSku(product.getSku());
        return inventory;
    }

    @Override
    public ProductInventory inventory(ProductVariant variant) throws ServiceException {
        Assert.notNull(variant.getAvailabilities(), "availabilities can't be null");
        Assert.notNull(variant.getProduct(), "product can't be null");

        ProductAvailability availability = null;
        if (!CollectionUtils.isEmpty(variant.getAvailabilities())) {
            availability = defaultAvailability(variant.getAvailabilities());
        } else {
            availability = defaultAvailability(variant.getProduct().getAvailabilities());
        }
        FinalPrice finalPrice = pricingService.calculateProductPrice(variant);

        if (finalPrice == null) {
            finalPrice = pricingService.calculateProductPrice(variant.getProduct());
        }

        ProductInventory inventory = inventory(availability, finalPrice);
        inventory.setSku(variant.getSku());
        return inventory;
    }

    private ProductAvailability defaultAvailability(Set<ProductAvailability> availabilities) {

        ProductAvailability defaultAvailability = availabilities.iterator().next();

        for (ProductAvailability availability : availabilities) {
            if (!StringUtils.isEmpty(availability.getRegion())
                    && availability.getRegion().equals(Constants.ALL_REGIONS)) {// TODO REL 2.1 accept a region
                defaultAvailability = availability;
            }
        }

        return defaultAvailability;

    }

    private ProductInventory inventory(ProductAvailability availability, FinalPrice price) {
        ProductInventory inventory = new ProductInventory();
        inventory.setQuantity(availability.getProductQuantity());
        inventory.setPrice(price);

        return inventory;
    }

}
