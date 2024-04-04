package com.asrevo.cvhome.store.service.facade.system;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.system.Configs;

public interface MerchantConfigurationFacade {

    Configs getMerchantConfig(MerchantStore merchantStore, Language language);

}
