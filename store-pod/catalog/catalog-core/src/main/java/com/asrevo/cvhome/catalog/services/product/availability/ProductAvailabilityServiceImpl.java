package com.asrevo.cvhome.catalog.services.product.availability;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.repositories.product.availability.PageableProductAvailabilityRepository;
import com.asrevo.cvhome.catalog.repositories.product.availability.ProductAvailabilityRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

/**
 * Availability -> Inventory
 *
 * @author carlsamson
 */
@Service("productAvailabilityService")
public class ProductAvailabilityServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductAvailability>
        implements ProductAvailabilityService {

    private final ProductAvailabilityRepository productAvailabilityRepository;

    private final PageableProductAvailabilityRepository pageableProductAvailabilityRepository;

    @Autowired
    public ProductAvailabilityServiceImpl(ProductAvailabilityRepository productAvailabilityRepository,
                                          PageableProductAvailabilityRepository pageableProductAvailabilityRepository) {
        super(productAvailabilityRepository);
        this.productAvailabilityRepository = productAvailabilityRepository;
        this.pageableProductAvailabilityRepository = pageableProductAvailabilityRepository;
    }

    @Override
    public ProductAvailability saveOrUpdate(ProductAvailability availability) {
        if (isPositive(availability.getId())) {
            update(availability);
        } else {
            create(availability);
        }

        return availability;
    }

    private boolean isPositive(Long id) {
        return Objects.nonNull(id) && id > 0;
    }

    @Override
    public Page<ProductAvailability> listByProduct(Long productId, StoreMerchantId store, Pageable pageable) {
        return pageableProductAvailabilityRepository.getByProductId(productId, store, pageable);
    }

    @Override
    public Optional<ProductAvailability> getById(Long availabilityId, StoreMerchantId store) {
        return productAvailabilityRepository.findProductAvailabilityById(availabilityId);
    }

    @Override
    public Page<ProductAvailability> getBySku(String sku, Pageable pageable) {
        return pageableProductAvailabilityRepository.getBySku(sku, pageable);
    }

    @Override
    public List<ProductAvailability> getBySku(String sku, StoreMerchantId store) {
        return productAvailabilityRepository.getBySku(sku, store);
    }

}
