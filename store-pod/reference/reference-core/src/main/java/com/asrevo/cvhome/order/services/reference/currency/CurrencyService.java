package com.asrevo.cvhome.order.services.reference.currency;

import com.asrevo.cvhome.order.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface CurrencyService extends SalesManagerEntityService<CurrencyCode, Currency> {

    Currency getByCode(CurrencyCode code);
}
