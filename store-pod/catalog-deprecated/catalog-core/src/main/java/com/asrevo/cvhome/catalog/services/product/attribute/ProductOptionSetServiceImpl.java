package com.asrevo.cvhome.catalog.services.product.attribute;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.catalog.repositories.product.attribute.ProductOptionSetRepository;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

@Service("productOptionSetService")
public class ProductOptionSetServiceImpl extends SalesManagerEntityServiceImpl<Long, ProductOptionSet>
        implements ProductOptionSetService {

    private final ProductOptionSetRepository productOptionSetRepository;

    @Autowired
    public ProductOptionSetServiceImpl(ProductOptionSetRepository productOptionSetRepository) {
        super(productOptionSetRepository);
        this.productOptionSetRepository = productOptionSetRepository;
    }

    @Override
    public List<ProductOptionSet> listByStore(StoreMerchantId store, LanguageCode language) {
        return productOptionSetRepository.findByStore(store, language);
    }

    @Override
    public ProductOptionSet getById(StoreMerchantId store, Long optionSetId, LanguageCode lang) {
        return productOptionSetRepository.findOne(store, optionSetId, lang);
    }

    @Override
    public ProductOptionSet getCode(StoreMerchantId store, String code) {
        return productOptionSetRepository.findByCode(store, code);
    }

    @Override
    public List<ProductOptionSet> getByProductType(Long productTypeId, StoreMerchantId store, LanguageCode lang) {
        return productOptionSetRepository.findByProductType(productTypeId, store, lang);
    }

}
