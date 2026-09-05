package com.asrevo.cvhome.checkout.services.reference.currency;

import com.asrevo.cvhome.checkout.entity.reference.currency.Currency;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

public interface CurrencyService extends SalesManagerEntityService<CurrencyCode, Currency> {

    Currency getByCode(CurrencyCode code);

}
