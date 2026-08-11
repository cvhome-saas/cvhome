package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

/**
 * Asks whether an org may create another store.
 *
 * @param org the org asking
 */
public record StoreQuotaRequest(ManagerOrgId org) implements Serializable {
}
