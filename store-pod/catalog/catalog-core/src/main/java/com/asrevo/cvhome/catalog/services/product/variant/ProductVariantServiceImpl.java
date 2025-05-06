package com.asrevo.cvhome.catalog.services.product.variant;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.repositories.product.variant.PageableProductVariantRepositoty;
import com.asrevo.cvhome.catalog.repositories.product.variant.ProductVariantRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service("productVariantService")
public class ProductVariantServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductVariant>
        implements ProductVariantService {

    private final ProductVariantRepository productVariantRepository;

    private final PageableProductVariantRepositoty pageableProductVariantRepositoty;

    @Autowired
    public ProductVariantServiceImpl(
            ProductVariantRepository productVariantRepository,
            PageableProductVariantRepositoty pageableProductVariantRepositoty) {
        super(productVariantRepository);
        this.productVariantRepository = productVariantRepository;
        this.pageableProductVariantRepositoty = pageableProductVariantRepositoty;
    }

    @Override
    public Optional<ProductVariant> getById(Long id, Long productId, StoreMerchantId store) {
        return productVariantRepository.findById(id, productId, store);
    }

    public Page<ProductVariant> getByProductId(
            StoreMerchantId store, Product product, LanguageCode language, Pageable pageable) {
        return pageableProductVariantRepositoty.findByProductId(store, product.getId(), pageable);
    }

    @Override
    public List<ProductVariant> getByProductId(
            StoreMerchantId store, Product product, LanguageCode language) {
        return productVariantRepository.findByProductId(store, product.getId());
    }

    @Override
    public boolean exist(String sku, Long productId) {

        ProductVariant instance = productVariantRepository.existsBySkuAndProduct(sku, productId);
        return instance != null;
    }

    @Override
    public Optional<ProductVariant> getById(Long id, StoreMerchantId store) {

        return productVariantRepository.findOne(id, store);
    }

    @Override
    public List<ProductVariant> getByIds(List<Long> ids, StoreMerchantId store) {

        return productVariantRepository.findByIds(ids, store);
    }

    @Override
    public ProductVariant saveProductVariant(ProductVariant variant) {

        variant = productVariantRepository.save(variant);
        return variant;
    }

    @Override
    public void delete(ProductVariant instance) throws ServiceException {
        super.delete(instance);
    }
}
