package com.asrevo.cvhome.certificatemanager.commons.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChallengeValidationType {
    Http01("http-01", true), TlsAlpn01("tls-alpn-01", true), Dns01("dns-01", false);
    private final String challenge;
    private final boolean automaticValidation;
}
