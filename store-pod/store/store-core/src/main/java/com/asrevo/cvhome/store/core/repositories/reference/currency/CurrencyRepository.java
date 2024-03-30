package com.asrevo.cvhome.store.core.repositories.reference.currency;

import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {


    Currency getByCode(String code);
}
