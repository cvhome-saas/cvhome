package com.asrevo.cvhome.store.core.services.catalog.product.attribute;

import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.attribute.PageableProductOptionValueRepository;
import com.asrevo.cvhome.store.core.repositories.catalog.product.attribute.ProductOptionValueRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service("productOptionValueService")
public class ProductOptionValueServiceImpl extends
        SalesManagerEntityServiceImpl<Long, ProductOptionValue> implements
        ProductOptionValueService {

    @Autowired
    private ProductAttributeService productAttributeService;

    @Autowired
    private PageableProductOptionValueRepository pageableProductOptionValueRepository;

    private ProductOptionValueRepository productOptionValueRepository;

    @Autowired
    public ProductOptionValueServiceImpl(
            ProductOptionValueRepository productOptionValueRepository) {
        super(productOptionValueRepository);
        this.productOptionValueRepository = productOptionValueRepository;
    }


    @Override
    public List<ProductOptionValue> listByStore(MerchantStore store, Language language) throws ServiceException {

        return productOptionValueRepository.findByStoreId(store.getId(), language.getId());
    }

    @Override
    public List<ProductOptionValue> listByStoreNoReadOnly(MerchantStore store, Language language) throws ServiceException {

        return productOptionValueRepository.findByReadOnly(store.getId(), language.getId(), false);
    }

    @Override
    public List<ProductOptionValue> getByName(MerchantStore store, String name, Language language) throws ServiceException {

        try {
            return productOptionValueRepository.findByName(store.getId(), name, language.getId());
        } catch (Exception e) {
            throw new ServiceException(e);
        }


    }

    @Override
    public void saveOrUpdate(ProductOptionValue entity) throws ServiceException {


        //save or update (persist and attach entities
        if (entity.getId() != null && entity.getId() > 0) {

            super.update(entity);

        } else {

            super.save(entity);

        }

    }


    public void delete(ProductOptionValue entity) throws ServiceException {

        //remove all attributes having this option
        List<ProductAttribute> attributes = productAttributeService.getByOptionValueId(entity.getMerchantStore(), entity.getId());

        for (ProductAttribute attribute : attributes) {
            productAttributeService.delete(attribute);
        }

        ProductOptionValue option = getById(entity.getId());

        //remove option
        super.delete(option);

    }

    @Override
    public ProductOptionValue getByCode(MerchantStore store, String optionValueCode) {
        return productOptionValueRepository.findByCode(store.getId(), optionValueCode);
    }


    @Override
    public ProductOptionValue getById(MerchantStore store, Long optionValueId) {
        return productOptionValueRepository.findOne(store.getId(), optionValueId);
    }


    @Override
    public Page<ProductOptionValue> getByMerchant(MerchantStore store, Language language, String name, int page,
                                                  int count) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Pageable p = PageRequest.of(page, count);
        return pageableProductOptionValueRepository.listOptionValues(store.getId(), name, p);
    }


}
