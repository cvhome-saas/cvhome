package com.asrevo.cvhome.store.service.facade.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.catalog.PersistableCatalog;
import com.asrevo.cvhome.store.core.model.catalog.catalog.PersistableCatalogCategoryEntry;
import com.asrevo.cvhome.store.core.model.catalog.catalog.ReadableCatalog;
import com.asrevo.cvhome.store.core.model.catalog.catalog.ReadableCatalogCategoryEntry;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import java.util.Optional;

public interface CatalogFacade {

    ReadableCatalog saveCatalog(PersistableCatalog catalog, MerchantStore store, Language language);

    void updateCatalog(
            Long catalogId, PersistableCatalog catalog, MerchantStore store, Language language);

    void deleteCatalog(Long catalogId, MerchantStore store, Language language);

    boolean uniqueCatalog(String code, MerchantStore store);

    ReadableCatalog getCatalog(String code, MerchantStore store, Language language);

    Catalog getCatalog(String code, MerchantStore store);

    ReadableCatalog getCatalog(Long id, MerchantStore store, Language language);

    ReadableEntityList<ReadableCatalog> getListCatalogs(
            Optional<String> code, MerchantStore store, Language language, int page, int count);

    ReadableEntityList<ReadableCatalogCategoryEntry> listCatalogEntry(
            Optional<String> product,
            Long catalogId,
            MerchantStore store,
            Language language,
            int page,
            int count);

    ReadableCatalogCategoryEntry getCatalogEntry(Long id, MerchantStore store, Language language);

    ReadableCatalogCategoryEntry addCatalogEntry(
            PersistableCatalogCategoryEntry entry, MerchantStore store, Language language);

    void removeCatalogEntry(
            Long catalogId, Long catalogEntryId, MerchantStore store, Language language);
}
