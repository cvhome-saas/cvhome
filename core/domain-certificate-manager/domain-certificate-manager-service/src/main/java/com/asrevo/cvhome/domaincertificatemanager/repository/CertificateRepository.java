package com.asrevo.cvhome.domaincertificatemanager.repository;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateId;
import com.asrevo.cvhome.domaincertificatemanager.entity.CertificateEntity;
import org.springframework.data.repository.CrudRepository;

public interface CertificateRepository extends CrudRepository<CertificateEntity, CertificateId> {
}
