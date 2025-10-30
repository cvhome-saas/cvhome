package com.asrevo.cvhome.merchant.repositories.merchant;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MerchantRepository extends JpaRepository<MerchantStore, StoreMerchantId> {

	@Query("""
			select m from MerchantStore m
			left join fetch m.languages mls where m.id = ?1""")
	MerchantStore findByMerchantStoreId(StoreMerchantId storeMerchantId);

}
