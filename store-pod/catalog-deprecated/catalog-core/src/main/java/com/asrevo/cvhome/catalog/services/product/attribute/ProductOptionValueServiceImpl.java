package com.asrevo.cvhome.catalog.services.product.attribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.repositories.product.attribute.PageableProductOptionValueRepository;
import com.asrevo.cvhome.catalog.repositories.product.attribute.ProductOptionValueRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

@Service("productOptionValueService")
public class ProductOptionValueServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductOptionValue>
        implements ProductOptionValueService {

    private final ProductAttributeService productAttributeService;

    private final PageableProductOptionValueRepository pageableProductOptionValueRepository;

    private final ProductOptionValueRepository productOptionValueRepository;

    @Autowired
    public ProductOptionValueServiceImpl(ProductOptionValueRepository productOptionValueRepository,
                                         ProductAttributeService productAttributeService,
                                         PageableProductOptionValueRepository pageableProductOptionValueRepository) {
        super(productOptionValueRepository);
        this.productOptionValueRepository = productOptionValueRepository;
        this.productAttributeService = productAttributeService;
        this.pageableProductOptionValueRepository = pageableProductOptionValueRepository;
    }

    @Override
    public void saveOrUpdate(ProductOptionValue entity) {

        // save or update (persist and attach entities
        if (entity.getId() != null && entity.getId() > 0) {

            super.update(entity);

        } else {

            super.save(entity);
        }
    }

    public void delete(ProductOptionValue entity) {

        // remove all attributes having this option
        List<ProductAttribute> attributes = productAttributeService.getByOptionValueId(entity.getStoreMerchantId(),
                entity.getId());

        for (ProductAttribute attribute : attributes) {
            productAttributeService.delete(attribute);
        }

        ProductOptionValue option = getById(entity.getId());

        // remove option
        super.delete(option);
    }

    @Override
    public ProductOptionValue getByCode(StoreMerchantId store, String optionValueCode) {
        return productOptionValueRepository.findByCode(store, optionValueCode);
    }

    @Override
    public ProductOptionValue getById(StoreMerchantId store, Long optionValueId) {
        return productOptionValueRepository.findOne(store, optionValueId);
    }

    @Override
    public Page<ProductOptionValue> getByMerchant(StoreMerchantId store, LanguageCode language, String name,
                                                  Pageable pageable) {
        return pageableProductOptionValueRepository.listOptionValues(store, name, pageable);
    }

}
