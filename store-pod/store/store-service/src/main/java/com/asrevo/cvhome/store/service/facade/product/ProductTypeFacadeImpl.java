package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.type.PersistableProductType;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductTypeList;
import com.asrevo.cvhome.store.core.services.catalog.product.type.ProductTypeService;
import com.asrevo.cvhome.store.service.mapper.catalog.PersistableProductTypeMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableProductTypeMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.stream.Collectors;

@Service("productTypeFacade")
public class ProductTypeFacadeImpl implements ProductTypeFacade {

    private final ProductTypeService productTypeService;

    private final ReadableProductTypeMapper readableProductTypeMapper;

    private final PersistableProductTypeMapper persistableProductTypeMapper;

    public ProductTypeFacadeImpl(ProductTypeService productTypeService, ReadableProductTypeMapper readableProductTypeMapper, PersistableProductTypeMapper persistableProductTypeMapper) {
        this.productTypeService = productTypeService;
        this.readableProductTypeMapper = readableProductTypeMapper;
        this.persistableProductTypeMapper = persistableProductTypeMapper;
    }

    @Override
    public ReadableProductTypeList getByMerchant(MerchantStore store, Language language, int count, int page) {

        Assert.notNull(store, "MerchantStore cannot be null");
        ReadableProductTypeList returnList = new ReadableProductTypeList();

        try {

            Page<ProductType> types = productTypeService.getByMerchant(store, language, page, count);

            if (types != null) {
                returnList.setList(types.getContent().stream().map(t -> readableProductTypeMapper.convert(t, store, language)).collect(Collectors.toList()));
                returnList.setTotalPages(types.getTotalPages());
                returnList.setRecordsTotal(types.getTotalElements());
                returnList.setRecordsFiltered(types.getSize());
            }

            return returnList;
        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "An exception occured while getting product types for merchant[ " + store.getCode() + "]", e);
        }

    }

    @Override
    public ReadableProductType get(MerchantStore store, Long id, Language language) {

        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(id, "ProductType code cannot be empty");
        try {

            ProductType type = null;
            if (language == null) {
                type = productTypeService.getById(id, store);
            } else {
                type = productTypeService.getById(id, store, language);
            }

            if (type == null) {
                throw new ResourceNotFoundException("Product type [" + id + "] not found for store [" + store.getCode() + "]");
            }

            ReadableProductType readableType = readableProductTypeMapper.convert(type, store, language);


            return readableType;

        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "An exception occured while getting product type [" + id + "] not found for store [" + store.getCode() + "]", e);
        }

    }

    @Override
    public Long save(PersistableProductType type, MerchantStore store, Language language) {

        Assert.notNull(type, "ProductType cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(type.getCode(), "ProductType code cannot be empty");

        try {

            if (this.exists(type.getCode(), store, language)) {
                throw new OperationNotAllowedException(
                        "Product type [" + type.getCode() + "] already exist for store [" + store.getCode() + "]");
            }

            ProductType model = persistableProductTypeMapper.convert(type, store, language);
            model.setMerchantStore(store);
            ProductType saved = productTypeService.saveOrUpdate(model);
            return saved.getId();

        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "An exception occured while saving product type", e);
        }

    }

    @Override
    public void update(PersistableProductType type, Long id, MerchantStore store, Language language) {
        Assert.notNull(type, "ProductType cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(id, "id cannot be empty");

        try {

            ProductType t = productTypeService.getById(id, store, language);
            if (t == null) {
                throw new ResourceNotFoundException(
                        "Product type [" + type.getCode() + "] does not exist for store [" + store.getCode() + "]");
            }

            type.setId(t.getId());
            type.setCode(t.getCode());

            ProductType model = persistableProductTypeMapper.merge(type, t, store, language);
            model.setMerchantStore(store);
            productTypeService.saveOrUpdate(model);

        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "An exception occured while saving product type", e);
        }

    }

    @Override
    public void delete(Long id, MerchantStore store, Language language) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(id, "id cannot be empty");

        try {

            ProductType t = productTypeService.getById(id, store, language);
            if (t == null) {
                throw new ResourceNotFoundException(
                        "Product type [" + id + "] does not exist for store [" + store.getCode() + "]");
            }

            productTypeService.delete(t);


        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "An exception occured while saving product type", e);
        }

    }

    @Override
    public boolean exists(String code, MerchantStore store, Language language) {
        ProductType t;
        try {
            t = productTypeService.getByCode(code, store, language);
        } catch (ServiceException e) {
            throw new RuntimeException("An exception occured while getting product type [" + code + "] for merchant store [" + store.getCode() + "]", e);
        }
        return t != null;
    }

    @Override
    public ReadableProductType get(MerchantStore store, String code, Language language) {
        ProductType t;
        try {
            t = productTypeService.getByCode(code, store, language);
        } catch (ServiceException e) {
            throw new RuntimeException("An exception occured while getting product type [" + code + "] for merchant store [" + store.getCode() + "]", e);
        }

        if (t == null) {
            throw new ResourceNotFoundException("Product type [" + code + "] not found for merchant [" + store.getCode() + "]");
        }

        ReadableProductType readableType = readableProductTypeMapper.convert(t, store, language);
        return readableType;

    }


}
