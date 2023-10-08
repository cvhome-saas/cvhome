package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.certificatemanager.entity.OwnerEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface DomainRepository extends ListCrudRepository<DomainEntity, DomainId> {
    Optional<DomainEntity> findByDomain(Domain domain);

    List<DomainEntity> findByOwnerAndStatusIs(AggregateReference<OwnerEntity, IdentityId> reference, DomainCertificateStatus domainCertificateStatus);
}
