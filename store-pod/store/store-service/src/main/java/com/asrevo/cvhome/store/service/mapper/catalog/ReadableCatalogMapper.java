package com.asrevo.cvhome.store.service.mapper.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.catalog.Catalog;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.catalog.ReadableCatalog;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.store.ReadableMerchantStore;
import com.asrevo.cvhome.store.service.facade.store.StoreFacade;
import com.asrevo.cvhome.store.service.mapper.Mapper;
import com.asrevo.cvhome.store.utils.DateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ReadableCatalogMapper implements Mapper<Catalog, ReadableCatalog> {

    private final StoreFacade storeFacade;


    private final ReadableCategoryMapper readableCategoryMapper;

    public ReadableCatalogMapper(StoreFacade storeFacade, ReadableCategoryMapper readableCategoryMapper) {
        this.storeFacade = storeFacade;
        this.readableCategoryMapper = readableCategoryMapper;
    }

    @Override
    public ReadableCatalog convert(Catalog source, MerchantStore store, Language language) {
        ReadableCatalog destination = new ReadableCatalog();
        return merge(source, destination, store, language);
    }

    @Override
    public ReadableCatalog merge(Catalog source, ReadableCatalog destination, MerchantStore store,
                                 Language language) {
        if (destination == null) {
            destination = new ReadableCatalog();
        }

        if (isPositive(source.getId())) {
            destination.setId(source.getId());
        }

        destination.setCode(source.getCode());
        destination.setDefaultCatalog(source.isDefaultCatalog());
        destination.setVisible(source.isVisible());

        Optional<ReadableMerchantStore> readableStore = Optional.ofNullable(source.getMerchantStore())
                .map(MerchantStore::getCode)
                .map(code -> storeFacade.getByCode(code, language));
        readableStore.ifPresent(destination::setStore);

        destination.setDefaultCatalog(source.isDefaultCatalog());

        Optional<String> formattedCreationDate = Optional.ofNullable(source.getAuditSection())
                .map(AuditSection::getDateCreated)
                .map(DateUtil::formatDate);
        formattedCreationDate.ifPresent(destination::setCreationDate);

        if (CollectionUtils.isNotEmpty(source.getEntry())) {

            //hierarchy temp object
            Map<Long, ReadableCategory> hierarchy = new HashMap<Long, ReadableCategory>();

            source.getEntry().forEach(entry -> {
                processCategory(entry.getCategory(), store, language, hierarchy, new HashMap<>());
            });

            destination.setCategory(new ArrayList<>(hierarchy.values()));
        }

        return destination;

    }

    private boolean isPositive(Long id) {
        return Objects.nonNull(id) && id > 0;
    }

    /**
     * B
     * 1
     * D
     * 2
     * C
     * 1
     * 4
     * A
     *
     * @param parent
     * @param c
     * @param store
     * @param language
     * @param hierarchy
     */

    //TODO it needs to cover by unit tests
    private void processCategory(Category c, MerchantStore store, Language language, Map<Long, ReadableCategory> hierarchy, Map<Long, ReadableCategory> processed) {

        //build category hierarchy

        ReadableCategory rc = null;
        ReadableCategory rp = null;

        if (CollectionUtils.isNotEmpty(c.getCategories())) {
            c.getCategories().forEach(element -> processCategory(element, store, language, hierarchy, processed));
        }

        Category parent = c.getParent();
        if (Objects.nonNull(parent)) {
            rp = hierarchy.computeIfAbsent(parent.getId(), i -> toReadableCategory(c.getParent(), store, language, processed));
        }

        rc = toReadableCategory(c, store, language, processed);
        if (Objects.nonNull(rp)) {
            rp.getChildren().add(rc);
        } else {
            hierarchy.put(c.getId(), rc);
        }

    }

    private ReadableCategory toReadableCategory(Category c, MerchantStore store, Language lang, Map<Long, ReadableCategory> processed) {
        Long id = c.getId();
        return processed.computeIfAbsent(id, it -> readableCategoryMapper.convert(c, store, lang));
    }

}
