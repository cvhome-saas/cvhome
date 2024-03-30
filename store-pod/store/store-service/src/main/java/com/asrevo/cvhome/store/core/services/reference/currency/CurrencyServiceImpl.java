package com.asrevo.cvhome.store.core.services.reference.currency;

import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.repositories.reference.currency.CurrencyRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("currencyService")
public class CurrencyServiceImpl extends SalesManagerEntityServiceImpl<Long, Currency>
        implements CurrencyService {

    private CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        super(currencyRepository);
        this.currencyRepository = currencyRepository;
    }

    @Override
    public Currency getByCode(String code) {
        return currencyRepository.getByCode(code);
    }

}
