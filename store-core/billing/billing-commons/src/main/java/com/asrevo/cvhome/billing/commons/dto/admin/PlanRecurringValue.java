package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;

import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;

/**
 * What one plan is contracted to bring in, per currency.
 *
 * <p>
 * <strong>The annual figure is the one the database computes, and the monthly one is derived from it.</strong>
 * Dividing a yearly price by twelve truncates on every row — a 1199/yr plan contributes 99.91 and four hundred of
 * them lose 33 to the floor — while multiplying a monthly one by twelve is exact in {@code bigint}. The single
 * division happens once, on the aggregate, and both figures ship so no caller ever has to divide either.
 * </p>
 *
 * <p>
 * {@code status} stays in the key rather than being filtered to {@code ACTIVE}: whether an operator wants a
 * committed run rate or one that counts trials is their judgement, and an endpoint that silently picked one is the
 * classic way to overstate a book.
 * </p>
 */
public record PlanRecurringValue(String planCode, SubscriptionStatus status,
                                 long subscriptions, Money monthly, Money annual) implements Serializable {
}
