package com.asrevo.cvhome.catalog.services.product.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.entity.product.group.ProductGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface ProductGroupService extends SalesManagerEntityService<Long, ProductGroup> {

    void saveOrUpdate(ProductGroup productGroup) throws ServiceException;

    Optional<ProductGroup> getByCode(StoreMerchantId store, String code) throws ServiceException;

    Page<ProductGroup> listByStore(StoreMerchantId store, LanguageCode language, Pageable pageable);

    void delete(ProductGroup productGroup) throws ServiceException;

    List<ProductGroup> listByParentProduct(StoreMerchantId store, Long productId) throws ServiceException;

    Optional<ProductGroup> getByParentProductAndCode(StoreMerchantId store, Long productId, String code)
            throws ServiceException;

}
