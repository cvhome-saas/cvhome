package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.entity.CertificateEntity;
import com.asrevo.cvhome.commons.domain.CertificateId;
import org.springframework.data.repository.CrudRepository;

public interface CertificateRepository extends CrudRepository<CertificateEntity, CertificateId> {
}
