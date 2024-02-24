package com.asrevo.cvhome.domaincertificatemanager.commons.domain;

public enum CertificateOrderStatus {
    INITIATED,
    REQUESTED,
    VALIDATION_REQUESTED,
    PRE_VALIDATED_INVALID,
    VALIDATED_VALID,
    VALIDATED_INVALID,
    GENERATED,
    FAIL_GENERATING
}
