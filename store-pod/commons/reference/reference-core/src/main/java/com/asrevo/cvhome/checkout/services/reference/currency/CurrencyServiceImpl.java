package com.asrevo.cvhome.checkout.services.reference.currency;

import com.asrevo.cvhome.checkout.entity.reference.currency.Currency;
import com.asrevo.cvhome.checkout.repositories.reference.currency.CurrencyRepository;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("currencyService")
public class CurrencyServiceImpl extends SalesManagerEntityServiceImpl<CurrencyCode, Currency>
		implements CurrencyService {

	private final CurrencyRepository currencyRepository;

	@Autowired
	public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
		super(currencyRepository);
		this.currencyRepository = currencyRepository;
	}

	@Override
	public Currency getByCode(CurrencyCode code) {
		return currencyRepository.getByCode(code);
	}

}
