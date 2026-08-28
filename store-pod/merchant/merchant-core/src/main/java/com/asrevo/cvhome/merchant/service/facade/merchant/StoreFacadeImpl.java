package com.asrevo.cvhome.merchant.service.facade.merchant;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.errors.DefaultStoreNotRemovableException;
import com.asrevo.cvhome.merchant.errors.DuplicateMerchantStoreException;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.populator.merchant.PersistableMerchantStorePopulator;
import com.asrevo.cvhome.merchant.service.populator.merchant.ReadableMerchantStorePopulator;
import com.asrevo.cvhome.merchant.services.merchant.MerchantStoreService;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1;

@Service("storeFacade")
@Slf4j
public class StoreFacadeImpl implements StoreFacade {

    private final MerchantStoreService merchantStoreService;

    private final PersistableMerchantStorePopulator persistableMerchantStorePopulator;

    private final ReadableMerchantStorePopulator readableMerchantStorePopulator;

    public StoreFacadeImpl(MerchantStoreService merchantStoreService,
                           PersistableMerchantStorePopulator persistableMerchantStorePopulator,
                           ReadableMerchantStorePopulator readableMerchantStorePopulator) {
        this.merchantStoreService = merchantStoreService;
        this.persistableMerchantStorePopulator = persistableMerchantStorePopulator;
        this.readableMerchantStorePopulator = readableMerchantStorePopulator;
    }

    @Override
    public MerchantStore get(StoreMerchantId storeMerchantId) {
        return merchantStoreService.getByMerchantStoreId(storeMerchantId);
    }

    @Override
    public ReadableMerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId, LanguageCode lang)
            throws MerchantStoreNotFoundException {
        MerchantStore store = getMerchantStoreByMerchantStoreId(storeMerchantId);
        return convertMerchantStoreToReadableMerchantStore(store, lang);
    }

    private ReadableMerchantStore convertMerchantStoreToReadableMerchantStore(MerchantStore store,
                                                                              LanguageCode language) {
        ReadableMerchantStore readable = new ReadableMerchantStore();

        // The populator declares no failure and this catch only ever saw unchecked ones, so it turned a bug in our own
        // mapping code into a 400 blaming the caller.
        readableMerchantStorePopulator.populate(store, readable, store, language);
        return readable;
    }

    private MerchantStore getMerchantStoreByMerchantStoreId(StoreMerchantId storeMerchantId)
            throws MerchantStoreNotFoundException {
        return Optional.ofNullable(get(storeMerchantId))
                .orElseThrow(() -> MerchantStoreNotFoundException.of(storeMerchantId));
    }

    @Override
    public void create(PersistableMerchantStore store) throws DuplicateMerchantStoreException {
        MerchantStore storeForCheck = get(new StoreMerchantId(store.getId()));
        if (storeForCheck != null) {
            // Was a 400 that said "MerhantStore ... already exists"; a taken id is a conflict, not a malformed request.
            throw DuplicateMerchantStoreException.of(store.getId());
        }

        MerchantStore mStore = convertPersistableMerchantStoreToMerchantStore(store, LanguageCode.defaultLanguage());
        createMerchantStore(mStore);
    }

    private void createMerchantStore(MerchantStore mStore) {
        merchantStoreService.saveOrUpdate(mStore);
    }

    private MerchantStore convertPersistableMerchantStoreToMerchantStore(PersistableMerchantStore store,
                                                                         LanguageCode language) {
        MerchantStore mStore = new MerchantStore();
        store.setStoreDomains(Set.of(new ManagerStoreDomain(store.getName(), DomainType.SUB_DOMAIN)));

        mStore = persistableMerchantStorePopulator.populate(store, mStore, language);
        return mStore;
    }

    @Override
    public void update(StoreMerchantId storeMerchantId, PersistableMerchantStore store)
            throws MerchantStoreNotFoundException {
        MerchantStore mStore = mergePersistableMerchantStoreToMerchantStore(store, storeMerchantId,
                LanguageCode.defaultLanguage());

        updateMerchantStore(mStore);
    }

    private void updateMerchantStore(MerchantStore mStore) {
        merchantStoreService.update(mStore);
    }

    private MerchantStore mergePersistableMerchantStoreToMerchantStore(PersistableMerchantStore store,
                                                                       StoreMerchantId storeMerchantId, LanguageCode language)
            throws MerchantStoreNotFoundException {

        MerchantStore mStore = getMerchantStoreByMerchantStoreId(storeMerchantId);

        store.setId(mStore.getId().getId());
        store.setOrg(mStore.getOrg());

        store.setStoreDomains(mStore.getStoreDomains());

        mStore = persistableMerchantStorePopulator.populate(store, mStore, language);
        return mStore;
    }

    @Override
    public void delete(StoreMerchantId storeMerchantId)
            throws DefaultStoreNotRemovableException, MerchantStoreNotFoundException {

        if (DEFAULT_ORG1_STORE1.equals(storeMerchantId)) {
            throw DefaultStoreNotRemovableException.of(storeMerchantId);
        }

        MerchantStore mStore = getMerchantStoreByMerchantStoreId(storeMerchantId);

        // The catch (Exception) here flattened a constraint violation — a store still referenced by orders — and a
        // store outage into the same message. Both are unchecked now: DataIntegrityErrorHandler renders the first as
        // a 409 and the shared advice renders the second as a 500 with a traceId.
        merchantStoreService.delete(mStore);
    }

    private MerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId)
            throws MerchantStoreNotFoundException {
        return getMerchantStoreByMerchantStoreId(storeMerchantId);
    }

    private void saveMerchantStore(MerchantStore store) {
        merchantStoreService.save(store);
    }

    @Override
    public List<LanguageCode> supportedLanguages(StoreMerchantId storeMerchantId) {
        MerchantStore store = merchantStoreService.getByMerchantStoreId(storeMerchantId);

        if (store != null) {
            return store.getLanguages();
        }

        return Collections.emptyList();
    }

    @Override
    public ReadableMerchantStore getReadableMerchantStoreId(StoreMerchantId storeMerchantId) {
        MerchantStore merchantStore = get(storeMerchantId);
        return convertMerchantStoreToReadableMerchantStore(merchantStore, merchantStore.getDefaultLanguageCode());
    }

}
