package com.asrevo.cvhome.store.core.services.catalog.product.manufacturer;

import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.manufacturer.ManufacturerRepository;
import com.asrevo.cvhome.store.core.repositories.catalog.product.manufacturer.PageableManufacturerRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.HashSet;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("manufacturerService")
@Slf4j
public class ManufacturerServiceImpl extends SalesManagerEntityServiceImpl<Long, Manufacturer>
        implements ManufacturerService {

    private final PageableManufacturerRepository pageableManufacturerRepository;

    private final ManufacturerRepository manufacturerRepository;

    @Autowired
    public ManufacturerServiceImpl(
            ManufacturerRepository manufacturerRepository,
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
    public Long getCountManufAttachedProducts(Manufacturer manufacturer) throws ServiceException {
        return manufacturerRepository.countByProduct(manufacturer.getId());
        // .getCountManufAttachedProducts( manufacturer );
    }

    @Override
    public List<Manufacturer> listByStore(MerchantStore store, Language language)
            throws ServiceException {
        return manufacturerRepository.findByStoreAndLanguage(store.getId(), language.getId());
    }

    @Override
    public List<Manufacturer> listByStore(MerchantStore store) throws ServiceException {
        return manufacturerRepository.findByStore(store.getId());
    }

    @Override
    public List<Manufacturer> listByProductsByCategoriesId(
            MerchantStore store, List<Long> ids, Language language) throws ServiceException {
        return manufacturerRepository.findByCategoriesAndLanguage(ids, language.getId());
    }

    @Override
    public void addManufacturerDescription(
            Manufacturer manufacturer, ManufacturerDescription description)
            throws ServiceException {

        if (manufacturer.getDescriptions() == null) {
            manufacturer.setDescriptions(new HashSet<>());
        }

        manufacturer.getDescriptions().add(description);
        description.setManufacturer(manufacturer);
        update(manufacturer);
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
    public Manufacturer getByCode(MerchantStore store, String code) {
        return manufacturerRepository.findByCodeAndMerchandStore(code, store.getId());
    }

    @Override
    public Manufacturer getById(Long id) {
        return manufacturerRepository.findOne(id);
    }

    @Override
    public List<Manufacturer> listByProductsInCategory(
            MerchantStore store, Category category, Language language) throws ServiceException {
        Assert.notNull(store, "Store cannot be null");
        Assert.notNull(category, "Category cannot be null");
        Assert.notNull(language, "Language cannot be null");
        return manufacturerRepository.findByProductInCategoryId(
                store.getId(), category.getLineage(), language.getId());
    }

    @Override
    public Page<Manufacturer> listByStore(
            MerchantStore store, Language language, int page, int count) throws ServiceException {

        Pageable pageRequest = PageRequest.of(page, count);
        return pageableManufacturerRepository.findByStore(
                store.getId(), language.getId(), null, pageRequest);
    }

    @Override
    public int count(MerchantStore store) {
        Assert.notNull(store, "Merchant must not be null");
        return manufacturerRepository.count(store.getId());
    }

    @Override
    public Page<Manufacturer> listByStore(
            MerchantStore store, Language language, String name, int page, int count)
            throws ServiceException {

        Pageable pageRequest = PageRequest.of(page, count);
        return pageableManufacturerRepository.findByStore(
                store.getId(), language.getId(), name, pageRequest);
    }

    @Override
    public Page<Manufacturer> listByStore(MerchantStore store, String name, int page, int count)
            throws ServiceException {

        Pageable pageRequest = PageRequest.of(page, count);
        return pageableManufacturerRepository.findByStore(store.getId(), name, pageRequest);
    }
}
