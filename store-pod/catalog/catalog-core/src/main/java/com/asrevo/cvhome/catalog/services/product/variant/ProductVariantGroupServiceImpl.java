package com.asrevo.cvhome.catalog.services.product.variant;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.catalog.repositories.product.variant.PageableProductVariantGroupRepository;
import com.asrevo.cvhome.catalog.repositories.product.variant.ProductVariantGroupRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.Optional;
import org.springframework.data.domain.Page;
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
    public Optional<ProductVariantGroup> getById(Long id, StoreMerchantId store) {
        return productVariantGroupRepository.findOne(id, store);
    }

    @Override
    public ProductVariantGroup saveOrUpdate(ProductVariantGroup entity) {

        entity = productVariantGroupRepository.save(entity);
        return entity;
    }

    @Override
    public Page<ProductVariantGroup> getByProductId(
            StoreMerchantId store, Long productId, LanguageCode language, Pageable pageable) {
        return pageableProductVariantGroupRepository.findByProductId(store, productId, pageable);
    }
}
