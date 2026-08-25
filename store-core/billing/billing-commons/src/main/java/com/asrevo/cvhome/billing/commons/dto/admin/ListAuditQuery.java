package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/** What narrows the subscription audit trail. The range is over {@code occurred_at}. */
public record ListAuditQuery(StoreMerchantId store, ManagerOrgId org, AuditEventType eventType, ChangeSource source,
                             Instant from, Instant to) implements Serializable {
}
