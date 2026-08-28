package com.asrevo.cvhome.merchant.service.facade.merchant;

import java.util.List;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.errors.DefaultStoreNotRemovableException;
import com.asrevo.cvhome.merchant.errors.DuplicateMerchantStoreException;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

/**
 * Layer between shop controllers, services and API with sm-core
 *
 * @author carlsamson
 */
public interface StoreFacade {

    MerchantStore get(StoreMerchantId storeMerchantId);

    List<LanguageCode> supportedLanguages(StoreMerchantId storeMerchantId);

    ReadableMerchantStore getReadableMerchantStoreId(StoreMerchantId storeMerchantId);

    ReadableMerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId, LanguageCode lang)
            throws MerchantStoreNotFoundException;

    /**
     * Creates a brand new MerchantStore
     */
    void create(PersistableMerchantStore store) throws DuplicateMerchantStoreException;

    /**
     * Deletes a MerchantStore based on store code
     */
    void delete(StoreMerchantId storeMerchantId)
            throws DefaultStoreNotRemovableException, MerchantStoreNotFoundException;

    void update(StoreMerchantId storeMerchantId, PersistableMerchantStore store)
            throws MerchantStoreNotFoundException;

}
