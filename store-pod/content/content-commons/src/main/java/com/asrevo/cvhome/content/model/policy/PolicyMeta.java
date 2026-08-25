package com.asrevo.cvhome.content.model.policy;

/**
 * The JSON {@code meta} column of a POLICY row.
 */
public record PolicyMeta(String jurisdiction, boolean requiresAcceptance, boolean notifyCustomers,
                         DisplayAt displayAt) {

    public record DisplayAt(boolean footer, boolean checkout, boolean signup) {
    }

}
