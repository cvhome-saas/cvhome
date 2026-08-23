package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * What narrows the platform's invoice ledger.
 *
 * <p>
 * The date range is over {@code issued_at} rather than {@code paid_at}: an operator filtering the ledger is asking
 * "what did we bill in August", and an unpaid invoice has no {@code paid_at} to be found by. The revenue statistic
 * makes the opposite choice, for the opposite reason.
 * </p>
 */
public record ListInvoiceQuery(ManagerOrgId org, StoreMerchantId store, InvoiceStatus status, Instant from,
                               Instant to) implements Serializable {
}
