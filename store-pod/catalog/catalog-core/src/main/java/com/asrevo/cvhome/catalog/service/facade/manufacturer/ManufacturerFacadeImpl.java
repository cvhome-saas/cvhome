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
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ForeignStoreProductAccessException;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotFoundException;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturerList;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableManufacturerMapper;
import com.asrevo.cvhome.catalog.service.populator.manufacturer.PersistableManufacturerPopulator;
import com.asrevo.cvhome.catalog.service.populator.manufacturer.ReadableManufacturerPopulator;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;

@Service("manufacturerFacade")
public class ManufacturerFacadeImpl implements ManufacturerFacade {

    /**
     * Typed as the concrete mapper, not as {@code Mapper<Manufacturer, ReadableManufacturer>}: the SPI declares the
     * shared {@code ConversionException} base, so a field of the interface type would hand this facade a failure it
     * cannot name. The implementation narrows it, and that narrowing is only visible through the concrete type.
     */
    private final ReadableManufacturerMapper readableManufacturerConverter;

    private final ManufacturerService manufacturerService;

    private final CategoryService categoryService;

    public ManufacturerFacadeImpl(ReadableManufacturerMapper readableManufacturerConverter,
                                  ManufacturerService manufacturerService, CategoryService categoryService) {
        this.readableManufacturerConverter = readableManufacturerConverter;
        this.manufacturerService = manufacturerService;
        this.categoryService = categoryService;
    }

    @Override
    public List<ReadableManufacturer> getByProductInCategory(StoreMerchantId store, LanguageCode language,
                                                             Long categoryId)
            throws CategoryNotFoundException, ForeignStoreProductAccessException, ManufacturerNotConvertibleException {
        Category category = categoryService.getById(categoryId, store);

        if (category == null) {
            throw CategoryNotFoundException.of(categoryId, store);
        }

        if (!Objects.equals(category.getStoreMerchantId(), store)) {
            // Was a 401 from UnauthorizedException. The caller is authenticated and passed the permission check; the
            // category simply is not theirs, which is a 403.
            throw ForeignStoreProductAccessException.of(categoryId, store);
        }

        List<Manufacturer> manufacturers = manufacturerService.listByProductsInCategory(store, category, language);
        manufacturers.sort(Comparator.comparing(Manufacturer::getCode));

        // A plain loop rather than stream().map(...): the mapper declares a checked failure now.
        List<ReadableManufacturer> readable = new ArrayList<>();
        for (Manufacturer manuf : manufacturers) {
            readable.add(readableManufacturerConverter.convert(manuf, store, language));
        }
        return readable;
    }

    @Override
    public void saveOrUpdateManufacturer(PersistableManufacturer manufacturer, StoreMerchantId store,
                                         LanguageCode language)
            throws ManufacturerNotFoundException, ManufacturerNotConvertibleException, ServiceException {

        PersistableManufacturerPopulator populator = new PersistableManufacturerPopulator();

        Manufacturer manuf = new Manufacturer();

        if (manufacturer.getId() != null && manufacturer.getId() > 0) {
            manuf = manufacturerService.getById(manufacturer.getId());
            if (manuf == null || !Objects.equals(manuf.getStoreMerchantId(), store)) {
                throw ManufacturerNotFoundException.of(manufacturer.getId(), store);
            }
        }

        populator.populate(manufacturer, manuf, store, language);

        manufacturerService.saveOrUpdate(manuf);

        manufacturer.setId(manuf.getId());
    }

    @Override
    public void deleteManufacturer(Manufacturer manufacturer) throws ServiceException {
        manufacturerService.delete(manufacturer);
    }

    @Override
    public ReadableManufacturer getManufacturer(Long id, StoreMerchantId store, LanguageCode language)
            throws ManufacturerNotFoundException, ManufacturerNotConvertibleException {
        Manufacturer manufacturer = manufacturerService.getById(id);

        if (manufacturer == null || !manufacturer.getStoreMerchantId().equals(store)) {
            throw ManufacturerNotFoundException.of(id, store);
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
                                                Pageable pageable) throws ManufacturerNotConvertibleException {

        ReadableManufacturerList readableList = new ReadableManufacturerList();

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
    }

}
