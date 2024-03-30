package com.asrevo.cvhome.store.service.mapper;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;

public interface Mapper<S, T> {

    T convert(S source, MerchantStore store, Language language);

    T merge(S source, T destination, MerchantStore store, Language language);


}
