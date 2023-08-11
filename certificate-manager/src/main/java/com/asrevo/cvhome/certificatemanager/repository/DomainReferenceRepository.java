package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.domain.DomainReference;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DomainReferenceRepository extends CrudRepository<DomainReference, Long> {

    Optional<DomainReference> findOneByDomain(String domain);
}
