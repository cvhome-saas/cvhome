package com.asrevo.cvhome.billing.commons;

import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.CurrencyCode;

/**
 * An amount in a currency, held in <em>minor units</em> — cents, not euros.
 *
 * <p>
 * Minor units are what Stripe speaks, and keeping the same representation end to end removes the rounding step that
 * would otherwise sit between our catalog and an invoice. Zero-decimal currencies (JPY and friends) work unchanged
 * because the unit is defined by the currency, not assumed to be 1/100.
 * </p>
 *
 * @param currency    ISO-4217 code
 * @param minorUnits  the amount, in the currency's smallest unit
 */
public record Money(CurrencyCode currency, Long minorUnits) implements Serializable {

    public static Money of(String currency, long minorUnits) {
        return new Money(new CurrencyCode(currency), minorUnits);
    }

    public boolean free() {
        return minorUnits == null || minorUnits == 0L;
    }

}
