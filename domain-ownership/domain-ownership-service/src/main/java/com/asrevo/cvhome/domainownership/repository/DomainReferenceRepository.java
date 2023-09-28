package com.asrevo.cvhome.domainownership.repository;

public interface DomainReferenceRepository /*extends ReactiveCrudRepository<DomainReference, Long> {

    Mono<DomainReference> findOneByDomain(String domain);

    @Query("SELECT dr.* FROM domain_reference dr WHERE dr.reference = :reference and (dr.domain_type= :domainType or :domainType is null)")
    Flux<DomainReference> getAllByReferenceAndDomainType(String reference, DomainType domainType);

    @Modifying
    @Query("update domain_reference set external_acm_order_id = :externalAcmOrderId where id= :domainId")
    Mono<Long> updateExternalAcmOrderId(Long domainId, Long externalAcmOrderId);
}
*/ {
}