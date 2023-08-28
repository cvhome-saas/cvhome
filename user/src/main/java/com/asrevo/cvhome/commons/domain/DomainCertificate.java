package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.shredzone.acme4j.Certificate;

import java.util.Date;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainCertificate {

    private String domain;

    private String status;

    private DomainCertificateOrder domainCertificateOrder;

    private Date notAfter;

    private Date notBefore;

    private String serialNumber;

    private Integer version;

    private String sigAlgName;

    private String sigAlgOID;

    private DomainCertificate(DomainCertificateOrder domainCertificateOrder, String status) {
        this.domain = domainCertificateOrder.getDomain();
        this.status = status;
        this.domainCertificateOrder = domainCertificateOrder;
    }

    public DomainCertificate(DomainCertificateOrder domainCertificateOrder, Certificate certificate, String status) {
        this(domainCertificateOrder, status);

        if (certificate != null && certificate.getCertificate() != null) {
            this.notAfter = certificate.getCertificate().getNotAfter();
            this.notBefore = certificate.getCertificate().getNotAfter();
            this.serialNumber = certificate.getCertificate().getSerialNumber().toString();
            this.version = certificate.getCertificate().getVersion();
            this.sigAlgName = certificate.getCertificate().getSigAlgName();
            this.sigAlgOID = certificate.getCertificate().getSigAlgOID();
        }
    }

    public static DomainCertificate from(DomainCertificateOrder domainCertificateOrder, String status) {
        return new DomainCertificate(domainCertificateOrder, status);
    }
}
