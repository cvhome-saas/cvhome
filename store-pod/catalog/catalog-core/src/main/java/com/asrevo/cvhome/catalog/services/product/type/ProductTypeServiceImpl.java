package com.asrevo.cvhome.catalog.services.product.type;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.repositories.product.type.PageableProductTypeRepository;
import com.asrevo.cvhome.catalog.repositories.product.type.ProductTypeRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

@Service("productTypeService")
public class ProductTypeServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductType>
        implements ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    private final PageableProductTypeRepository pageableProductTypeRepository;

    @Autowired
    public ProductTypeServiceImpl(ProductTypeRepository productTypeRepository,
                                  PageableProductTypeRepository pageableProductTypeRepository) {
        super(productTypeRepository);
        this.productTypeRepository = productTypeRepository;
        this.pageableProductTypeRepository = pageableProductTypeRepository;
    }

    @Override
    public ProductType getByCode(String code, StoreMerchantId store, LanguageCode language) {
        return productTypeRepository.findByCode(code, store);
    }

    @Override
    public Page<ProductType> getByMerchant(StoreMerchantId store, LanguageCode language, Pageable pageable) {
        return pageableProductTypeRepository.findByStoreMerchantId(store, pageable);
    }

    @Override
    public ProductType saveOrUpdate(ProductType productType) {
        if (productType.getId() != null && productType.getId() > 0) {
            this.update(productType);
        } else {
            productType = super.saveAndFlush(productType);
        }

        return productType;
    }

    @Override
    public List<ProductType> listProductTypes(List<Long> ids, StoreMerchantId store, LanguageCode language) {
        return productTypeRepository.findByIds(ids, store, language);
    }

    @Override
    public ProductType getById(Long id, StoreMerchantId store) {
        return productTypeRepository.findById(id, store);
    }

}
