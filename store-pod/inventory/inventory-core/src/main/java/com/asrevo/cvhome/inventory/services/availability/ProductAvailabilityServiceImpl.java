package com.asrevo.cvhome.inventory.services.availability;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductAvailability;
import com.asrevo.cvhome.inventory.repositories.PageableProductAvailabilityRepository;
import com.asrevo.cvhome.inventory.repositories.ProductAvailabilityRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

/**
 * Availability -> Inventory
 */
@Service("productAvailabilityService")
public class ProductAvailabilityServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductAvailability>
        implements ProductAvailabilityService {

    private final ProductAvailabilityRepository productAvailabilityRepository;

    private final PageableProductAvailabilityRepository pageableProductAvailabilityRepository;

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
    public List<ProductAvailability> listAllByProduct(Long productId, StoreMerchantId store) {
        return productAvailabilityRepository.findByProductId(productId, store);
    }

    @Override
    public Optional<ProductAvailability> getById(Long availabilityId, StoreMerchantId store) {
        return productAvailabilityRepository.findProductAvailabilityById(availabilityId)
                .filter(it -> Objects.equals(it.getStoreMerchantId(), store));
    }

    @Override
    public Page<ProductAvailability> getBySku(String sku, Pageable pageable) {
        return pageableProductAvailabilityRepository.getBySku(sku, pageable);
    }

    @Override
    public List<ProductAvailability> getBySku(String sku, StoreMerchantId store) {
        return productAvailabilityRepository.getBySku(sku, store);
    }

    @Override
    public List<ProductAvailability> getBySkus(Collection<String> skus, StoreMerchantId store) {
        return productAvailabilityRepository.findBySkus(skus, store);
    }

}
