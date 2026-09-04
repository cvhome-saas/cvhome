package com.asrevo.cvhome.sso.dto;

import com.asrevo.cvhome.sso.audit.AuditEventType;

/** One entry of the type catalogue: the wire name and the group the screen files it under. */
public record AuditTypeDto(String type, AuditEventType.AuditCategory category) {

    public static AuditTypeDto of(AuditEventType type) {
        return new AuditTypeDto(type.wire(), type.category());
    }

}
