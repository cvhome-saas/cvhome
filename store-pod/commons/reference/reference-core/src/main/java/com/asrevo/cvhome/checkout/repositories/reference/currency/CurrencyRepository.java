package com.asrevo.cvhome.checkout.repositories.reference.currency;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.checkout.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;

public interface CurrencyRepository extends JpaRepository<Currency, CurrencyCode> {

    Currency getByCode(CurrencyCode code);

}
