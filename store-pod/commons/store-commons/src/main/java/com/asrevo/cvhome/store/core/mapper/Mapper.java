package com.asrevo.cvhome.store.core.mapper;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface Mapper<S, T> {

    T convert(S source, StoreMerchantId store, LanguageCode language);

    T merge(S source, T destination, StoreMerchantId store, LanguageCode language);

}
