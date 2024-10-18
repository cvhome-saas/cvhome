package com.asrevo.cvhome.store.core.repositories.catalog.catalog;

import com.asrevo.cvhome.store.core.entity.catalog.catalog.CatalogCategoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogEntryRepository extends JpaRepository<CatalogCategoryEntry, Long> {}
