package com.asrevo.cvhome.podregistry.commons;

/** Who made a change to a pod — an operator through the API, a scheduled job, or the service itself. */
public enum AuditSource {

    API,
    JOB,
    SYSTEM

}
