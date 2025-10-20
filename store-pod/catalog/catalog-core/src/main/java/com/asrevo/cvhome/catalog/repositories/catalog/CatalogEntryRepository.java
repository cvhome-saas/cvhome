package com.asrevo.cvhome.catalog.repositories.catalog;

import com.asrevo.cvhome.catalog.entity.catalog.CatalogCategoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogEntryRepository extends JpaRepository<CatalogCategoryEntry, Long> {

}
