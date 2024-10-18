package com.asrevo.cvhome.store.core.services.catalog.product.availability;

import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.availability.PageableProductAvailabilityRepository;
import com.asrevo.cvhome.store.core.repositories.catalog.product.availability.ProductAvailabilityRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Availability -> Inventory
 *
 * @author carlsamson
 */
@Service("productAvailabilityService")
public class ProductAvailabilityServiceImpl
        extends SalesManagerEntityServiceImpl<Long, ProductAvailability>
        implements ProductAvailabilityService {

    private final ProductAvailabilityRepository productAvailabilityRepository;

    private final PageableProductAvailabilityRepository pageableProductAvailabilityRepository;

    @Autowired
    public ProductAvailabilityServiceImpl(
            ProductAvailabilityRepository productAvailabilityRepository,
            PageableProductAvailabilityRepository pageableProductAvailabilityRepository) {
        super(productAvailabilityRepository);
        this.productAvailabilityRepository = productAvailabilityRepository;
        this.pageableProductAvailabilityRepository = pageableProductAvailabilityRepository;
    }

    @Override
    public ProductAvailability saveOrUpdate(ProductAvailability availability)
            throws ServiceException {
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
    public Page<ProductAvailability> listByProduct(
            Long productId, MerchantStore store, int page, int count) {
        Assert.notNull(productId, "Product cannot be null");
        Assert.notNull(store, "MercantStore cannot be null");
        Pageable pageRequest = PageRequest.of(page, count);
        return pageableProductAvailabilityRepository.getByProductId(
                productId, store.getCode(), pageRequest);
    }

    @Override
    public Optional<ProductAvailability> getById(Long availabilityId, MerchantStore store) {
        Assert.notNull(store, "Merchant must not be null");
        return Optional.ofNullable(productAvailabilityRepository.getById(availabilityId));
    }

    @Override
    public Page<ProductAvailability> getBySku(
            String sku, MerchantStore store, int page, int count) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Pageable pageRequest = PageRequest.of(page, count);
        return pageableProductAvailabilityRepository.getBySku(sku, store.getCode(), pageRequest);
    }

    @Override
    public Page<ProductAvailability> getBySku(String sku, int page, int count) {
        Pageable pageRequest = PageRequest.of(page, count);
        return pageableProductAvailabilityRepository.getBySku(sku, pageRequest);
    }

    @Override
    public List<ProductAvailability> getBySku(String sku, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        return productAvailabilityRepository.getBySku(sku, store.getCode());
    }
}
