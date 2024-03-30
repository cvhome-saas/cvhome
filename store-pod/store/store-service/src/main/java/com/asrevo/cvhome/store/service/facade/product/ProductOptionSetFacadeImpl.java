package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import com.asrevo.cvhome.store.core.services.catalog.product.attribute.ProductOptionSetService;
import com.asrevo.cvhome.store.service.mapper.catalog.PersistableProductOptionSetMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableProductOptionSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductOptionSetFacadeImpl implements ProductOptionSetFacade {

    @Autowired
    private PersistableProductOptionSetMapper persistableProductOptionSetMapper;

    @Autowired
    private ReadableProductOptionSetMapper readableProductOptionSetMapper;

    @Autowired
    private ProductOptionSetService productOptionSetService;

    @Autowired
    private ProductTypeFacade productTypeFacade;

    @Override
    public ReadableProductOptionSet get(Long id, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        ProductOptionSet optionSet = productOptionSetService.getById(store, id, language);
        if (optionSet == null) {
            throw new ResourceNotFoundException(
                    "ProductOptionSet not found for id [" + id + "] and store [" + store.getCode() + "]");
        }

        return readableProductOptionSetMapper.convert(optionSet, store, language);
    }

    @Override
    public List<ReadableProductOptionSet> list(MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");

        try {
            List<ProductOptionSet> optionSets = productOptionSetService.listByStore(store, language);
            return optionSets.stream().map(opt -> this.convert(opt, store, language)).collect(Collectors.toList());
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while listing ProductOptionSet", e);
        }

    }

    private ReadableProductOptionSet convert(ProductOptionSet optionSet, MerchantStore store, Language language) {
        return readableProductOptionSetMapper.convert(optionSet, store, language);
    }

    @Override
    public void create(PersistableProductOptionSet optionSet, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(optionSet, "PersistableProductOptionSet cannot be null");

        if (this.exists(optionSet.getCode(), store)) {
            throw new OperationNotAllowedException("Option set with code [" + optionSet.getCode() + "] already exist");
        }

        ProductOptionSet opt = persistableProductOptionSetMapper.convert(optionSet, store, language);
        try {
            opt.setStore(store);
            productOptionSetService.create(opt);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while creating ProductOptionSet", e);
        }

    }

    @Override
    public void update(Long id, PersistableProductOptionSet optionSet, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(optionSet, "PersistableProductOptionSet cannot be null");

        ProductOptionSet opt = productOptionSetService.getById(store, id, language);
        if (opt == null) {
            throw new ResourceNotFoundException(
                    "ProductOptionSet not found for id [" + id + "] and store [" + store.getCode() + "]");
        }

        optionSet.setId(id);
        optionSet.setCode(opt.getCode());
        ProductOptionSet model = persistableProductOptionSetMapper.convert(optionSet, store, language);
        try {
            model.setStore(store);
            productOptionSetService.save(model);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while creating ProductOptionSet", e);
        }

    }

    @Override
    public void delete(Long id, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(id, "id cannot be null");
        ProductOptionSet opt = productOptionSetService.getById(id);
        if (opt == null) {
            throw new ResourceNotFoundException(
                    "ProductOptionSet not found for id [" + id + "] and store [" + store.getCode() + "]");
        }
        if (!opt.getStore().getCode().equals(store.getCode())) {
            throw new ResourceNotFoundException(
                    "ProductOptionSet not found for id [" + id + "] and store [" + store.getCode() + "]");
        }
        try {
            productOptionSetService.delete(opt);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while deleting ProductOptionSet", e);
        }

    }

    @Override
    public boolean exists(String code, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(code, "code cannot be null");
        ProductOptionSet optionSet = productOptionSetService.getCode(store, code);
        return optionSet != null;
    }

    @Override
    public List<ReadableProductOptionSet> list(MerchantStore store, Language language, String type) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(type, "Product type cannot be null");

        // find product type by id
        ReadableProductType readable = productTypeFacade.get(store, type, language);

        if (readable == null) {
            throw new ResourceNotFoundException("Can't fing product type [" + type + "] fpr merchand [" + store.getCode() + "]");
        }

        List<ProductOptionSet> optionSets = productOptionSetService.getByProductType(readable.getId(), store, language);
        return optionSets.stream().map(opt -> this.convert(opt, store, language)).collect(Collectors.toList());

    }

}
