package com.asrevo.cvhome.certificatemanager.commons.domain;

public enum DomainCertificateStatus {
    INITIATED,
    FIRST_ORDERING,
    RENEWING_ORDER_PROCESS,
    EXPIRED_CERTIFICATE,
    EXPIRING_CERTIFICATE_SOON,
    ACTIVE_CERTIFICATE_GENERATED,
    FAILED_CERTIFICATE_GENERATING
}
