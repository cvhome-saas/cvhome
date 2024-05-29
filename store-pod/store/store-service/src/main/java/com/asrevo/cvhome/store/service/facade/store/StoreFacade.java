package com.asrevo.cvhome.store.service.facade.store;

import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStoreCriteria;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.store.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Layer between shop controllers, services and API with sm-core
 *
 * @author carlsamson
 */
public interface StoreFacade {

    /**
     * Find MerchantStore model from store code
     *
     */
    MerchantStore getByCode(HttpServletRequest request);

    MerchantStore get(String code);

    MerchantStore getByCode(String code);

    List<Language> supportedLanguages(MerchantStore store);

    ReadableMerchantStore getByCode(String code, String lang);

    ReadableMerchantStore getFullByCode(String code, String lang);

    ReadableMerchantStoreList findAll(MerchantStoreCriteria criteria, Language language, int page, int count);

    /**
     * List child stores
     *
     */
    ReadableMerchantStoreList getChildStores(Language language, String code, int start, int count);

    ReadableMerchantStore getByCode(String code, Language lang);

    ReadableMerchantStore getFullByCode(String code, Language language);

    boolean existByCode(String code);

    /**
     * List MerchantStore using various criterias
     *
     */
    ReadableMerchantStoreList getByCriteria(MerchantStoreCriteria criteria, Language lang);

    /**
     * Creates a brand new MerchantStore
     *
     */
    //ReadableMerchantStore create(PersistableMerchantStore store);
    void create(PersistableMerchantStore store);

    /**
     * Updates an existing store
     *
     */
    //ReadableMerchantStore update(PersistableMerchantStore store);
    void update(PersistableMerchantStore store);

    /**
     * Deletes a MerchantStore based on store code
     *
     */
    void delete(String code);

    /**
     * Get Logo, social networks and other brand configurations
     *
     */
    ReadableBrand getBrand(String code);

    /**
     * Create store brand
     *
     */
    void createBrand(String merchantStoreCode, PersistableBrand brand);

    /**
     * Delete store logo
     */
    void deleteLogo(String code);

    /**
     * Delete store logo
     */
    void deleteBanner(String code);

    /**
     * Add MerchantStore logo
     *
     */
    void addStoreLogo(String code, InputContentFile cmsContentImage);

    /**
     * Add MerchantStore banner
     *
     */
    void addStoreBanner(String code, InputContentFile cmsContentImage);

    /**
     * Returns store id, code and name only
     *
     */
    List<ReadableMerchantStore> getMerchantStoreNames(MerchantStoreCriteria criteria);

}
