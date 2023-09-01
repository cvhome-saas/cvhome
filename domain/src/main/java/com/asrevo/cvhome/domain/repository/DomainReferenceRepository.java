package com.asrevo.cvhome.domain.repository;

import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.domain.DomainType;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DomainReferenceRepository extends ReactiveCrudRepository<DomainReference, Long> {

    Mono<DomainReference> findOneByDomain(String domain);

    @Query("SELECT dr.* FROM domain_reference dr WHERE dr.reference = :reference and (dr.domain_type= :domainType or :domainType is null)")
    Flux<DomainReference> getAllByReferenceAndDomainType(String reference, DomainType domainType);

    @Modifying
    @Query("update domain_reference set external_acm_order_id = :externalAcmOrderId where id= :domainId")
    Mono<Long> updateExternalAcmOrderId(Long domainId, Long externalAcmOrderId);
}
