package com.asrevo.cvhome.store.core.services.reference.currency;

import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface CurrencyService extends SalesManagerEntityService<Long, Currency> {

    Currency getByCode(String code);
}
