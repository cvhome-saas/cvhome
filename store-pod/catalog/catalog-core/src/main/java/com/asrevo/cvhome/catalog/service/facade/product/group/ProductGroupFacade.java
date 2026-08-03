package com.asrevo.cvhome.catalog.service.facade.product.group;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.ProductGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupListV2;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;

public interface ProductGroupFacade {

    ReadableProductGroup getByCode(StoreMerchantId store, String code, LanguageCode language)
            throws ProductGroupNotFoundException, ProductNotConvertibleException;

    ReadableProductGroupListV2 list(StoreMerchantId store, LanguageCode language, Pageable pageable);

    void delete(StoreMerchantId store, String code)
            throws ProductGroupNotFoundException, ServiceException;

    PersistableProductGroup saveProductGroup(StoreMerchantId store, PersistableProductGroup productGroup)
            throws ServiceException;

    void addProductToGroup(StoreMerchantId store, String groupCode, Long productId)
            throws ProductGroupNotFoundException, ProductNotFoundException, ServiceException;

    void removeProductFromGroup(StoreMerchantId store, String groupCode, Long productId)
            throws ProductGroupNotFoundException, ServiceException;

    boolean existByCode(StoreMerchantId store, String code);

    ReadableProductGroup getByCodeAndParent(StoreMerchantId store, Long productId, String code, LanguageCode language)
            throws ProductGroupNotFoundException, ProductNotConvertibleException;

    void addProductToGroupForParent(StoreMerchantId store, Long parentProductId, String code, Long productId)
            throws ProductNotFoundException, ServiceException;

    void removeProductFromGroupForParent(StoreMerchantId store, Long parentProductId, String code, Long productId)
            throws ProductGroupNotFoundException, ServiceException;

}
