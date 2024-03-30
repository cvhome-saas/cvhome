package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup.PersistableProductVariantGroup;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup.ReadableProductVariantGroup;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import org.springframework.web.multipart.MultipartFile;

public interface ProductVariantGroupFacade {

    ReadableProductVariantGroup get(Long instanceGroupId, MerchantStore store, Language language);

    Long create(PersistableProductVariantGroup productVariantGroup, MerchantStore store, Language language);

    void update(Long productVariantGroup, PersistableProductVariantGroup instance, MerchantStore store, Language language);

    void delete(Long productVariant, Long productId, MerchantStore store);

    ReadableEntityList<ReadableProductVariantGroup> list(Long productId, MerchantStore store, Language language, int page, int count);

    void addImage(MultipartFile image, Long instanceGroupId,
                  MerchantStore store, Language language);

    void removeImage(Long imageId, Long instanceGroupId, MerchantStore store);

}
