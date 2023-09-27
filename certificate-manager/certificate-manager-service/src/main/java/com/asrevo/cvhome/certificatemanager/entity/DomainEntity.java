package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.commons.event.domain.DomainRegisteredEvent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("domain")
public class DomainEntity extends BaseEntity<DomainEntity, DomainId> {
    @NotNull
    @Embedded(onEmpty = USE_NULL)
    private Domain domain;
    private DomainCertificateStatus status;
    private boolean autoRenew;
    private boolean autoOrder;
    @MappedCollection(idColumn = "domain_id")
    private Set<DomainCertificateRefEntity> certificates;
    @MappedCollection(idColumn = "domain_id")
    private Set<DomainOrdersRefEntity> orders;
    @Column("active_certificate_id")
    private AggregateReference<CertificateEntity, CertificateId> activeCertificateId;

    public static DomainEntity createDomain(Domain domain, boolean autoRenew, boolean autoOrder) {
        DomainEntity entity = new DomainEntity();
        entity.setNew();
        entity.setDomain(domain);
        entity.setAutoRenew(autoRenew);
        entity.setAutoOrder(autoOrder);
        entity.setStatus(DomainCertificateStatus.INITIATED);
        entity.registerEvent(DomainRegisteredEvent.from(entity.getId(), domain, autoRenew, autoOrder));
        return entity;
    }

    @Override
    protected DomainId generateId() {
        return DomainId.newId();
    }

    private void addNewCertificate(CertificateId certificateId) {
        if (this.getCertificates() == null) this.setCertificates(new HashSet<>());
        DomainCertificateRefEntity e = new DomainCertificateRefEntity();
        e.setCertificate(AggregateReference.to(certificateId));
        this.getCertificates().add(e);
    }

    private void addNewOrders(OrdersId ordersId) {
        if (this.getOrders() == null) this.setOrders(new HashSet<>());
        DomainOrdersRefEntity e = new DomainOrdersRefEntity();
        e.setOrders(AggregateReference.to(ordersId));
        this.getOrders().add(e);
    }

    public void addNewCertificate(OrdersId ordersId, CertificateId certificateId) {
        this.setActiveCertificateId(AggregateReference.to(certificateId));
        this.setStatus(DomainCertificateStatus.ACTIVE_CERTIFICATE_GENERATED);
        this.addNewCertificate(certificateId);
        this.addNewOrders(ordersId);
    }
}
