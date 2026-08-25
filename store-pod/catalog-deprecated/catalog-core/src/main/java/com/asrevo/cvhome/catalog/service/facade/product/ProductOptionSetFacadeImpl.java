package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionSet;
import com.asrevo.cvhome.catalog.errors.DuplicateProductOptionSetException;
import com.asrevo.cvhome.catalog.errors.ProductOptionSetNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductTypeNotFoundException;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductOptionSetMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductOptionSetMapper;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductOptionSetService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

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
    public ReadableProductOptionSet get(Long id, StoreMerchantId store, LanguageCode language)
            throws ProductOptionSetNotFoundException {
        ProductOptionSet optionSet = productOptionSetService.getById(store, id, language);
        if (optionSet == null) {
            throw ProductOptionSetNotFoundException.of(id, store);
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
    public void create(PersistableProductOptionSet optionSet, StoreMerchantId store, LanguageCode language)
            throws DuplicateProductOptionSetException {
        if (this.exists(optionSet.getCode(), store)) {
            throw DuplicateProductOptionSetException.of(optionSet.getCode(), store);
        }

        ProductOptionSet opt = persistableProductOptionSetMapper.convert(optionSet, store, language);
        opt.setStoreMerchantId(store);
        productOptionSetService.create(opt);
    }

    @Override
    public void update(Long id, PersistableProductOptionSet optionSet, StoreMerchantId store, LanguageCode language)
            throws ProductOptionSetNotFoundException {
        ProductOptionSet opt = productOptionSetService.getById(store, id, language);
        if (opt == null) {
            throw ProductOptionSetNotFoundException.of(id, store);
        }

        optionSet.setId(id);
        optionSet.setCode(opt.getCode());
        ProductOptionSet model = persistableProductOptionSetMapper.convert(optionSet, store, language);
        model.setStoreMerchantId(store);
        productOptionSetService.save(model);
    }

    @Override
    public void delete(Long id, StoreMerchantId store) throws ProductOptionSetNotFoundException {
        ProductOptionSet opt = productOptionSetService.getById(id);
        if (opt == null || !opt.getStoreMerchantId().equals(store)) {
            throw ProductOptionSetNotFoundException.of(id, store);
        }
        productOptionSetService.delete(opt);
    }

    @Override
    public boolean exists(String code, StoreMerchantId store) {
        ProductOptionSet optionSet = productOptionSetService.getCode(store, code);
        return optionSet != null;
    }

    @Override
    public List<ReadableProductOptionSet> list(StoreMerchantId store, LanguageCode language, String type)
            throws ProductTypeNotFoundException {
        // productTypeFacade.get now throws rather than returning null, so the null check that followed it is gone.
        ReadableProductType readable = productTypeFacade.get(store, type, language);

        List<ProductOptionSet> optionSets = productOptionSetService.getByProductType(readable.getId(), store, language);
        return optionSets.stream().map(opt -> this.convert(opt, store, language)).toList();
    }

}
