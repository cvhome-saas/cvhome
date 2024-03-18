package com.asrevo.cvhome.domaincertificatemanager.entity;

import com.asrevo.cvhome.commons.domain.DomainId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("owner_domain_ref")
@Getter
@Setter
public class OwnerDomainRefEntity {
    @Column("domain_id")
    private AggregateReference<DomainEntity, DomainId> domain;
}
