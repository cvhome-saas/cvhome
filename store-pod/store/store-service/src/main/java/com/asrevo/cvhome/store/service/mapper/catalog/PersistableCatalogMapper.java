package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.core.model.catalog.catalog.PersistableCatalog;
import org.springframework.stereotype.Component;

@Component
public class PersistableCatalogMapper implements Mapper<PersistableCatalog, Catalog> {

    @Override
    public Catalog convert(PersistableCatalog source, MerchantStore store, Language language) {
        Catalog c = new Catalog();
        return this.merge(source, c, store, language);
    }

    @Override
    public Catalog merge(PersistableCatalog source, Catalog destination, MerchantStore store, Language language) {


        destination.setCode(source.getCode());
        destination.setDefaultCatalog(source.isDefaultCatalog());
        destination.setId(source.getId());
        destination.setMerchantStore(store);
        destination.setVisible(source.isVisible());

        return destination;
    }

}
