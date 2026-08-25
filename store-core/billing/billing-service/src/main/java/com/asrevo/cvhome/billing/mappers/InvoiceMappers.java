package com.asrevo.cvhome.billing.mappers;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.dto.InvoiceView;
import com.asrevo.cvhome.billing.domain.SubscriptionInvoiceEntity;

/**
 * One invoice row, as a store's own history renders it.
 *
 * <p>
 * Extracted from {@code InvoiceServiceImpl}, where it was private, once the platform ledger needed the same shape.
 * A second copy is how the two would end up disagreeing about which URL a row links to.
 * </p>
 */
@Component
public class InvoiceMappers {

    public InvoiceView toView(SubscriptionInvoiceEntity entity) {
        return new InvoiceView(entity.getId(), entity.getInvoiceNumber(), entity.getStatus(), entity.amountDue(),
                entity.amountPaid(), entity.getPeriodStart(), entity.getPeriodEnd(), entity.getIssuedAt(),
                entity.getPaidAt(), entity.getHostedInvoiceUrl(), entity.getInvoicePdfUrl());
    }

}
