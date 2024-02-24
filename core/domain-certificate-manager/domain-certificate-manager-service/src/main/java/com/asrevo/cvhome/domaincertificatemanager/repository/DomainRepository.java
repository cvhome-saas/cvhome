package com.asrevo.cvhome.domaincertificatemanager.repository;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.domaincertificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.domaincertificatemanager.entity.OwnerEntity;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface DomainRepository extends ListCrudRepository<DomainEntity, DomainId> {
    Optional<DomainEntity> findByDomain(Domain domain);

    List<DomainEntity> findByOwnerAndStatusIs(AggregateReference<OwnerEntity, IdentityId> owner, DomainCertificateStatus domainCertificateStatus);

    List<DomainEntity> findByOwnerAndDomainTypeIn(AggregateReference<OwnerEntity, IdentityId> owner, List<DomainType> domainTypes);
}
