package com.asrevo.cvhome.store.service.facade.catalog;

import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.catalog.catalog.CatalogCategoryEntry;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.catalog.PersistableCatalog;
import com.asrevo.cvhome.store.core.model.catalog.catalog.PersistableCatalogCategoryEntry;
import com.asrevo.cvhome.store.core.model.catalog.catalog.ReadableCatalog;
import com.asrevo.cvhome.store.core.model.catalog.catalog.ReadableCatalogCategoryEntry;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.services.catalog.catalog.CatalogEntryService;
import com.asrevo.cvhome.store.core.services.catalog.catalog.CatalogService;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.service.mapper.catalog.PersistableCatalogMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableCatalogCategoryEntryMapper;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableCatalogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;

@Service("catalogFacade")
public class CatalogFacadeImpl implements CatalogFacade {


    private final CatalogService catalogService;

    private final CatalogEntryService catalogEntryService;

    private final PersistableCatalogMapper persistableCatalogMapper;

    private final ReadableCatalogMapper readableCatalogMapper;

    private final Mapper<PersistableCatalogCategoryEntry, CatalogCategoryEntry> persistableCatalogEntryMapper;

    private final ReadableCatalogCategoryEntryMapper readableCatalogEntryMapper;

    public CatalogFacadeImpl(CatalogService catalogService, CatalogEntryService catalogEntryService, PersistableCatalogMapper persistableCatalogMapper, ReadableCatalogMapper readableCatalogMapper, Mapper<PersistableCatalogCategoryEntry, CatalogCategoryEntry> persistableCatalogEntryMapper, ReadableCatalogCategoryEntryMapper readableCatalogEntryMapper) {
        this.catalogService = catalogService;
        this.catalogEntryService = catalogEntryService;
        this.persistableCatalogMapper = persistableCatalogMapper;
        this.readableCatalogMapper = readableCatalogMapper;
        this.persistableCatalogEntryMapper = persistableCatalogEntryMapper;
        this.readableCatalogEntryMapper = readableCatalogEntryMapper;
    }


    @Override
    public ReadableCatalog saveCatalog(PersistableCatalog catalog, MerchantStore store, Language language) {
        Assert.notNull(catalog, "Catalog cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");
        Catalog catalogToSave = persistableCatalogMapper.convert(catalog, store, language);

        boolean existByCode = uniqueCatalog(catalog.getCode(), store);
        if (existByCode) {
            throw new OperationNotAllowedException("Catalog [" + catalog.getCode() + "] already exists");
        }
        catalogService.saveOrUpdate(catalogToSave, store);
        Catalog savedCatalog = catalogService.getByCode(catalogToSave.getCode(), store).get();
        return readableCatalogMapper.convert(savedCatalog, store, language);
    }

    @Override
    public void deleteCatalog(Long catalogId, MerchantStore store, Language language) {
        Assert.notNull(catalogId, "Catalog id cannot be null");
        Assert.isTrue(catalogId > 0, "Catalog id cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");

        Catalog c = catalogService.getById(catalogId);

        if (Objects.isNull(c)) {
            throw new ResourceNotFoundException("Catalog with id [" + catalogId + "] not found");
        }

        if (Objects.nonNull(c.getMerchantStore()) && !c.getMerchantStore().getCode().equals(store.getCode())) {
            throw new ResourceNotFoundException("Catalog with id [" + catalogId + "] not found for merchant [" + store.getCode() + "]");
        }

        try {
            catalogService.delete(c);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while deleting catalog id [" + catalogId + "]", e);
        }

    }

    @Override
    public ReadableCatalog getCatalog(String code, MerchantStore store, Language language) {
        Assert.notNull(code, "Catalog code cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");

        Catalog catalog = catalogService.getByCode(code, store)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog with code [" + code + "] not found"));
        return readableCatalogMapper.convert(catalog, store, language);
    }

    @Override
    public void updateCatalog(Long catalogId, PersistableCatalog catalog, MerchantStore store, Language language) {
        Assert.notNull(catalogId, "Catalog id cannot be null");
        Assert.isTrue(catalogId > 0, "Catalog id cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");
        Assert.notNull(language, "Language cannot be null");

        Catalog c = Optional.ofNullable(catalogService.getById(catalogId))
                .orElseThrow(() -> new ResourceNotFoundException("Catalog with id [" + catalogId + "] not found"));

        if (Objects.nonNull(c.getMerchantStore()) && !c.getMerchantStore().getCode().equals(store.getCode())) {
            throw new ResourceNotFoundException("Catalog with id [" + catalogId + "] not found for merchant [" + store.getCode() + "]");
        }

        c.setDefaultCatalog(catalog.isDefaultCatalog());
        c.setVisible(catalog.isVisible());

        catalogService.saveOrUpdate(c, store);
    }

