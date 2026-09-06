package com.asrevo.cvhome.checkout.services.money;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import com.asrevo.cvhome.checkout.errors.PriceNotFormattableException;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.errors.UncheckedBaseException;

/**
 * Renders an amount in a store's currency for the request's locale — the {@code display*} strings both frontends
 * show as-is.
 */
public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String format(BigDecimal amount, CurrencyCode currency, Locale locale) {
        try {
            NumberFormat format = NumberFormat.getCurrencyInstance(locale);
            format.setCurrency(Currency.getInstance(currency.code()));
            return format.format(amount == null ? BigDecimal.ZERO : amount);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new UncheckedBaseException(PriceNotFormattableException.of(amount, currency, e));
        }
    }
}
