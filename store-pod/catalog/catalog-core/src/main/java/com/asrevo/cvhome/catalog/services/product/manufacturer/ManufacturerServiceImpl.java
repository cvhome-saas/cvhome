package com.asrevo.cvhome.catalog.services.product.manufacturer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.repositories.product.manufacturer.ManufacturerRepository;
import com.asrevo.cvhome.catalog.repositories.product.manufacturer.PageableManufacturerRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Service("manufacturerService")
@Slf4j
public class ManufacturerServiceImpl extends SalesManagerEntityServiceImpl<Long, Manufacturer>
        implements ManufacturerService {

    private final PageableManufacturerRepository pageableManufacturerRepository;

    private final ManufacturerRepository manufacturerRepository;

    @Autowired
    public ManufacturerServiceImpl(ManufacturerRepository manufacturerRepository,
                                   PageableManufacturerRepository pageableManufacturerRepository) {
        super(manufacturerRepository);
        this.manufacturerRepository = manufacturerRepository;
        this.pageableManufacturerRepository = pageableManufacturerRepository;
    }

    @Override
    public void delete(Manufacturer manufacturer) throws ServiceException {
        manufacturer = this.getById(manufacturer.getId());
        super.delete(manufacturer);
    }

    @Override
    public List<Manufacturer> listByStore(StoreMerchantId store, LanguageCode language) {
        return manufacturerRepository.findByStoreAndLanguage(store, language);
    }

    @Override
    public List<Manufacturer> listByStore(StoreMerchantId store) {
        return manufacturerRepository.findByStore(store);
    }

    @Override
    public void saveOrUpdate(Manufacturer manufacturer) throws ServiceException {

        log.debug("Creating Manufacturer");

        if (manufacturer.getId() != null && manufacturer.getId() > 0) {
            super.update(manufacturer);

        } else {
            super.create(manufacturer);
        }
    }

    @Override
    public Manufacturer getByCode(StoreMerchantId store, String code) {
        return manufacturerRepository.findByCodeAndMerchandStore(code, store);
    }

    @Override
    public Manufacturer getById(Long id) {
        return manufacturerRepository.findOne(id);
    }

    @Override
    public List<Manufacturer> listByProductsInCategory(StoreMerchantId store, Category category,
                                                       LanguageCode language) {
        Assert.notNull(store, "Store cannot be null");
        Assert.notNull(category, "Category cannot be null");
        Assert.notNull(language, "Language cannot be null");
        return manufacturerRepository.findByProductInCategoryId(store, category.getLineage(), language);
    }

    @Override
    public int count(StoreMerchantId store) {
        Assert.notNull(store, "Merchant must not be null");
        return manufacturerRepository.count(store);
    }

    @Override
    public Page<Manufacturer> listByStore(StoreMerchantId store, LanguageCode language, String name,
                                          Pageable pageable) {
        return pageableManufacturerRepository.findByStore(store, language, name, pageable);
    }

}
