package com.asrevo.cvhome.tenancy.manager.entity;

import java.time.Instant;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.commons.dto.StoreStatus;
import com.asrevo.cvhome.tenancy.events.store.StoreCreatedEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(schema = "tenancy", name = "manager_store")
public class ManagerStoreEntity extends BaseEntity<ManagerStoreEntity, StoreMerchantId> {

    /** Matches {@code manager_store.provisioning_error}; a pod's problem detail can run longer than the column. */
    private static final int PROVISIONING_ERROR_MAX = 500;

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

    @Column("status")
    private StoreStatus status;

    /**
     * Why the last provisioning attempt failed, as the pod described it. Null unless
     * {@link #provisioningState} is {@code FAILED_PROVISIONING}.
     */
    @Column("provisioning_error")
    private String provisioningError;

    public static ManagerStoreEntity createStore(CreateStoreRequest request, ManagerOrgId orgId, PodId podId) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.id = entity.generateId();
        entity.setName(request.getName());
        entity.setCreatedDate(Instant.now());
        entity.setOrgId(orgId);
        entity.setPodId(podId);
        entity.provisioningState = ProvisioningState.NOT_STARTED_PROVISIONING;
        entity.status = StoreStatus.ACTIVE;
        entity.registerEvent(StoreCreatedEvent.from(entity.getId(), orgId, podId, request));
        return entity;
    }

    @Override
    protected StoreMerchantId generateId() {
        return StoreMerchantId.newId();
    }

    public ManagerStoreEntity completeProvisioning() {
        this.provisioningState = ProvisioningState.SUCCESSFULLY_PROVISIONING;
        this.provisioningError = null;
        return this;
    }

    /**
     * Records the refusal along with the state, truncated to the column.
     *
     * <p>
     * The reason is part of the transition rather than a separate setter: a FAILED row without one is the state this
     * change exists to remove, and taking the reason as an argument is what stops a caller producing it by accident.
     * </p>
     */
    public ManagerStoreEntity failProvisioning(String reason) {
        this.provisioningState = ProvisioningState.FAILED_PROVISIONING;
        this.provisioningError = truncate(reason);
        return this;
    }

    public ManagerStoreEntity startProvisioning() {
        this.provisioningState = ProvisioningState.IN_PROGRESS_PROVISIONING;
        // A retry that is about to run again must not still be showing the previous attempt's reason.
        this.provisioningError = null;
        return this;
    }

    private static String truncate(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.length() <= PROVISIONING_ERROR_MAX ? reason : reason.substring(0, PROVISIONING_ERROR_MAX);
    }

}
