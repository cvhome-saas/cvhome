package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.variation.ProductVariation;
import com.asrevo.cvhome.catalog.model.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.catalog.model.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductVariationMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductVariationMapper;
import com.asrevo.cvhome.catalog.services.product.variation.ProductVariationService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

@Service
public class ProductVariationFacadeImpl implements ProductVariationFacade {

    private static final String PRODUCT_VARIATION_NOT_FOUND_MESSAGE = "ProductVariation not found for id [";

    private static final String AND_STORE_MESSAGE = "] and store [";

    private static final String BRACKET_CLOSE = "]";

    private final PersistableProductVariationMapper persistableProductVariationMapper;

    private final ReadableProductVariationMapper readableProductVariationMapper;

    private final ProductVariationService productVariationService;

    public ProductVariationFacadeImpl(PersistableProductVariationMapper persistableProductVariationMapper,
                                      ReadableProductVariationMapper readableProductVariationMapper,
                                      ProductVariationService productVariationService) {
        this.persistableProductVariationMapper = persistableProductVariationMapper;
        this.readableProductVariationMapper = readableProductVariationMapper;
        this.productVariationService = productVariationService;
    }

    @Override
    public ReadableProductVariation get(Long variationId, StoreMerchantId store, LanguageCode language) {
        Optional<ProductVariation> variation = productVariationService.getById(store, variationId, language);
        if (variation.isEmpty()) {
            throw new ResourceNotFoundException(
                    PRODUCT_VARIATION_NOT_FOUND_MESSAGE + variationId + AND_STORE_MESSAGE + store + BRACKET_CLOSE);
        }

        return readableProductVariationMapper.convert(variation.get(), store, language);
    }

    @Override
    public ReadableEntityList<ReadableProductVariation> list(StoreMerchantId store, LanguageCode language,
                                                             Pageable pageable) {
        Page<ProductVariation> vars = productVariationService.getByMerchant(store, language, null, pageable);
        List<ReadableProductVariation> variations = vars.stream()
                .map(opt -> this.convert(opt, store, language))
                .toList();
        ReadableEntityList<ReadableProductVariation> returnList = new ReadableEntityList<>();
        returnList.setContent(variations);
        returnList.setSize(variations.size());
        returnList.setTotalElements(vars.getTotalElements());
        returnList.setTotalPages(vars.getTotalPages());
        returnList.setPageNumber(vars.getNumber());
        return returnList;
    }

    private ReadableProductVariation convert(ProductVariation variation, StoreMerchantId store, LanguageCode language) {
        return readableProductVariationMapper.convert(variation, store, language);
    }

    @Override
    public Long create(PersistableProductVariation variation, StoreMerchantId store, LanguageCode language) {

        if (this.exists(variation.getCode(), store)) {
            throw new OperationNotAllowedException(
                    "Option set with code [%s] already exist".formatted(variation.getCode()));
        }

        ProductVariation p = persistableProductVariationMapper.convert(variation, store, language);
        p.setStoreMerchantId(store);
        try {
            productVariationService.saveOrUpdate(p);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while creating ProductOptionSet", e);
        }

        return p.getId();
    }

    @Override
    public void update(Long variationId, PersistableProductVariation variation, StoreMerchantId store,
                       LanguageCode language) {

        Optional<ProductVariation> p = productVariationService.getById(store, variationId, language);
        if (p.isEmpty()) {
            throw new ResourceNotFoundException(
                    PRODUCT_VARIATION_NOT_FOUND_MESSAGE + variationId + AND_STORE_MESSAGE + store + BRACKET_CLOSE);
        }

        ProductVariation productVariant = p.get();

        productVariant.setId(variationId);
        productVariant.setCode(variation.getCode());
        ProductVariation model = persistableProductVariationMapper.merge(variation, productVariant, store, language);
        model.setStoreMerchantId(store);
        productVariationService.save(model);
    }

    @Override
    public void delete(Long variationId, StoreMerchantId store) {
        ProductVariation opt = productVariationService.getById(variationId);
        if (opt == null) {
            throw new ResourceNotFoundException(
                    PRODUCT_VARIATION_NOT_FOUND_MESSAGE + variationId + AND_STORE_MESSAGE + store + BRACKET_CLOSE);
        }
        if (!opt.getStoreMerchantId().equals(store)) {
            throw new ResourceNotFoundException(
                    PRODUCT_VARIATION_NOT_FOUND_MESSAGE + variationId + AND_STORE_MESSAGE + store + BRACKET_CLOSE);
        }
        try {
            productVariationService.delete(opt);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while deleting ProductVariation", e);
        }
    }

    @Override
    public boolean exists(String code, StoreMerchantId store) {
        Optional<ProductVariation> productVariation = productVariationService.getByCode(store, code);
        return productVariation.isPresent();
    }

}
