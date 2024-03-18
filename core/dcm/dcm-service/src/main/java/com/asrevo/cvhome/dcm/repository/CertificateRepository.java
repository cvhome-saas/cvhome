package com.asrevo.cvhome.dcm.repository;

import com.asrevo.cvhome.dcm.commons.domain.CertificateId;
import com.asrevo.cvhome.dcm.entity.CertificateEntity;
import org.springframework.data.repository.CrudRepository;

public interface CertificateRepository extends CrudRepository<CertificateEntity, CertificateId> {
}
