package com.asrevo.cvhome.billing.repository.projection;

import com.asrevo.cvhome.commons.domain.CurrencyCode;

/** One currency's slice of a filtered ledger, summed in the database. */
public record InvoiceTotalRow(CurrencyCode currency, Long paid, Long due, long invoices) {
}
