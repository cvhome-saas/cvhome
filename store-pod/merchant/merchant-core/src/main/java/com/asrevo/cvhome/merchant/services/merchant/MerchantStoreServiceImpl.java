package com.asrevo.cvhome.merchant.services.merchant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

@Service("merchantService")
public class MerchantStoreServiceImpl extends SalesManagerEntityServiceImpl<StoreMerchantId, MerchantStore>
        implements MerchantStoreService {

    private final MerchantRepository merchantRepository;

    @Autowired
    public MerchantStoreServiceImpl(MerchantRepository merchantRepository) {
        super(merchantRepository);
        this.merchantRepository = merchantRepository;
    }

    @Override
    public void saveOrUpdate(MerchantStore store) {
        super.save(store);
    }

    @Override
    public MerchantStore getByMerchantStoreId(StoreMerchantId storeMerchantId) {
        return merchantRepository.findByMerchantStoreId(storeMerchantId);
    }

}
