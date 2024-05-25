package com.asrevo.cvhome.store.service.facade.manufacturer;

import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.store.core.model.catalog.manufacturer.ReadableManufacturerList;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;
import com.asrevo.cvhome.store.core.services.catalog.category.CategoryService;
import com.asrevo.cvhome.store.core.services.catalog.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.service.populator.manufacturer.PersistableManufacturerPopulator;
import com.asrevo.cvhome.store.service.populator.manufacturer.ReadableManufacturerPopulator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service("manufacturerFacade")
public class ManufacturerFacadeImpl implements ManufacturerFacade {

    private final Mapper<Manufacturer, ReadableManufacturer> readableManufacturerConverter;


    private final ManufacturerService manufacturerService;

    private final CategoryService categoryService;

    private final LanguageService languageService;

    public ManufacturerFacadeImpl(Mapper<Manufacturer, ReadableManufacturer> readableManufacturerConverter, ManufacturerService manufacturerService, CategoryService categoryService, LanguageService languageService) {
        this.readableManufacturerConverter = readableManufacturerConverter;
        this.manufacturerService = manufacturerService;
        this.categoryService = categoryService;
        this.languageService = languageService;
    }

    @Override
    public List<ReadableManufacturer> getByProductInCategory(MerchantStore store, Language language,
                                                             Long categoryId) {
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Assert.notNull(categoryId, "Category id cannot be null");

        Category category = categoryService.getById(categoryId, store.getId());

        if (category == null) {
            throw new ResourceNotFoundException("Category with id [" + categoryId + "] not found");
        }

        if (category.getMerchantStore().getId().longValue() != store.getId().longValue()) {
            throw new UnauthorizedException("Merchant [" + store.getCode() + "] not authorized");
        }

        try {
            List<Manufacturer> manufacturers = manufacturerService.listByProductsInCategory(store, category, language);

            List<ReadableManufacturer> manufacturersList = manufacturers.stream()
                    .sorted((object1, object2) -> object1.getCode().compareTo(object2.getCode()))
                    .map(manuf -> readableManufacturerConverter.convert(manuf, store, language))
                    .collect(Collectors.toList());

            return manufacturersList;
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

    }

    @Override
    public void saveOrUpdateManufacturer(PersistableManufacturer manufacturer, MerchantStore store,
                                         Language language) throws Exception {

        PersistableManufacturerPopulator populator = new PersistableManufacturerPopulator();
        populator.setLanguageService(languageService);


        Manufacturer manuf = new Manufacturer();

        if (manufacturer.getId() != null && manufacturer.getId() > 0) {
            manuf = manufacturerService.getById(manufacturer.getId());
            if (manuf == null) {
                throw new ResourceNotFoundException("Manufacturer with id [" + manufacturer.getId() + "] not found");
            }

            if (manuf.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                throw new ResourceNotFoundException("Manufacturer with id [" + manufacturer.getId() + "] not found for store [" + store.getId() + "]");
            }
        }

        populator.populate(manufacturer, manuf, store, language);

        manufacturerService.saveOrUpdate(manuf);

        manufacturer.setId(manuf.getId());

    }

    @Override
    public void deleteManufacturer(Manufacturer manufacturer, MerchantStore store, Language language)
            throws Exception {
        manufacturerService.delete(manufacturer);

    }

    @Override
    public ReadableManufacturer getManufacturer(Long id, MerchantStore store, Language language)
            throws Exception {
        Manufacturer manufacturer = manufacturerService.getById(id);


        if (manufacturer == null) {
            throw new ResourceNotFoundException("Manufacturer [" + id + "] not found");
        }

        if (!manufacturer.getMerchantStore().getId().equals(store.getId())) {
            throw new ResourceNotFoundException("Manufacturer [" + id + "] not found for store [" + store.getId() + "]");
        }

        ReadableManufacturer readableManufacturer = new ReadableManufacturer();

        ReadableManufacturerPopulator populator = new ReadableManufacturerPopulator();
        readableManufacturer = populator.populate(manufacturer, readableManufacturer, store, language);


        return readableManufacturer;
    }

    @Override
    public ReadableManufacturerList getAllManufacturers(MerchantStore store, Language language, ListCriteria criteria, int page, int count) {

        ReadableManufacturerList readableList = new ReadableManufacturerList();
        try {

            List<Manufacturer> manufacturers = null;
            if (page == 0 && count == 0) {
                //need total count
                int total = manufacturerService.count(store);

                if (language != null) {
                    manufacturers = manufacturerService.listByStore(store, language);
                } else {
                    manufacturers = manufacturerService.listByStore(store);
                }
                readableList.setRecordsTotal(total);
                readableList.setNumber(manufacturers.size());
            } else {

                Page<Manufacturer> m = null;
                if (language != null) {
                    m = manufacturerService.listByStore(store, language, criteria.getName(), page, count);
                } else {
                    m = manufacturerService.listByStore(store, criteria.getName(), page, count);
                }
                manufacturers = m.getContent();
                readableList.setTotalPages(m.getTotalPages());
                readableList.setRecordsTotal(m.getTotalElements());
                readableList.setNumber(m.getNumber());
            }


            ReadableManufacturerPopulator populator = new ReadableManufacturerPopulator();
            List<ReadableManufacturer> returnList = new ArrayList<>();

            for (Manufacturer m : manufacturers) {
                ReadableManufacturer readableManufacturer = new ReadableManufacturer();
                populator.populate(m, readableManufacturer, store, language);
                returnList.add(readableManufacturer);
            }

            readableList.setManufacturers(returnList);
            return readableList;

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while get manufacturers", e);
        }
    }


    @Override
    public boolean manufacturerExist(MerchantStore store, String manufacturerCode) {
        Assert.notNull(store, "Store must not be null");
        Assert.notNull(manufacturerCode, "Manufacturer code must not be null");
        boolean exists = false;
        Manufacturer manufacturer = manufacturerService.getByCode(store, manufacturerCode);
        if (manufacturer != null) {
            exists = true;
        }
        return exists;
    }

    @Override
    public ReadableManufacturerList listByStore(MerchantStore store, Language language, ListCriteria criteria, int page,
                                                int count) {

        ReadableManufacturerList readableList = new ReadableManufacturerList();

        try {

            List<Manufacturer> manufacturers = null;

            Page<Manufacturer> m = null;
            if (language != null) {
                m = manufacturerService.listByStore(store, language, criteria.getName(), page, count);
            } else {
                m = manufacturerService.listByStore(store, criteria.getName(), page, count);
            }

            manufacturers = m.getContent();
            readableList.setTotalPages(m.getTotalPages());
            readableList.setRecordsTotal(m.getTotalElements());
            readableList.setNumber(m.getContent().size());


            ReadableManufacturerPopulator populator = new ReadableManufacturerPopulator();
            List<ReadableManufacturer> returnList = new ArrayList<>();

            for (Manufacturer mf : manufacturers) {
                ReadableManufacturer readableManufacturer = new ReadableManufacturer();
                populator.populate(mf, readableManufacturer, store, language);
                returnList.add(readableManufacturer);
            }

            readableList.setManufacturers(returnList);
            return readableList;

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while get manufacturers", e);
        }

    }


}
