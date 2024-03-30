package com.asrevo.cvhome.store.core.services.catalog.inventory;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.inventory.ProductInventory;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;

public interface ProductInventoryService {


    ProductInventory inventory(Product product) throws ServiceException;

    ProductInventory inventory(ProductVariant variant) throws ServiceException;

}
