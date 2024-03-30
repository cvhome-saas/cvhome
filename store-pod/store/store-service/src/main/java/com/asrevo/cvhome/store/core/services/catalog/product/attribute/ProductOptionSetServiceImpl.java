package com.asrevo.cvhome.store.core.services.catalog.product.attribute;

import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.attribute.ProductOptionSetRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("productOptionSetService")
public class ProductOptionSetServiceImpl extends
        SalesManagerEntityServiceImpl<Long, ProductOptionSet> implements ProductOptionSetService {


    private ProductOptionSetRepository productOptionSetRepository;


    @Autowired
    public ProductOptionSetServiceImpl(
            ProductOptionSetRepository productOptionSetRepository) {
        super(productOptionSetRepository);
        this.productOptionSetRepository = productOptionSetRepository;
    }


    @Override
    public List<ProductOptionSet> listByStore(MerchantStore store, Language language) throws ServiceException {
        return productOptionSetRepository.findByStore(store.getId(), language.getId());
    }


    @Override
    public ProductOptionSet getById(MerchantStore store, Long optionSetId, Language lang) {
        return productOptionSetRepository.findOne(store.getId(), optionSetId, lang.getId());
    }


    @Override
    public ProductOptionSet getCode(MerchantStore store, String code) {
        return productOptionSetRepository.findByCode(store.getId(), code);
    }


    @Override
    public List<ProductOptionSet> getByProductType(Long productTypeId, MerchantStore store, Language lang) {
        return productOptionSetRepository.findByProductType(productTypeId, store.getId(), lang.getId());
    }


}
