package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductOptionSetMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductOptionSetMapper;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionSetService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

@Service
public class ProductOptionSetFacadeImpl implements ProductOptionSetFacade {

    private final PersistableProductOptionSetMapper persistableProductOptionSetMapper;

    private final ReadableProductOptionSetMapper readableProductOptionSetMapper;

    private final ProductOptionSetService productOptionSetService;

    private final ProductTypeFacade productTypeFacade;

    public ProductOptionSetFacadeImpl(PersistableProductOptionSetMapper persistableProductOptionSetMapper,
                                      ReadableProductOptionSetMapper readableProductOptionSetMapper,
                                      ProductOptionSetService productOptionSetService, ProductTypeFacade productTypeFacade) {
        this.persistableProductOptionSetMapper = persistableProductOptionSetMapper;
        this.readableProductOptionSetMapper = readableProductOptionSetMapper;
        this.productOptionSetService = productOptionSetService;
        this.productTypeFacade = productTypeFacade;
    }

    @Override
    public ReadableProductOptionSet get(Long id, StoreMerchantId store, LanguageCode language) {
        ProductOptionSet optionSet = productOptionSetService.getById(store, id, language);
        if (optionSet == null) {
            throw new ResourceNotFoundException(
                    "ProductOptionSet not found for id [" + id + "] and store [" + store + "]");
        }

        return readableProductOptionSetMapper.convert(optionSet, store, language);
    }

    @Override
    public List<ReadableProductOptionSet> list(StoreMerchantId store, LanguageCode language) {
        List<ProductOptionSet> optionSets = productOptionSetService.listByStore(store, language);
        return optionSets.stream().map(opt -> this.convert(opt, store, language)).toList();
    }

    private ReadableProductOptionSet convert(ProductOptionSet optionSet, StoreMerchantId store, LanguageCode language) {
        return readableProductOptionSetMapper.convert(optionSet, store, language);
    }

    @Override
    public void create(PersistableProductOptionSet optionSet, StoreMerchantId store, LanguageCode language) {
        if (this.exists(optionSet.getCode(), store)) {
            throw new OperationNotAllowedException("Option set with code [" + optionSet.getCode() + "] already exist");
        }

        ProductOptionSet opt = persistableProductOptionSetMapper.convert(optionSet, store, language);
        try {
            opt.setStoreMerchantId(store);
            productOptionSetService.create(opt);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while creating ProductOptionSet", e);
        }
    }

    @Override
    public void update(Long id, PersistableProductOptionSet optionSet, StoreMerchantId store, LanguageCode language) {
        ProductOptionSet opt = productOptionSetService.getById(store, id, language);
        if (opt == null) {
            throw new ResourceNotFoundException(
                    "ProductOptionSet not found for id [" + id + "] and store [" + store + "]");
        }

        optionSet.setId(id);
        optionSet.setCode(opt.getCode());
        ProductOptionSet model = persistableProductOptionSetMapper.convert(optionSet, store, language);
        model.setStoreMerchantId(store);
        productOptionSetService.save(model);
    }

    @Override
    public void delete(Long id, StoreMerchantId store) {
        ProductOptionSet opt = productOptionSetService.getById(id);
        if (opt == null) {
            throw new ResourceNotFoundException(
                    "ProductOptionSet not found for id [" + id + "] and store [" + store + "]");
        }
        if (!opt.getStoreMerchantId().equals(store)) {
            throw new ResourceNotFoundException(
                    "ProductOptionSet not found for id [" + id + "] and store [" + store + "]");
        }
        try {
            productOptionSetService.delete(opt);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while deleting ProductOptionSet", e);
        }
    }

    @Override
    public boolean exists(String code, StoreMerchantId store) {
        ProductOptionSet optionSet = productOptionSetService.getCode(store, code);
        return optionSet != null;
    }

    @Override
    public List<ReadableProductOptionSet> list(StoreMerchantId store, LanguageCode language, String type) {
        ReadableProductType readable = productTypeFacade.get(store, type, language);

        if (readable == null) {
            throw new ResourceNotFoundException("Can't fing product type [" + type + "] fpr merchand [" + store + "]");
        }

        List<ProductOptionSet> optionSets = productOptionSetService.getByProductType(readable.getId(), store, language);
        return optionSets.stream().map(opt -> this.convert(opt, store, language)).toList();
    }

}
