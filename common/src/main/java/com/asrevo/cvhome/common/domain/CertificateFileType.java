package com.asrevo.cvhome.common.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CertificateFileType {
    CRT("crt"), CSR("csr"), KEY("key");
    private final String type;

    public String getFile() {
        return "domain." + type;
    }
}
