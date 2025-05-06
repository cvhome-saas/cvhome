package com.asrevo.cvhome.catalog.services.product.manufacturer;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ManufacturerService extends SalesManagerEntityService<Long, Manufacturer> {

    List<Manufacturer> listByStore(StoreMerchantId store, LanguageCode language);

    List<Manufacturer> listByStore(StoreMerchantId store);

    Page<Manufacturer> listByStore(
            StoreMerchantId store, LanguageCode language, String name, Pageable pageable);

    void saveOrUpdate(Manufacturer manufacturer) throws ServiceException;

    void delete(Manufacturer manufacturer) throws ServiceException;

    Manufacturer getByCode(StoreMerchantId store, String code);

    /**
     * List by product in category lineage
     */
    List<Manufacturer> listByProductsInCategory(
            StoreMerchantId store, Category category, LanguageCode language);

    int count(StoreMerchantId store);
}
