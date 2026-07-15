package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.catalog.model.product.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductTypeList;
import com.asrevo.cvhome.catalog.service.mapper.catalog.PersistableProductTypeMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductTypeMapper;
import com.asrevo.cvhome.catalog.services.product.type.ProductTypeService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.commons.domain.LanguageCode;

@Service("productTypeFacade")
public class ProductTypeFacadeImpl implements ProductTypeFacade {

    private final ProductTypeService productTypeService;

    private final ReadableProductTypeMapper readableProductTypeMapper;

    private final PersistableProductTypeMapper persistableProductTypeMapper;

    public ProductTypeFacadeImpl(ProductTypeService productTypeService,
                                 ReadableProductTypeMapper readableProductTypeMapper,
                                 PersistableProductTypeMapper persistableProductTypeMapper) {
        this.productTypeService = productTypeService;
        this.readableProductTypeMapper = readableProductTypeMapper;
        this.persistableProductTypeMapper = persistableProductTypeMapper;
    }

    @Override
    public ReadableProductTypeList getByMerchant(StoreMerchantId store, LanguageCode language, Pageable pageable) {

        ReadableProductTypeList returnList = new ReadableProductTypeList();

        Page<ProductType> types = productTypeService.getByMerchant(store, language, pageable);

        returnList.setContent(types.getContent()
                .stream()
                .map(t -> readableProductTypeMapper.convert(t, store, language))
                .toList());
        returnList.setTotalPages(types.getTotalPages());
        returnList.setTotalElements(types.getTotalElements());
        returnList.setSize(types.getSize());
        returnList.setPageNumber(types.getNumber());

        return returnList;
    }

    @Override
    public ReadableProductType get(StoreMerchantId store, Long id, LanguageCode language) {
        try {

            ProductType type = productTypeService.getById(id, store);

            if (type == null) {
                throw new ResourceNotFoundException("Product type [" + id + "] not found for store [" + store + "]");
            }

            return readableProductTypeMapper.convert(type, store, language);

        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "An exception occured while getting product type [" + id + "] not found for store [" + store + "]",
                    e);
        }
    }

    @Override
    public Long save(PersistableProductType type, StoreMerchantId store, LanguageCode language) {

        try {

            if (this.exists(type.getCode(), store, language)) {
                throw new OperationNotAllowedException(
                        "Product type [" + type.getCode() + "] already exist for store [" + store + "]");
            }

            ProductType model = persistableProductTypeMapper.convert(type, store, language);
            model.setStoreMerchantId(store);
            ProductType saved = productTypeService.saveOrUpdate(model);
            return saved.getId();

        } catch (Exception e) {
            throw new ServiceRuntimeException("An exception occured while saving product type", e);
        }
    }

    @Override
    public void update(PersistableProductType type, Long id, StoreMerchantId store, LanguageCode language) {

        try {

            ProductType t = productTypeService.getById(id, store);
            if (t == null) {
                throw new ResourceNotFoundException(
                        "Product type [" + type.getCode() + "] does not exist for store [" + store + "]");
            }

            type.setId(t.getId());
            type.setCode(t.getCode());

            ProductType model = persistableProductTypeMapper.merge(type, t, store, language);
            model.setStoreMerchantId(store);
            productTypeService.saveOrUpdate(model);

        } catch (Exception e) {
            throw new ServiceRuntimeException("An exception occured while saving product type", e);
        }
    }

    @Override
    public void delete(Long id, StoreMerchantId store, LanguageCode language) {
        try {

            ProductType t = productTypeService.getById(id, store);
            if (t == null) {
                throw new ResourceNotFoundException(
                        "Product type [" + id + "] does not exist for store [" + store + "]");
            }

            productTypeService.delete(t);

        } catch (Exception e) {
            throw new ServiceRuntimeException("An exception occured while saving product type", e);
        }
    }

    @Override
    public boolean exists(String code, StoreMerchantId store, LanguageCode language) {
        ProductType t;
        t = productTypeService.getByCode(code, store, language);
        return t != null;
    }

    @Override
    public ReadableProductType get(StoreMerchantId store, String code, LanguageCode language) {
        ProductType t;
        t = productTypeService.getByCode(code, store, language);

        if (t == null) {
            throw new ResourceNotFoundException("Product type [" + code + "] not found for merchant [" + store + "]");
        }

        return readableProductTypeMapper.convert(t, store, language);
    }

}
