package com.asrevo.cvhome.catalog.service.facade.manufacturer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturerList;
import com.asrevo.cvhome.catalog.service.populator.manufacturer.PersistableManufacturerPopulator;
import com.asrevo.cvhome.catalog.service.populator.manufacturer.ReadableManufacturerPopulator;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;

@Service("manufacturerFacade")
public class ManufacturerFacadeImpl implements ManufacturerFacade {

    private final Mapper<Manufacturer, ReadableManufacturer> readableManufacturerConverter;

    private final ManufacturerService manufacturerService;

    private final CategoryService categoryService;

    public ManufacturerFacadeImpl(Mapper<Manufacturer, ReadableManufacturer> readableManufacturerConverter,
                                  ManufacturerService manufacturerService, CategoryService categoryService) {
        this.readableManufacturerConverter = readableManufacturerConverter;
        this.manufacturerService = manufacturerService;
        this.categoryService = categoryService;
    }

    @Override
    public List<ReadableManufacturer> getByProductInCategory(StoreMerchantId store, LanguageCode language,
                                                             Long categoryId) {
        Category category = categoryService.getById(categoryId, store);

        if (category == null) {
            throw new ResourceNotFoundException("Category with id [" + categoryId + "] not found");
        }

        if (!Objects.equals(category.getStoreMerchantId(), store)) {
            throw new UnauthorizedException("Merchant [" + store + "] not authorized");
        }

        List<Manufacturer> manufacturers = manufacturerService.listByProductsInCategory(store, category, language);

        return manufacturers.stream()
                .sorted(Comparator.comparing(Manufacturer::getCode))
                .map(manuf -> readableManufacturerConverter.convert(manuf, store, language))
                .toList();
    }

    @Override
    public void saveOrUpdateManufacturer(PersistableManufacturer manufacturer, StoreMerchantId store,
                                         LanguageCode language) throws Exception {

        PersistableManufacturerPopulator populator = new PersistableManufacturerPopulator();

        Manufacturer manuf = new Manufacturer();

        if (manufacturer.getId() != null && manufacturer.getId() > 0) {
            manuf = manufacturerService.getById(manufacturer.getId());
            if (manuf == null) {
                throw new ResourceNotFoundException("Manufacturer with id [" + manufacturer.getId() + "] not found");
            }

            if (!Objects.equals(manuf.getStoreMerchantId(), store)) {
                throw new ResourceNotFoundException(
                        "Manufacturer with id [" + manufacturer.getId() + "] not found for store [" + store + "]");
            }
        }

        populator.populate(manufacturer, manuf, store, language);

        manufacturerService.saveOrUpdate(manuf);

        manufacturer.setId(manuf.getId());
    }

    @Override
    public void deleteManufacturer(Manufacturer manufacturer) throws Exception {
        manufacturerService.delete(manufacturer);
    }

    @Override
    public ReadableManufacturer getManufacturer(Long id, StoreMerchantId store, LanguageCode language) {
        Manufacturer manufacturer = manufacturerService.getById(id);

        if (manufacturer == null) {
            throw new ResourceNotFoundException("Manufacturer [" + id + "] not found");
        }

        if (!manufacturer.getStoreMerchantId().equals(store)) {
            throw new ResourceNotFoundException("Manufacturer [" + id + "] not found for store [" + store + "]");
        }

        ReadableManufacturer readableManufacturer = new ReadableManufacturer();

        ReadableManufacturerPopulator populator = new ReadableManufacturerPopulator();
        readableManufacturer = populator.populate(manufacturer, readableManufacturer, store, language);

        return readableManufacturer;
    }

    @Override
    public boolean manufacturerExist(StoreMerchantId store, String manufacturerCode) {
        boolean exists = false;
        Manufacturer manufacturer = manufacturerService.getByCode(store, manufacturerCode);
        if (manufacturer != null) {
            exists = true;
        }
        return exists;
    }

    @Override
    public ReadableManufacturerList listByStore(StoreMerchantId store, LanguageCode language, ListCriteria criteria,
                                                Pageable pageable) {

        ReadableManufacturerList readableList = new ReadableManufacturerList();

        try {

            List<Manufacturer> manufacturers;

            Page<Manufacturer> m = manufacturerService.listByStore(store, language, criteria.getName(), pageable);

            manufacturers = m.getContent();
            readableList.setTotalPages(m.getTotalPages());
            readableList.setTotalElements(m.getTotalElements());
            readableList.setSize(m.getNumberOfElements());
            readableList.setPageNumber(m.getNumber());

            ReadableManufacturerPopulator populator = new ReadableManufacturerPopulator();
            List<ReadableManufacturer> returnList = new ArrayList<>();

            for (Manufacturer mf : manufacturers) {
                ReadableManufacturer readableManufacturer = new ReadableManufacturer();
                populator.populate(mf, readableManufacturer, store, language);
                returnList.add(readableManufacturer);
            }

            readableList.setContent(returnList);
            return readableList;

        } catch (Exception e) {
            throw new ServiceRuntimeException("Error while get manufacturers", e);
        }
    }

}
