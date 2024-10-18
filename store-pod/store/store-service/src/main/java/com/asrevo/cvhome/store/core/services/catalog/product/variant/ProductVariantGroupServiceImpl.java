package com.asrevo.cvhome.store.core.services.catalog.product.variant;

import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.variant.PageableProductVariantGroupRepository;
import com.asrevo.cvhome.store.core.repositories.catalog.product.variant.ProductVariantGroupRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service("productVariantGroupService")
public class ProductVariantGroupServiceImpl
        extends SalesManagerEntityServiceImpl<Long, ProductVariantGroup>
        implements ProductVariantGroupService {

    private final PageableProductVariantGroupRepository pageableProductVariantGroupRepository;

    private final ProductVariantGroupRepository productVariantGroupRepository;

    public ProductVariantGroupServiceImpl(
            ProductVariantGroupRepository repository,
            PageableProductVariantGroupRepository pageableProductVariantGroupRepository) {
        super(repository);
        this.productVariantGroupRepository = repository;
        this.pageableProductVariantGroupRepository = pageableProductVariantGroupRepository;
    }

    @Override
    public Optional<ProductVariantGroup> getById(Long id, MerchantStore store) {
        return productVariantGroupRepository.findOne(id, store.getCode());
    }

    @Override
    public Optional<ProductVariantGroup> getByProductVariant(
            Long productVariantId, MerchantStore store, Language language) {
        return productVariantGroupRepository.finByProductVariant(productVariantId, store.getCode());
    }

    @Override
    public ProductVariantGroup saveOrUpdate(ProductVariantGroup entity) throws ServiceException {

        entity = productVariantGroupRepository.save(entity);
        return entity;
    }

    @Override
    public Page<ProductVariantGroup> getByProductId(
            MerchantStore store, Long productId, Language language, int page, int count) {

        Pageable pageRequest = PageRequest.of(page, count);
        return pageableProductVariantGroupRepository.findByProductId(
                store.getId(), productId, pageRequest);
    }
}
