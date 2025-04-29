package com.asrevo.cvhome.manager.entity;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.manager.commons.dto.ProvisioningState;
import com.asrevo.cvhome.manager.commons.event.store.StoreCreatedEvent;
import com.asrevo.cvhome.manager.commons.event.store.StoreProvisionedEvent;
import com.asrevo.cvhome.manager.dto.StoreDomainDto;
import com.asrevo.cvhome.manager.dto.StoreDomainList;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Table("manager_store")
public class ManagerStoreEntity extends BaseEntity<ManagerStoreEntity, ManagerStoreId> {
    @Column("name")
    private String name;
    @Column("org_id")
    private ManagerOrgId orgId;
    @Column("created_date")
    private Instant createdDate;
    @Column("pod_id")
    private PodId podId;
    @Column("provisioning_state")
    private ProvisioningState provisioningState;
    @MappedCollection(idColumn = "manager_store_id")
    private ManagerStoreDomains managerStoreDomains;

    public static ManagerStoreEntity createStore(Map<Object, Object> request, ManagerOrgId orgId, PodId podId) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.id = entity.generateId();
        String storeName = request.get("name").toString();
        entity.setName(storeName);
        entity.setCreatedDate(Instant.now());
        entity.setOrgId(orgId);
        entity.setPodId(podId);
        entity.provisioningState = ProvisioningState.NOT_STARTED_PROVISIONING;
        entity.managerStoreDomains = ManagerStoreDomains.of(new ManagerStoreDomain(storeName, AlisType.SUB_DOMAIN));
        entity.registerEvent(StoreCreatedEvent.from(entity.getId(), orgId, podId, request));
        return entity;
    }

    public ManagerStoreEntity addDomain(Domain domain) {
        this.managerStoreDomains = managerStoreDomains.addDomain(domain);
        return this;
    }

    public ManagerStoreEntity removeDomain(Domain domain) {
        this.managerStoreDomains = managerStoreDomains.removeDomain(domain);
        return this;
    }

    public StoreDomainList domains() {
        return new StoreDomainList(this.managerStoreDomains.stream().map(it -> new StoreDomainDto(it.domain(), it.domainType())).toList());
    }

    @Override
    protected ManagerStoreId generateId() {
        return ManagerStoreId.newId();
    }

    public ManagerStoreEntity completeProvisioning() {
        this.provisioningState = ProvisioningState.SUCCESSFULLY_PROVISIONING;
        this.registerEvent(StoreProvisionedEvent.from(this.getId(), podId, this.provisioningState));
        return this;
    }

    public ManagerStoreEntity failProvisioning() {
        this.provisioningState = ProvisioningState.FAILED_PROVISIONING;
        this.registerEvent(StoreProvisionedEvent.from(this.getId(), podId, this.provisioningState));
        return this;
    }

    public ManagerStoreEntity startProvisioning() {
        this.provisioningState = ProvisioningState.IN_PROGRESS_PROVISIONING;
        this.registerEvent(StoreProvisionedEvent.from(this.getId(), podId, this.provisioningState));
        return this;
    }
}
