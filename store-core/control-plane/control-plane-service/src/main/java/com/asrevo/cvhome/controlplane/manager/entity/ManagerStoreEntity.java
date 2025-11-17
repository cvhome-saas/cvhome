package com.asrevo.cvhome.controlplane.manager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.manager.commons.dto.ProvisioningState;
import com.asrevo.cvhome.manager.commons.event.store.StoreCreatedEvent;
import com.asrevo.cvhome.manager.commons.event.store.StoreProvisionedEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Table(schema = "manager", name = "manager_store")
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

	public static ManagerStoreEntity createStore(Map<Object, Object> request, ManagerOrgId orgId, PodId podId) {
		ManagerStoreEntity entity = new ManagerStoreEntity();
		entity.id = entity.generateId();
		String storeName = request.get("name").toString();
		entity.setName(storeName);
		entity.setCreatedDate(Instant.now());
		entity.setOrgId(orgId);
		entity.setPodId(podId);
		entity.provisioningState = ProvisioningState.NOT_STARTED_PROVISIONING;
		entity.registerEvent(StoreCreatedEvent.from(entity.getId(), orgId, podId, request));
		return entity;
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
