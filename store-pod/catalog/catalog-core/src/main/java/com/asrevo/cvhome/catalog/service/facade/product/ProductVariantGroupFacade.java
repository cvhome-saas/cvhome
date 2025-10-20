package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.model.product.product.variantGroup.PersistableProductVariantGroup;
import com.asrevo.cvhome.catalog.model.product.product.variantGroup.ReadableProductVariantGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.data.domain.Pageable;

public interface ProductVariantGroupFacade {

	ReadableProductVariantGroup get(Long instanceGroupId, StoreMerchantId store, LanguageCode language);

	Long create(PersistableProductVariantGroup productVariantGroup, StoreMerchantId store, LanguageCode language);

	void update(Long productVariantGroup, PersistableProductVariantGroup instance, StoreMerchantId store,
			LanguageCode language);

	void delete(Long productVariant, Long productId, StoreMerchantId store);

	ReadableEntityList<ReadableProductVariantGroup> list(Long productId, StoreMerchantId store, LanguageCode language,
			Pageable pageable);
	/*
	 *
	 * @TODO not needed for now void addImage(MultipartFile image, Long instanceGroupId,
	 * StoreMerchantId store, LanguageCode language);
	 *
	 * void removeImage(Long imageId, Long instanceGroupId, StoreMerchantId store);
	 */

}
