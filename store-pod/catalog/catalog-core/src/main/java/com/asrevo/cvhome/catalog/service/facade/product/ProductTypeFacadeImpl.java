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
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;

@Service("productTypeFacade")
public class ProductTypeFacadeImpl implements ProductTypeFacade {

    private static final String PRODUCT_TYPE_MESSAGE = "Product type [";

    private static final String NOT_FOUND_FOR_STORE_MESSAGE = "] not found for store [";

    private static final String BRACKET_CLOSE = "]";

    private static final String SAVE_ERROR_MESSAGE = "An exception occured while saving product type";

    private static final String NOT_EXIST_FOR_STORE_MESSAGE = "] does not exist for store [";

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
                throw new ResourceNotFoundException(
                        PRODUCT_TYPE_MESSAGE + id + NOT_FOUND_FOR_STORE_MESSAGE + store + BRACKET_CLOSE);
            }

            return readableProductTypeMapper.convert(type, store, language);

        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "An exception occured while getting product type [%s] not found for store [%s]"
                            .formatted(id, store), e);
        }
    }

    @Override
    public Long save(PersistableProductType type, StoreMerchantId store, LanguageCode language) {

        try {

            if (this.exists(type.getCode(), store, language)) {
                throw new OperationNotAllowedException(
                        "Product type [%s] already exist for store [%s]".formatted(type.getCode(), store));
            }

            ProductType model = persistableProductTypeMapper.convert(type, store, language);
            model.setStoreMerchantId(store);
            ProductType saved = productTypeService.saveOrUpdate(model);
            return saved.getId();

        } catch (Exception e) {
            throw new ServiceRuntimeException(SAVE_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void update(PersistableProductType type, Long id, StoreMerchantId store, LanguageCode language) {

        try {

            ProductType t = productTypeService.getById(id, store);
            if (t == null) {
                throw new ResourceNotFoundException(
                        PRODUCT_TYPE_MESSAGE + type.getCode() + NOT_EXIST_FOR_STORE_MESSAGE + store + BRACKET_CLOSE);
            }

            type.setId(t.getId());
            type.setCode(t.getCode());

            ProductType model = persistableProductTypeMapper.merge(type, t, store, language);
            model.setStoreMerchantId(store);
            productTypeService.saveOrUpdate(model);

        } catch (Exception e) {
            throw new ServiceRuntimeException(SAVE_ERROR_MESSAGE, e);
        }
    }

    @Override
    public void delete(Long id, StoreMerchantId store, LanguageCode language) {
        try {

            ProductType t = productTypeService.getById(id, store);
            if (t == null) {
                throw new ResourceNotFoundException(
                        PRODUCT_TYPE_MESSAGE + id + NOT_EXIST_FOR_STORE_MESSAGE + store + BRACKET_CLOSE);
            }

            productTypeService.delete(t);

        } catch (Exception e) {
            throw new ServiceRuntimeException(SAVE_ERROR_MESSAGE, e);
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
            throw new ResourceNotFoundException(
                    "Product type [%s] not found for merchant [%s]".formatted(code, store));
        }

        return readableProductTypeMapper.convert(t, store, language);
    }

}
