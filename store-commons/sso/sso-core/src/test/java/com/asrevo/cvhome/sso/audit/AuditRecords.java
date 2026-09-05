package com.asrevo.cvhome.sso.audit;

/**
 * Reads an {@link AuditRecord} from outside its package.
 *
 * <p>
 * {@code AuditRecord}'s accessors are package-private on purpose — only {@link AuditService} turns one into a row —
 * but the listeners that build them live in other packages, and a test that can only assert "something was
 * recorded" is a test that would pass with the wrong event type, the wrong actor, or a rejection filed as a
 * success. This fixture lends those tests the reader without widening the production API.
 * </p>
 */
public final class AuditRecords {

    private AuditRecords() {
    }

    public static AuditEventType typeOf(AuditRecord record) {
        return record.type();
    }

    public static AuditOutcome outcomeOf(AuditRecord record) {
        return record.outcome();
    }

    public static String reasonCodeOf(AuditRecord record) {
        return record.reasonCode();
    }

    public static AuditTargetType targetTypeOf(AuditRecord record) {
        return record.targetType();
    }

    public static String targetIdOf(AuditRecord record) {
        return record.targetId();
    }

    public static String targetNameOf(AuditRecord record) {
        return record.targetName();
    }

    public static String clientIdOf(AuditRecord record) {
        return record.clientId();
    }

    public static Object beforeOf(AuditRecord record) {
        return record.before();
    }

    public static Object afterOf(AuditRecord record) {
        return record.after();
    }

    public static String detailOf(AuditRecord record) {
        return record.detail();
    }

    public static AuditActor actorOf(AuditRecord record) {
        return record.actor();
    }

}
