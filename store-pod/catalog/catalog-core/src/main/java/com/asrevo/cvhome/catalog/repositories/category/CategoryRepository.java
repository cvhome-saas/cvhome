package com.asrevo.cvhome.catalog.repositories.category;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {

	@Query("""
			select c from Category c left join fetch c.descriptions cd
			where cd.seUrl=?2 and cd.languageCode=?3 and c.storeMerchantId = ?1""")
	Category findByFriendlyUrl(StoreMerchantId storeMerchantId, String friendlyUrl, LanguageCode languageId);

	@Query("""
			select c from Category c left join fetch c.descriptions cd
			 where c.code=?2 and c.storeMerchantId = ?1""")
	Category findByCode(StoreMerchantId storeMerchantId, String code);

	@Query("""
			select c from Category c left join fetch c.descriptions cd
			 left join fetch c.categories where c.id = ?1 and
			 c.storeMerchantId=?2""")
	Category findByIdAndStore(Long categoryId, StoreMerchantId storeMerchantId);

	@Query("""
			select c from Category c left join fetch c.parent cp left join fetch c.descriptions cd
			 left join fetch
			 c.categories where c.id = ?1""")
	@Override
	Optional<Category> findById(Long categoryId);

	@Query("""
			select distinct c from Category c left join fetch c.descriptions cd
			 where c.storeMerchantId=?1 and c.lineage like
			 %?2% order by c.lineage, c.sortOrder asc""")
	List<Category> findByLineage(StoreMerchantId storeMerchantId, String linenage);

}
