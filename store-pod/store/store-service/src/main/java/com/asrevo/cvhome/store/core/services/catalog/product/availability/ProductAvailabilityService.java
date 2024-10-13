package com.asrevo.cvhome.store.core.services.catalog.product.availability;

import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ProductAvailabilityService
        extends SalesManagerEntityService<Long, ProductAvailability> {

    ProductAvailability saveOrUpdate(ProductAvailability availability) throws ServiceException;

    Page<ProductAvailability> listByProduct(
            Long productId, MerchantStore store, int page, int count);

    /**
     * Get by product sku and store
     *
     */
    Page<ProductAvailability> getBySku(String sku, MerchantStore store, int page, int count);

    /**
     * Get by sku
     *
     */
    Page<ProductAvailability> getBySku(String sku, int page, int count);

    /**
     * All availability by product / product variant sku and store
     *
     */
    List<ProductAvailability> getBySku(String sku, MerchantStore store);

    Optional<ProductAvailability> getById(Long availabilityId, MerchantStore store);
}
