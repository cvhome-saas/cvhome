package com.asrevo.cvhome.order.repositories.reference.currency;

import com.asrevo.cvhome.order.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, CurrencyCode> {

	Currency getByCode(CurrencyCode code);

}
