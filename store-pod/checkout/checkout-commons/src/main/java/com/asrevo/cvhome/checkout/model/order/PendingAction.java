package com.asrevo.cvhome.checkout.model.order;

/**
 * The remote step an order is still owed. Placement walks {@code RESERVE → INITIATE_PAYMENT → (COMMIT)}; a signal or
 * a console action can leave {@code COMMIT} or {@code RELEASE} behind. {@code NONE} means nothing outside this service
 * is waiting on the order. Whatever is not {@code NONE} is what {@code OrderRecoveryJob} re-drives.
 */
public enum PendingAction {
    NONE, RESERVE, INITIATE_PAYMENT, COMMIT, RELEASE
}
