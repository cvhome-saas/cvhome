package com.asrevo.cvhome.catalog.service.facade.product.group;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.model.product.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupListV2;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductGroupFacade {

    ReadableProductGroup getByCode(StoreMerchantId store, String code, LanguageCode language);

    ReadableProductGroupListV2 list(StoreMerchantId store, LanguageCode language, Pageable pageable);

    void delete(StoreMerchantId store, String code);

    PersistableProductGroup saveProductGroup(StoreMerchantId store, PersistableProductGroup productGroup);

    void addProductToGroup(StoreMerchantId store, String groupCode, Long productId);

    void removeProductFromGroup(StoreMerchantId store, String groupCode, Long productId);

    boolean existByCode(StoreMerchantId store, String code);

    ReadableProductGroup getByCodeAndParent(StoreMerchantId store, Long productId, String code, LanguageCode language);

    void addProductToGroupForParent(StoreMerchantId store, Long parentProductId, String code, Long productId);

    void removeProductFromGroupForParent(StoreMerchantId store, Long parentProductId, String code, Long productId);

}
