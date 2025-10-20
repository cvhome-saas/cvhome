package com.asrevo.cvhome.catalog.repositories.catalog;

import com.asrevo.cvhome.catalog.entity.catalog.Catalog;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableCatalogRepository extends PagingAndSortingRepository<Catalog, Long> {

}
