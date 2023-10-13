package com.asrevo.cvhome.domaincertificatemanager.domain;

import lombok.Getter;
import lombok.Setter;
import org.shredzone.acme4j.Certificate;

import java.util.Date;

@Getter
@Setter
public class DomainCertificate {

    private Date notAfter;

    private Date notBefore;

    private String serialNumber;

    private Integer version;

    private String sigAlgName;

    private String sigAlgOID;


    public DomainCertificate(Certificate certificate) {

        if (certificate != null && certificate.getCertificate() != null) {
            this.notAfter = certificate.getCertificate().getNotAfter();
            this.notBefore = certificate.getCertificate().getNotAfter();
            this.serialNumber = certificate.getCertificate().getSerialNumber().toString();
            this.version = certificate.getCertificate().getVersion();
            this.sigAlgName = certificate.getCertificate().getSigAlgName();
            this.sigAlgOID = certificate.getCertificate().getSigAlgOID();
        }
    }

}
