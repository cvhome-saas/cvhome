package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.CertificateId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table("certificate")
public class CertificateEntity extends BaseEntity<CertificateEntity, CertificateId> {
    private Instant notAfter;
    private Instant notBefore;
    private String serialNumber;
    private Integer version;
    private String sigAlgName;
    private String sigAlgOID;


    public static CertificateEntity createNewCertificate(Instant notAfter, Instant notBefore, String serialNumber, Integer version, String sigAlgName, String sigAlgOID) {
        CertificateEntity certificate = new CertificateEntity();
        certificate.setNew();
        certificate.notAfter = notAfter;
        certificate.notBefore = notBefore;
        certificate.serialNumber = serialNumber;
        certificate.version = version;
        certificate.sigAlgName = sigAlgName;
        certificate.sigAlgOID = sigAlgOID;
        return certificate;
    }

    @Override
    protected CertificateId generateId() {
        return CertificateId.newId();
    }
}