    @Override
    public ReadableCatalog getCatalog(Long id, MerchantStore store, Language language) {
        Assert.notNull(id, "Catalog id cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");

        Catalog catalog = catalogService.getById(id, store)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog with id [" + id + "] not found"));
        return readableCatalogMapper.convert(catalog, store, language);
    }

    @Override
    public Catalog getCatalog(String code, MerchantStore store) {
        Assert.notNull(code, "Catalog code cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");

        return catalogService.getByCode(code, store).get();
    }

    @Override
    public ReadableEntityList<ReadableCatalog> getListCatalogs(Optional<String> code, MerchantStore store, Language language, int page, int count) {
        Assert.notNull(store, "MerchantStore cannot be null");

        String catalogCode = code.orElse(null);
        Page<Catalog> catalogs = catalogService.getCatalogs(store, language, catalogCode, page, count);
        if (catalogs.isEmpty()) {
            return new ReadableEntityList<>();
        }

        List<ReadableCatalog> readableList = catalogs.getContent().stream()
                .map(cat -> readableCatalogMapper.convert(cat, store, language))
                .collect(Collectors.toList());
        return createReadableList(catalogs, readableList);
    }

    @Override
    public ReadableEntityList<ReadableCatalogCategoryEntry> listCatalogEntry(Optional<String> product, Long id, MerchantStore store, Language language, int page, int count) {
        Assert.notNull(store, "MerchantStore cannot be null");

        String productCode = product.orElse(null);
        Catalog catalog = catalogService.getById(id, store)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog with id [" + id + "] not found for store [" + store.getCode() + "]"));

        Page<CatalogCategoryEntry> entries = catalogEntryService.list(catalog, store, language, productCode, page, count);

        if (entries.isEmpty()) {
            return new ReadableEntityList<>();
        }

        List<ReadableCatalogCategoryEntry> readableList = entries.getContent().stream()
                .map(cat -> readableCatalogEntryMapper.convert(cat, store, language))
                .collect(Collectors.toList());
        return createReadableList(entries, readableList);
    }

    @Override
    public ReadableCatalogCategoryEntry getCatalogEntry(Long id, MerchantStore store, Language language) {
        CatalogCategoryEntry entry = catalogEntryService.getById(id);
        if (Objects.isNull(entry)) {
            throw new ResourceNotFoundException("catalog entry [" + id + "] not found");
        }

        if (entry.getCatalog().getMerchantStore().getId().intValue() != store.getId().intValue()) {
            throw new ResourceNotFoundException("catalog entry [" + id + "] not found");
        }
        return readableCatalogEntryMapper.convert(entry, store, language);
    }

    @Override
    public ReadableCatalogCategoryEntry addCatalogEntry(PersistableCatalogCategoryEntry entry, MerchantStore store, Language language) {

        Assert.notNull(entry, "PersistableCatalogEntry cannot be null");
        Assert.notNull(entry.getCatalog(), "CatalogEntry.catalog cannot be null");
        Assert.notNull(store, "MerchantStore cannot be null");

        Catalog catalog = catalogService.getByCode(entry.getCatalog(), store)
                .orElseThrow(() -> new ResourceNotFoundException("catalog [" + entry.getCatalog() + "] not found"));

        CatalogCategoryEntry catalogEntryModel = persistableCatalogEntryMapper.convert(entry, store, language);
        catalogEntryService.add(catalogEntryModel, catalog);
        return readableCatalogEntryMapper.convert(catalogEntryModel, store, language);

    }

    @Override
    public void removeCatalogEntry(Long catalogId, Long catalogEntryId, MerchantStore store, Language language) {
        CatalogCategoryEntry entry = catalogEntryService.getById(catalogEntryId);
        if (Objects.isNull(entry)) {
            throw new ResourceNotFoundException("catalog entry [" + catalogEntryId + "] not found");
        }

        if (entry.getCatalog().getId().longValue() != catalogId.longValue()) {
            throw new ResourceNotFoundException("catalog entry [" + catalogEntryId + "] not found");
        }

        if (entry.getCatalog().getMerchantStore().getId().intValue() != store.getId().intValue()) {
            throw new ResourceNotFoundException("catalog entry [" + catalogEntryId + "] not found");
        }

        try {
            catalogEntryService.delete(entry);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Exception while deleting catalogEntry", e);
        }

    }

    @Override
    public boolean uniqueCatalog(String code, MerchantStore store) {
        return catalogService.existByCode(code, store);
    }

}
