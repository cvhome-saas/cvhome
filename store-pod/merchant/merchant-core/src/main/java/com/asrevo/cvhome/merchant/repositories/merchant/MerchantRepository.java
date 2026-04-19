package com.asrevo.cvhome.merchant.repositories.merchant;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;

public interface MerchantRepository extends JpaRepository<MerchantStore, StoreMerchantId> {

    @Query("""
            select m from MerchantStore m
            left join fetch m.languages mls where m.id = ?1""")
    MerchantStore findByMerchantStoreId(StoreMerchantId storeMerchantId);

    @Query(value = """
                    select m from MerchantStore m left join fetch m.storeDomains d
                    where (d.domain=?1) or (CONCAT(d.domain, '.',?2 )=?1)
            """)
    Optional<MerchantStore> findByDomain(String domain, String podDomain);

}
