package com.asrevo.cvhome.sso.audit;

/** Who an event is attributed to. */
public record AuditActor(AuditActorType type, String id, String name) {

    private static final String SYSTEM_NAME = "system";

    public static final AuditActor SYSTEM = new AuditActor(AuditActorType.SYSTEM, SYSTEM_NAME, SYSTEM_NAME);

    public static final AuditActor ANONYMOUS = new AuditActor(AuditActorType.ANONYMOUS, null, null);

}
