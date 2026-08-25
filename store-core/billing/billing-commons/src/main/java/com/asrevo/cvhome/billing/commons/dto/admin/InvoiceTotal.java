package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;

import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

/**
 * What one currency's slice of a filtered ledger comes to.
 *
 * <p>
 * <strong>One entry per currency, never a sum across them.</strong> Nothing on this platform holds an exchange
 * rate, so a mixed total would be a wrong number rather than a missing one.
 * </p>
 *
 * @param invoices how many invoices the figures cover, so a total of zero can be told from no invoices at all
 */
public record InvoiceTotal(CurrencyCode currency, Money paid, Money due, long invoices) implements Serializable {
}
