package com.asrevo.cvhome.catalog.services.product.availability;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductAvailabilityService
        extends SalesManagerEntityService<Long, ProductAvailability> {

    ProductAvailability saveOrUpdate(ProductAvailability availability) throws ServiceException;

    Page<ProductAvailability> listByProduct(
            Long productId, StoreMerchantId store, Pageable pageable);

    /**
     * Get by sku
     */
    Page<ProductAvailability> getBySku(String sku,Pageable pageable);

    /**
     * All availability by product / product variant sku and store
     */
    List<ProductAvailability> getBySku(String sku, StoreMerchantId store);

    Optional<ProductAvailability> getById(Long availabilityId, StoreMerchantId store);
}
