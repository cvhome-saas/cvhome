package com.asrevo.cvhome.catalog.repositories.product.manufacturer;

import com.asrevo.cvhome.catalog.entity.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {

	@Query("""
			select m from Manufacturer m
			left join fetch m.descriptions md
			where m.storeMerchantId=?1 and md.languageCode=?2""")
	List<Manufacturer> findByStoreAndLanguage(StoreMerchantId storeMerchantId, LanguageCode languageId);

	@Query("""
			select m from Manufacturer m
			left join fetch  m.descriptions md
			where m.id=?1""")
	Manufacturer findOne(Long id);

	@Query("""
			select m from Manufacturer m
			left join fetch m.descriptions md
			where m.storeMerchantId=?1""")
	List<Manufacturer> findByStore(StoreMerchantId storeMerchantId);

	@Query("""
			select m from Manufacturer m
			left join m.descriptions md
			where m.code=?1 and m.storeMerchantId=?2""")
	Manufacturer findByCodeAndMerchandStore(String code, StoreMerchantId storeMerchantId);

	@Query("""
			select count(distinct m) from Manufacturer as m where m.storeMerchantId=?1""")
	int count(StoreMerchantId storeMerchantId);

	@Query(value = """
			select distinct manufacturer from Product as p
			join p.manufacturer manufacturer
			left join manufacturer.descriptions pmd
			join p.categories pc
			where manufacturer.storeMerchantId = ?1
			and pc.id IN (
			select c.id from Category c
			where c.lineage like %?2% and pmd.languageCode = ?3
			)""")
	List<Manufacturer> findByProductInCategoryId(StoreMerchantId storeMerchantId, String lineage,
			LanguageCode languageId);

}
