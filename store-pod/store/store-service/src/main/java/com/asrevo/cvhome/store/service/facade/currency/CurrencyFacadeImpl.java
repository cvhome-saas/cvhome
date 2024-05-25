package com.asrevo.cvhome.store.service.facade.currency;

import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.services.reference.currency.CurrencyService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class CurrencyFacadeImpl implements CurrencyFacade {

    private final CurrencyService currencyService;

    public CurrencyFacadeImpl(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Override
    public List<Currency> getList() {
        List<Currency> currencyList = currencyService.list();
        if (currencyList.isEmpty()) {
            throw new ResourceNotFoundException("No languages found");
        }
        currencyList.sort(Comparator.comparing(Currency::getCode));
        return currencyList;
    }
}
