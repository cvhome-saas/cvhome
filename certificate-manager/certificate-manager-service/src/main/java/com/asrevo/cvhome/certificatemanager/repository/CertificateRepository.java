package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateId;
import com.asrevo.cvhome.certificatemanager.entity.CertificateEntity;
import org.springframework.data.repository.CrudRepository;

public interface CertificateRepository extends CrudRepository<CertificateEntity, CertificateId> {
}
