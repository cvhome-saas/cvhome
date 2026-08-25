package com.asrevo.cvhome.tenancy.commons.dto;

/** Who made the change — an operator through the API, a scheduled job, or the service itself. */
public enum AuditSource {

    API,
    JOB,
    SYSTEM

}
