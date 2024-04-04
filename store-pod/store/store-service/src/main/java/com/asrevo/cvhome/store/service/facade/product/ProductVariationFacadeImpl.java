package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.variation.ProductVariation;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.services.catalog.product.variation.ProductVariationService;
import com.asrevo.cvhome.store.service.mapper.catalog.PersistableProductVariationMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableProductVariationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductVariationFacadeImpl implements ProductVariationFacade {

    private final PersistableProductVariationMapper persistableProductVariationMapper;

    private final ReadableProductVariationMapper readableProductVariationMapper;

    private final ProductVariationService productVariationService;

    public ProductVariationFacadeImpl(PersistableProductVariationMapper persistableProductVariationMapper, ReadableProductVariationMapper readableProductVariationMapper, ProductVariationService productVariationService) {
        this.persistableProductVariationMapper = persistableProductVariationMapper;
        this.readableProductVariationMapper = readableProductVariationMapper;
        this.productVariationService = productVariationService;
    }


    @Override
    public ReadableProductVariation get(Long variationId, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Optional<ProductVariation> variation = productVariationService.getById(store, variationId, language);
        if (variation.isEmpty()) {
            throw new ResourceNotFoundException("ProductVariation not found for id [" + variationId + "] and store [" + store.getCode() + "]");
        }

        return readableProductVariationMapper.convert(variation.get(), store, language);
    }

    @Override
    public ReadableEntityList<ReadableProductVariation> list(MerchantStore store, Language language, int page, int count) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");


        Page<ProductVariation> vars = productVariationService.getByMerchant(store, language, null, page, count);
        List<ReadableProductVariation> variations = vars.stream().map(opt -> this.convert(opt, store, language)).collect(Collectors.toList());
        ReadableEntityList<ReadableProductVariation> returnList = new ReadableEntityList<ReadableProductVariation>();
        returnList.setItems(variations);
        returnList.setNumber(variations.size());
        returnList.setRecordsTotal(vars.getTotalElements());
        returnList.setTotalPages(vars.getTotalPages());
        return returnList;


    }

    private ReadableProductVariation convert(ProductVariation var, MerchantStore store, Language language) {
        return readableProductVariationMapper.convert(var, store, language);
    }

    @Override
    public Long create(PersistableProductVariation var, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(var, "PersistableProductVariation cannot be null");

        if (this.exists(var.getCode(), store)) {
            throw new OperationNotAllowedException("Option set with code [" + var.getCode() + "] already exist");
        }

        ProductVariation p = persistableProductVariationMapper.convert(var, store, language);
        p.setMerchantStore(store);
        try {
            productVariationService.saveOrUpdate(p);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while creating ProductOptionSet", e);
        }

        return p.getId();

    }


    @Override
    public void update(Long variationId, PersistableProductVariation var, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(var, "PersistableProductVariation cannot be null");

        Optional<ProductVariation> p = productVariationService.getById(store, variationId, language);
        if (p.isEmpty()) {
            throw new ResourceNotFoundException("ProductVariation not found for id [" + variationId + "] and store [" + store.getCode() + "]");
        }

        ProductVariation productVariant = p.get();

        productVariant.setId(variationId);
        productVariant.setCode(var.getCode());
        ProductVariation model = persistableProductVariationMapper.merge(var, productVariant, store, language);
        try {
            model.setMerchantStore(store);
            productVariationService.save(model);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while creating ProductVariation", e);
        }

    }

    @Override
    public void delete(Long variationId, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(variationId, "variationId cannot be null");
        ProductVariation opt = productVariationService.getById(variationId);
        if (opt == null) {
            throw new ResourceNotFoundException("ProductVariation not found for id [" + variationId + "] and store [" + store.getCode() + "]");
        }
        if (!opt.getMerchantStore().getCode().equals(store.getCode())) {
            throw new ResourceNotFoundException("ProductVariation not found for id [" + variationId + "] and store [" + store.getCode() + "]");
        }
        try {
            productVariationService.delete(opt);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while deleting ProductVariation", e);
        }

    }

    @Override
    public boolean exists(String code, MerchantStore store) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(code, "code cannot be null");
        Optional<ProductVariation> var = productVariationService.getByCode(store, code);
        return var.isPresent();
    }

}
