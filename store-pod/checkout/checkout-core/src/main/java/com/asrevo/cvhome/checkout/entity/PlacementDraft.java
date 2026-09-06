package com.asrevo.cvhome.checkout.entity;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.OrderRef;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

/**
 * Everything {@link Order#place} needs to open an order, before its lines are added.
 */
public record PlacementDraft(StoreMerchantId store, OrderRef ref, CartCode cartCode, Customer customer,
                             LanguageCode language, CurrencyCode currency, PaymentType paymentType,
                             AddressSnapshot billing, AddressSnapshot delivery, String comments) {
}
