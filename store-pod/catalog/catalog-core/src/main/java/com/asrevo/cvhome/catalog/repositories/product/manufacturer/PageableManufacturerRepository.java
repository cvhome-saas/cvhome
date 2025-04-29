package com.asrevo.cvhome.catalog.repositories.product.manufacturer;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PageableManufacturerRepository
        extends PagingAndSortingRepository<Manufacturer, Long> {

    @Query(
            """
                    select m from Manufacturer m
                    left join m.descriptions md
                    where m.storeMerchantId=?1
                    and md.languageCode=?2 and (?3 is null or md.name like %?3%)""")
    Page<Manufacturer> findByStore(
            StoreMerchantId storeMerchantId,
            LanguageCode languageId,
            String name,
            Pageable pageable);

    @Query(
            """
                    select m from Manufacturer m
                    left join m.descriptions md
                    where m.storeMerchantId=?1
                    and (?2 is null or md.name like %?2%)""")
    Page<Manufacturer> findByStore(StoreMerchantId storeMerchantId, String name, Pageable pageable);
}
