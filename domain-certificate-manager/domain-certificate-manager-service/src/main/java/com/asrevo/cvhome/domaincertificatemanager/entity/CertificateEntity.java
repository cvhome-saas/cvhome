package com.asrevo.cvhome.domaincertificatemanager.entity;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateId;
import com.asrevo.cvhome.commons.domain.BaseEntity;
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
