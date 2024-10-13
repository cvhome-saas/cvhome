package com.asrevo.cvhome.store.core.services.catalog.product.attribute;

import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductOption;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import org.springframework.data.domain.Page;

public interface ProductOptionService extends SalesManagerEntityService<Long, ProductOption> {

    List<ProductOption> listByStore(MerchantStore store, Language language) throws ServiceException;

    List<ProductOption> getByName(MerchantStore store, String name, Language language)
            throws ServiceException;

    void saveOrUpdate(ProductOption entity) throws ServiceException;

    List<ProductOption> listReadOnly(MerchantStore store, Language language)
            throws ServiceException;

    ProductOption getByCode(MerchantStore store, String optionCode);

    ProductOption getById(MerchantStore store, Long optionId);

    Page<ProductOption> getByMerchant(
            MerchantStore store, Language language, String name, int page, int count);
}
