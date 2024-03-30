package com.asrevo.cvhome.store.core.services.catalog.product.variant;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.variant.PageableProductVariantGroupRepository;
import com.asrevo.cvhome.store.core.repositories.catalog.product.variant.ProductVariantGroupRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service("productVariantGroupService")
public class ProductVariantGroupServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductVariantGroup> implements ProductVariantGroupService {


    @Autowired
    private PageableProductVariantGroupRepository pageableProductVariantGroupRepository;

    private ProductVariantGroupRepository productVariantGroupRepository;

    public ProductVariantGroupServiceImpl(ProductVariantGroupRepository repository) {
        super(repository);
        this.productVariantGroupRepository = repository;
    }

    @Override
    public Optional<ProductVariantGroup> getById(Long id, MerchantStore store) {
        return productVariantGroupRepository.findOne(id, store.getCode());

    }

    @Override
    public Optional<ProductVariantGroup> getByProductVariant(Long productVariantId, MerchantStore store,
                                                             Language language) {
        return productVariantGroupRepository.finByProductVariant(productVariantId, store.getCode());
    }

    @Override
    public ProductVariantGroup saveOrUpdate(ProductVariantGroup entity) throws ServiceException {

        entity = productVariantGroupRepository.save(entity);
        return entity;

    }

    @Override
    public Page<ProductVariantGroup> getByProductId(MerchantStore store, Long productId, Language language, int page,
                                                    int count) {

        Pageable pageRequest = PageRequest.of(page, count);
        return pageableProductVariantGroupRepository.findByProductId(store.getId(), productId, pageRequest);
    }


}
