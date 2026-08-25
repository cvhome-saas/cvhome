package com.asrevo.cvhome.catalog.services.product.attribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.repositories.product.attribute.PageableProductAttributeRepository;
import com.asrevo.cvhome.catalog.repositories.product.attribute.ProductAttributeRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

@Service("productAttributeService")
public class ProductAttributeServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductAttribute>
        implements ProductAttributeService {

    private final ProductAttributeRepository productAttributeRepository;

    private final PageableProductAttributeRepository pageableProductAttributeRepository;

    @Autowired
    public ProductAttributeServiceImpl(ProductAttributeRepository productAttributeRepository,
                                       PageableProductAttributeRepository pageableProductAttributeRepository) {
        super(productAttributeRepository);
        this.productAttributeRepository = productAttributeRepository;
        this.pageableProductAttributeRepository = pageableProductAttributeRepository;
    }

    @Override
    public ProductAttribute getById(Long id) {

        return productAttributeRepository.findOne(id);
    }

    @Override
    public List<ProductAttribute> getByOptionId(StoreMerchantId store, Long id) {

        return productAttributeRepository.findByOptionId(store, id);
    }

    @Override
    public List<ProductAttribute> getByOptionValueId(StoreMerchantId store, Long id) {

        return productAttributeRepository.findByOptionValueId(store, id);
    }

    /**
     * Returns all product attributes
     */
    @Override
    public Page<ProductAttribute> getByProductId(StoreMerchantId store, Product product, LanguageCode language,
                                                 Pageable pageable) {

        return pageableProductAttributeRepository.findByProductId(store, product.getId(), language, pageable);
    }

    @Override
    public Page<ProductAttribute> getByProductId(StoreMerchantId store, Product product, Pageable pageable) {
        return pageableProductAttributeRepository.findByProductId(store, product.getId(), pageable);
    }

    @Override
    public ProductAttribute saveOrUpdate(ProductAttribute productAttribute) {
        productAttribute = productAttributeRepository.save(productAttribute);
        return productAttribute;
    }

    @Override
    public void delete(ProductAttribute attribute) {

        // override method, this allows the error that we try to remove a detached
        // variant
        attribute = this.getById(attribute.getId());
        super.delete(attribute);
    }

    @Override
    public List<ProductAttribute> getProductAttributesByCategoryLineage(StoreMerchantId store, String lineage,
                                                                        LanguageCode language) {
        return productAttributeRepository.findOptionsByCategoryLineage(store, lineage, language);
    }

}
