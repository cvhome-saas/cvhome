package com.asrevo.cvhome.catalog.repositories.catalog;

import com.asrevo.cvhome.catalog.entity.catalog.CatalogCategoryEntry;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableCatalogEntryRepository
        extends PagingAndSortingRepository<CatalogCategoryEntry, Long> {}
