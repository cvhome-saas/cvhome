package com.asrevo.cvhome.sso.audit;

/** Who did it: a person, an OAuth2 client, uaa itself (a job), or nobody yet (a failed login). */
public enum AuditActorType {
    USER, CLIENT, SYSTEM, ANONYMOUS
}
